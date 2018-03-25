package better.smartcard.gp.scp;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.keys.GPKey;
import better.smartcard.gp.keys.GPKeySet;
import better.smartcard.gp.keys.GPKeyType;
import better.smartcard.gp.protocol.GP;
import better.smartcard.gp.protocol.GPCrypto;
import better.smartcard.protocol.ISO7816;
import better.smartcard.protocol.SW;
import better.smartcard.protocol.SWException;
import better.smartcard.util.APDUUtil;
import better.smartcard.util.ArrayUtil;
import better.smartcard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Card channel wrapper for GP secure messaging
 */
public class GPSecureChannel extends CardChannel {

    private static final Logger LOG = LoggerFactory.getLogger(GPSecureChannel.class);

    /**
     * Random for generating the host challenge
     */
    private SecureRandom mRandom;

    /**
     * Reference to the card for CardAPDU transactions
     */
    private GPCard mCard;

    /**
     * Underlying card channel
     */
    private CardChannel mChannel;

    /**
     * Protocol policy in effect
     */
    private SCPPolicy mPolicy;

    /**
     * Initial static keys
     */
    private GPKeySet mStaticKeys;

    /**
     * Derived session keys
     */
    private GPKeySet mSessionKeys;

    /**
     * True when authentication has succeeded and not been broken
     */
    private boolean mIsAuthenticated;

    /**
     * SCP protocol and parameters in use
     */
    private SCPParameters mSCP;

    /**
     * Configured SCP protocol
     */
    private int mUseProtocol = 0;
    /**
     * Configured SCP parameters
     */
    private int mUseParameters = 0;

    /**
     * Configured request for ENC
     */
    private boolean mUseENC = false;
    /**
     * Configured request for RMAC
     */
    private boolean mUseRMAC = false;
    /**
     * Configured request for RENC
     */
    private boolean mUseRENC = false;

    /**
     * Helper performing wrapping/unwrapping of APDUs
     */
    private SCPWrapper mWrapper;

    public GPSecureChannel(GPCard card, CardChannel channel, SCPPolicy policy, GPKeySet keys) {
        mRandom = new SecureRandom();
        mCard = card;
        mChannel = channel;
        mPolicy = policy;
        mStaticKeys = keys;
        mUseENC = false;
        mUseRMAC = false;
        mUseRENC = false;
        reset();
    }

    public SCPParameters getProtocol() {
        return mSCP;
    }

    public boolean isAuthenticated() {
        return mIsAuthenticated;
    }

    public void expectProtocol(int protocol, int parameters) throws CardException {
        mPolicy.checkProtocol(protocol, parameters);
        mUseProtocol = protocol;
        mUseParameters = parameters;
    }

    @Override
    public Card getCard() {
        return mChannel.getCard();
    }

    @Override
    public int getChannelNumber() {
        return mChannel.getChannelNumber();
    }

    /**
     * Transmit an APDU through the secure channel
     *
     * @param command to be sent
     * @return the response
     * @throws CardException on card-related errors
     */
    @Override
    public ResponseAPDU transmit(CommandAPDU command) throws CardException {
        boolean traceEnabled = LOG.isTraceEnabled();

        // bug out if the channel is not open
        if (mWrapper == null) {
            throw new CardException("Secure channel is closed");
        }

        // log the command
        if(traceEnabled) {
            LOG.trace("apdu > " + APDUUtil.toString(command));
        }

        // wrap the command (sign, encrypt)
        CommandAPDU wrappedCommand = mWrapper.wrap(command);
        // send the wrapped command
        ResponseAPDU wrappedResponse = mCard.transmit(mChannel, wrappedCommand);
        // unwrap the response, but not if it is an error
        int sw = wrappedResponse.getSW();
        ResponseAPDU response = wrappedResponse;
        if (sw == ISO7816.SW_NO_ERROR || SW.isWarning(sw)) {
            // unwrap the response (decrypt, verify)
            response = mWrapper.unwrap(wrappedResponse);
        } else {
            // data in error responses is illegal
            int dataLen = response.getNr();
            if (dataLen > 0) {
                throw new CardException("Card sent data in an error response");
            }
        }

        // log the response
        if(traceEnabled) {
            LOG.trace("apdu < " + APDUUtil.toString(response));
        }

        return response;
    }

    /**
     * Convenience wrapper for sending byte buffers
     *
     * @param command to be packed into the C-APDU
     * @param response buffer to be filled from R-APDU
     * @return length of data received (XXX define better!?)
     * @throws CardException on card-related errors
     */
    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
        CommandAPDU capdu = new CommandAPDU(command);
        ResponseAPDU rapdu = transmit(capdu);
        byte[] rapduBytes = rapdu.getBytes();
        response.put(rapduBytes);
        return rapduBytes.length;
    }

    @Override
    public void close() throws CardException {
        LOG.debug("closing channel");
        reset();
    }

    public void open() throws CardException {
        LOG.debug("opening secure channel");

        // generate the host challenge
        byte[] hostChallenge = generateChallenge();

        // determine key parameters
        byte keyVersion = (byte) mStaticKeys.getKeyVersion();
        byte keyId = 0;
        if (mUseProtocol == 1) {
            throw new UnsupportedOperationException("SCP01 requires key id in INITIALIZE UPDATE -- which key?");
        }
        LOG.debug("key id " + keyId + " version " + keyVersion);

        // perform INITIALIZE UPDATE, exchanging challenges
        InitUpdateResponse init = performInitializeUpdate(keyVersion, keyId, hostChallenge);

        // check and select the protocol to be used
        checkAndSelectProtocol(init);
        LOG.debug("using " + mSCP);

        // check key version
        int selectedKeyVersion = mStaticKeys.getKeyVersion();
        if (selectedKeyVersion > 0 && selectedKeyVersion != init.keyVersion) {
            throw new CardException("Key version mismatch: host " + selectedKeyVersion + " card " + init.keyVersion);
        }

        // derive session keys
        switch (mSCP.scpProtocol) {
            case 2:
                byte[] seq = Arrays.copyOfRange(init.cardChallenge, 0, 2);
                LOG.debug("card sequence " + HexUtil.bytesToHex(seq));
                mSessionKeys = mStaticKeys.deriveSCP02(seq);
                break;
            default:
                throw new CardException("Unsupported SCP version " + mSCP);
        }

        // XXX
        LOG.info("session keys:\n" + mSessionKeys.toString());

        // verify the card cryptogram
        if (verifyCardCryptogram(hostChallenge, init.cardChallenge, init.cardCryptogram)) {
            LOG.debug("card cryptogram verified");
        } else {
            throw new CardException("Invalid card cryptogram");
        }

        // now generate the host cryptogram
        byte[] hostCryptogram = computeHostCryptogram(hostChallenge, init.cardChallenge);

        // create CardAPDU wrapper
        switch (mSCP.scpProtocol) {
            case 2:
                mWrapper = new SCP0102Wrapper(mSessionKeys, ((SCP0102Parameters) mSCP));
                break;
            case 3:
                mWrapper = new SCP03Wrapper(mSessionKeys, ((SCP03Parameters) mSCP));
            default:
                throw new CardException("Unsupported SCP version " + mSCP);
        }

        // perform EXTERNAL AUTHENTICATE to authenticate to card
        LOG.debug("performing authentication");
        performExternalAuthenticate(hostCryptogram);
        LOG.debug("authentication succeeded");
        mIsAuthenticated = true;

        // can now start ENC, RMAC and RENC - if applicable
        if (mUseENC) {
            LOG.debug("enabling command encryption");
            mWrapper.startENC();
        }
        if (mUseRMAC) {
            LOG.debug("enabling response authentication");
            mWrapper.startRMAC();
        }
        if(mUseRENC) {
            LOG.debug("enabling response encryption");
            mWrapper.startRENC();
        }
    }

    private void reset() {
        mSessionKeys = null;
        mWrapper = null;
        mSCP = null;
        mIsAuthenticated = false;
    }

    /**
     * Generate a challenge for use in authentication.
     *
     * In this version the challenge always is 8 bytes long.
     *
     * @return an 8-byte random challenge
     */
    private byte[] generateChallenge() {
        byte[] result = new byte[8];
        mRandom.nextBytes(result);
        return result;
    }

    /**
     * Compute the card cryptogram for verification
     *
     * @param hostChallenge generated by the host
     * @param cardChallenge sent by the card
     * @return the card cryptogram
     */
    private byte[] computeCardCryptogram(byte[] hostChallenge, byte[] cardChallenge) {
        GPKey encKey = mSessionKeys.getKeyByType(GPKeyType.ENC);
        byte[] cardContext = ArrayUtil.concatenate(hostChallenge, cardChallenge);
        return GPCrypto.mac_3des_nulliv(encKey, cardContext);
    }

    /**
     * Verify the card cryptogram
     *
     * @param hostChallenge used for the cryptogram
     * @param cardChallenge used for the cryptogram
     * @param cardCryptogram to verify
     * @return true if the cryptogram is valid
     */
    private boolean verifyCardCryptogram(byte[] hostChallenge, byte[] cardChallenge, byte[] cardCryptogram) {
        byte[] myCardCryptogram = computeCardCryptogram(hostChallenge, cardChallenge);
        return Arrays.equals(myCardCryptogram, cardCryptogram);
    }

    /**
     * Compute the host cryptogram to send to the card
     *
     * @param hostChallenge to use for the cryptogram
     * @param cardChallenge to use for the cryptogram
     * @return a valid host cryptogram
     */
    private byte[] computeHostCryptogram(byte[] hostChallenge, byte[] cardChallenge) {
        GPKey encKey = mSessionKeys.getKeyByType(GPKeyType.ENC);
        byte[] hostContext = ArrayUtil.concatenate(cardChallenge, hostChallenge);
        return GPCrypto.mac_3des_nulliv(encKey, hostContext);
    }

    /**
     * Check and select the SCP protocol based on an INIT UPDATE response
     *
     * @param init response given by the card
     * @throws CardException when communication is denied
     */
    private void checkAndSelectProtocol(InitUpdateResponse init) throws CardException {
        // determine and check SCP protocol
        int scpProto = init.scpProtocol;
        if (mUseProtocol != 0 && scpProto != mUseProtocol) {
            throw new CardException("Unexpected SCP version " + HexUtil.hex8(scpProto)
                    + ", expected " + HexUtil.hex8(mUseProtocol));
        }

        // determine and check SCP parameters
        int scpParams = init.scp03Parameters;
        if (init.scpProtocol == 3) {
            // SCP03 has the parameters in the INIT UPDATE response,
            // so check them for previous expectations and use them.
            if (mUseParameters != 0 && scpParams != mUseParameters) {
                throw new CardException("Unexpected SCP parameters " + HexUtil.hex8(scpParams)
                        + ", expected " + HexUtil.hex8(mUseProtocol));
            }
        } else {
            // check that we have been told the parameters
            if (mUseParameters == 0) {
                throw new CardException("SCP parameters not provided - required for SCP version" + HexUtil.hex8(scpProto));
            }
            // use the expected parameters
            scpParams = mUseParameters;
        }

        // check configuration against policy
        mPolicy.checkProtocol(scpProto, scpParams);

        // we now know the protocol to be used
        mSCP = SCP0102Parameters.decode(scpProto, scpParams);
    }

    /**
     * Assemble and transact an INITIALIZE UPDATE command
     *
     * @param keyVersion    to indicate
     * @param keyId         to indicate
     * @param hostChallenge to send
     * @return a decoded response to the command
     * @throws CardException on error
     */
    private InitUpdateResponse performInitializeUpdate(byte keyVersion, byte keyId, byte[] hostChallenge) throws CardException {
        // build the command
        CommandAPDU initCommand = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_INITIALIZE_UPDATE,
                keyVersion,
                keyId,
                hostChallenge
        );
        // and transmit it on the underlying channel
        ResponseAPDU initResponse = mCard.transmit(mChannel, initCommand);
        // check for errors
        checkResponse(initResponse);
        // parse the response
        byte[] responseData = initResponse.getData();
        InitUpdateResponse response = new InitUpdateResponse(responseData);
        // return the parsed response
        return response;
    }

    /**
     * Assemble and transact an EXTERNAL AUTHENTICATE command
     *
     * @param hostCryptogram to send
     * @throws CardException on error
     */
    private void performExternalAuthenticate(byte[] hostCryptogram) throws CardException {
        // determine session parameters
        byte authParam = 0;
        // always enable MAC
        authParam |= GP.EXTERNAL_AUTHENTICATE_P1_MAC;
        // ENC and RMAC are optional
        if (mUseENC)
            authParam |= GP.EXTERNAL_AUTHENTICATE_P1_ENC;
        if (mUseRMAC)
            authParam |= GP.EXTERNAL_AUTHENTICATE_P1_RMAC;
        if (mUseRENC)
            authParam |= GP.EXTERNAL_AUTHENTICATE_P1_RENC;
        // build the command
        CommandAPDU authCommand = APDUUtil.buildCommand(
                GP.CLA_MAC,
                GP.INS_EXTERNAL_AUTHENTICATE,
                authParam,
                (byte) 0,
                hostCryptogram
        );
        // send it over the secure channel
        ResponseAPDU authResponse = transmit(authCommand);
        // check for errors
        checkResponse(authResponse);
        // nothing to return
    }

    /**
     * Strictly check a response (and throw if it is an error)
     *
     * @param response to check
     * @throws CardException if the response is an error
     */
    private void checkResponse(ResponseAPDU response) throws CardException {
        int sw = response.getSW();
        if (sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error in secure channel authentication", sw);
        }
    }

    /**
     * Parsed response to an INIT UPDATE command
     * <p/>
     * This is somewhat magic because the structure differs
     * slightly between SCP versions, so total size can differ.
     */
    private class InitUpdateResponse {

        /**
         * Length of data for SCP01/02
         */
        public static final int SCP0102_LENGTH = 28;
        /**
         * Length of data for SCP03
         */
        public static final int SCP03_LENGTH = 32;

        /**
         * Key diversification data
         */
        public final byte[] diversificationData;
        /**
         * Key version selected by card
         */
        public final int keyVersion;
        /**
         * SCP version selected by card
         */
        public final int scpProtocol;
        /**
         * SCP parameters selected by card (SCP03 only)
         */
        public final int scp03Parameters;
        /**
         * Card challenge for authentication
         */
        public final byte[] cardChallenge;
        /**
         * Card cryptogram for authentication
         */
        public final byte[] cardCryptogram;
        /**
         * Session sequence number for authentication (SCP03 only, others use part of challenge)
         */
        public final byte[] scp03Sequence;

        /**
         * Parse an INIT UPDATE response
         *
         * @param data to be parsed
         */
        InitUpdateResponse(byte[] data) {
            int offset = 0;
            int length = data.length;

            // check for possible lengths
            if (length != SCP0102_LENGTH || length == SCP03_LENGTH) {
                throw new IllegalArgumentException("Invalid INIT UPDATE response length " + length);
            }

            // key diversification data
            diversificationData = Arrays.copyOfRange(data, offset, offset + 10);
            offset += diversificationData.length;

            // key version that the card uses
            keyVersion = data[offset++] & 0xFF;

            // SCP protocol version
            scpProtocol = data[offset++] & 0xFF;

            // SCP03 has protocol parameters here
            if (scpProtocol == 3) {
                scp03Parameters = data[offset++] & 0xFF;
            } else {
                scp03Parameters = 0;
            }

            // card challenge for authentication
            cardChallenge = Arrays.copyOfRange(data, offset, offset + 8);
            offset += cardChallenge.length;

            // card cryptogram for authentication
            cardCryptogram = Arrays.copyOfRange(data, offset, offset + 8);
            offset += cardCryptogram.length;

            // SCP03 has its sequence number here
            if (scpProtocol == 3) {
                scp03Sequence = Arrays.copyOfRange(data, offset, offset + 3);
                offset += scp03Sequence.length;
            } else {
                scp03Sequence = null;
            }

            // check that we have consumed everything
            if (offset != length) {
                throw new IllegalArgumentException("BUG: INIT UPDATE response length mismatch");
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("INIT UPDATE response:");
            sb.append("\n scpProtocol ");
            sb.append(HexUtil.hex8(scpProtocol));
            if (scpProtocol == 3) {
                sb.append("\n scp03Parameters ");
                sb.append(HexUtil.hex8(scp03Parameters));
            }
            sb.append("\n keyVersion ");
            sb.append(HexUtil.hex8(keyVersion));
            sb.append("\n cardChallenge ");
            sb.append(HexUtil.bytesToHex(cardChallenge));
            sb.append("\n cardCryptogram ");
            sb.append(HexUtil.bytesToHex(cardCryptogram));
            sb.append("\n diversificationData ");
            sb.append(HexUtil.bytesToHex(diversificationData));
            if (scpProtocol == 3) {
                sb.append("\n scp03Sequence ");
                sb.append(HexUtil.bytesToHex(scp03Sequence));
            }
            return sb.toString();
        }
    }

}
