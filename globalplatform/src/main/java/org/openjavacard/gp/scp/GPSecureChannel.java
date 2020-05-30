/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.openjavacard.gp.scp;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.gp.crypto.GPBouncy;
import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyDiversification;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.gp.protocol.GP;
import org.openjavacard.gp.structure.GPInitUpdateResponse;
import org.openjavacard.gp.wrapper.GPBasicWrapper;
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SW;
import org.openjavacard.iso.SWException;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Card channel wrapper for GP secure messaging
 */
public class GPSecureChannel extends CardChannel {

    private static final Logger LOG = LoggerFactory.getLogger(GPSecureChannel.class);

    /** Random for generating the host challenge */
    private final SecureRandom mRandom;
    /** Context for checks */
    private final GPContext mContext;
    /** Reference to the card for communication */
    private final GPCard mCard;
    /** Underlying channel wrapper for communication */
    private final GPBasicWrapper mBasicWrapper;
    /** Underlying card channel */
    private final CardChannel mChannel;
    /** Initial static keys */
    private final GPKeySet mStaticKeys;
    /** Key diversification to apply */
    private final GPKeyDiversification mDiversification;
    /** Protocol policy in effect */
    private final SCPProtocolPolicy mProtocolPolicy;
    /** Security policy in effect */
    private final SCPSecurityPolicy mSecurityPolicy;

    /** Expected SCP protocol - 0 means ANY */
    private int mExpectedProtocol = 0;
    /** Expected SCP parameters - 0 means ANY */
    private int mExpectedParameters = 0;
    /** SCP protocol and parameters in use */
    private SCPParameters mActiveProtocol;
    /** Derived session keys */
    private GPKeySet mSessionKeys;
    /** Helper performing wrapping/unwrapping of APDUs */
    private SCPWrapper mWrapper;
    /** True when authentication has succeeded and is unbroken */
    private boolean mIsEstablished;

    /**
     * Construct a new secure channel
     * <p/>
     * Objects are not intended to be reconfigured.
     * <p/>
     * @param card this channel is for
     * @param basicWrapper to communicate through
     * @param keys to use
     * @param protocolPolicy to conform to
     * @param securityPolicy to conform to
     */
    public GPSecureChannel(GPCard card, GPBasicWrapper basicWrapper,
                           GPKeySet keys, GPKeyDiversification diversification,
                           SCPProtocolPolicy protocolPolicy, SCPSecurityPolicy securityPolicy) {
        mRandom = new SecureRandom();
        mContext = card.getContext();
        mCard = card;
        mBasicWrapper = basicWrapper;
        mChannel = mBasicWrapper.getChannel();
        mStaticKeys = keys;
        mDiversification = diversification;
        mProtocolPolicy = protocolPolicy;
        mSecurityPolicy = securityPolicy;
        reset();
    }

    /** @return the card this channel is for */
    @Override
    public Card getCard() {
        return mChannel.getCard();
    }

    /** @return the channel number of this channel */
    @Override
    public int getChannelNumber() {
        return mChannel.getChannelNumber();
    }

    /** @return the active SCP protocol on this channel */
    public SCPParameters getActiveProtocol() {
        return mActiveProtocol;
    }

    /** @return true if the channel is established */
    public boolean isEstablished() {
        return mIsEstablished;
    }

    /**
     * Inform the secure channel about the protocol to be used
     * <p/>
     * This call exists because protocol determination may happen late.
     * <p/>
     * The channel will check the protocol against its policy.
     * <p/>
     * @param protocol to be used
     * @param parameters to be used
     * @throws CardException when the protocol fails policy check
     */
    public void expectProtocol(int protocol, int parameters) throws CardException {
        // check the protocol against our policy
        mProtocolPolicy.checkProtocol(protocol, parameters);
        // okay, remember it
        mExpectedProtocol = protocol;
        mExpectedParameters = parameters;
    }

    /**
     * Encrypt sensitive data for transmission
     * <p/>
     * This is used to encrypt keys with an additional layer of encryption.
     * <p/>
     * The keys for this are session-specific, so the channel handles this.
     * <p/>
     * @param data to be encrypted
     * @return encrypted data
     * @throws CardException on card-related errors
     */
    public byte[] encryptSensitiveData(byte[] data) throws CardException {
        return mWrapper.encryptSensitiveData(data);
    }

    /**
     * Transmit an APDU through the secure channel
     * <p/>
     * This will wrap the command, send it to the card,
     * wait for a response, unwrap the response and return it.
     * <p/>
     * @param command to be wrapped and sent
     * @return the unwrapped response
     * @throws CardException on card-related errors
     */
    @Override
    public ResponseAPDU transmit(CommandAPDU command) throws CardException {
        if(!mIsEstablished) {
            throw new CardException("Secure channel is not established");
        }
        return transmitInternal(command);
    }

    /**
     * Convenience wrapper for sending byte buffers
     * <p/>
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

    /**
     * Close the secure channel
     * <p/>
     * @throws CardException
     */
    @Override
    public void close() throws CardException {
        LOG.debug("closing channel");
        reset();
    }

    /**
     * Reset the secure channel
     * <p/>
     * Clears all state relevant to an established connection.
     * <p/>
     */
    private void reset() {
        mIsEstablished = false;
        mWrapper = null;
        mSessionKeys = null;
        mActiveProtocol = null;
    }

    /**
     * Internal transmit method
     * <p/>
     * This variant does not check if the channel is fully established.
     * This is used during secure channel setup.
     * <p/>
     * @param command be wrapped and sent
     * @return the unwrapped response
     * @throws CardException
     */
    private ResponseAPDU transmitInternal(CommandAPDU command) throws CardException {
        // bug out if the channel is not open
        if (mWrapper == null) {
            throw new CardException("Secure channel is not connected");
        }
        // wrap the command (sign, encrypt)
        CommandAPDU wrappedCommand = mWrapper.wrap(command);
        // send the wrapped command
        ResponseAPDU wrappedResponse = mBasicWrapper.transmitRaw(wrappedCommand);
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
        // return unwrapped response
        return response;
    }

    /**
     * Open the secure channel
     * <p/>
     * This will exchange challenges with the card.
     * <p/>
     * @throws CardException
     */
    public void open() throws CardException {
        LOG.debug("opening secure channel");

        // generate the host challenge
        byte[] hostChallenge = generateChallenge();

        // determine key parameters
        byte keyVersion = (byte) mStaticKeys.getKeyVersion();
        byte keyId = 0;
        if (mExpectedProtocol == 1) {
            throw new UnsupportedOperationException("SCP01 requires key id in INITIALIZE UPDATE -- which key?");
        }
        LOG.debug("key id " + keyId + " version " + keyVersion);

        // perform INITIALIZE UPDATE, exchanging challenges
        GPInitUpdateResponse init = performInitializeUpdate(keyVersion, keyId, hostChallenge);

        // check and select the protocol to be used
        checkAndSelectProtocol(init);
        LOG.debug("protocol " + mActiveProtocol);

        // derive session keys (including diversification)
        mSessionKeys = deriveKeys(init, hostChallenge);

        // log session keys
        if(mContext.isKeyLoggingEnabled()) {
            LOG.trace("session keys:\n" + mSessionKeys.toString());
        }

        // verify the card cryptogram
        if (verifyCardCryptogram(hostChallenge, init.cardChallenge, init.cardCryptogram)) {
            LOG.debug("card cryptogram verified");
        } else {
            throw new CardException("Invalid card cryptogram");
        }

        // now generate the host cryptogram
        byte[] hostCryptogram = computeHostCryptogram(hostChallenge, init.cardChallenge);

        // create CardAPDU wrapper
        mWrapper = buildWrapper();

        // perform EXTERNAL AUTHENTICATE to authenticate to card
        LOG.debug("performing authentication");
        performExternalAuthenticate(hostCryptogram);
        LOG.debug("authentication succeeded");

        // can now start ENC, RMAC and RENC - if applicable
        if (mSecurityPolicy.requireCENC) {
            LOG.debug("starting command encryption");
            mWrapper.startENC();
        }
        if (mSecurityPolicy.requireRMAC) {
            LOG.debug("starting response authentication");
            mWrapper.startRMAC();
        }
        if(mSecurityPolicy.requireRENC) {
            LOG.debug("starting response encryption");
            mWrapper.startRENC();
        }

        // the channel is now established
        mIsEstablished = true;
    }

    /**
     * Generate a challenge for use in authentication.
     * <p/>
     * In this version the challenge always is 8 bytes long.
     * <p/>
     * @return an 8-byte random challenge
     */
    private byte[] generateChallenge() {
        byte[] result = new byte[8];
        mRandom.nextBytes(result);
        return result;
    }

    /**
     * Compute the card cryptogram for verification
     * <p/>
     * @param hostChallenge generated by the host
     * @param cardChallenge sent by the card
     * @return the card cryptogram
     */
    private byte[] computeCardCryptogram(byte[] hostChallenge, byte[] cardChallenge) {
        byte[] cardContext = ArrayUtil.concatenate(hostChallenge, cardChallenge);
        switch(mActiveProtocol.scpVersion) {
            case 0:
                return new byte[8];
            case 2:
                LOG.trace("computing card cryptogram for SCP02");
                GPKey encKey = mSessionKeys.getKeyByUsage(GPKeyUsage.ENC);
                return GPCrypto.mac_3des_nulliv(encKey, cardContext);
            case 3:
                LOG.trace("computing card cryptogram for SCP03");
                GPKey macKey = mSessionKeys.getKeyByUsage(GPKeyUsage.MAC);
                return GPBouncy.scp03_kdf(macKey, (byte)0x00, cardContext, 64);
            default:
                throw new RuntimeException("Unsupported SCP version " + mActiveProtocol);
        }
    }

    /**
     * Verify the card cryptogram
     * <p/>
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
     * <p/>
     * @param hostChallenge to use for the cryptogram
     * @param cardChallenge to use for the cryptogram
     * @return a valid host cryptogram
     */
    private byte[] computeHostCryptogram(byte[] hostChallenge, byte[] cardChallenge) {
        switch(mActiveProtocol.scpVersion) {
            case 0:
                return new byte[8];
            case 2:
                byte[] hostContext02 = ArrayUtil.concatenate(cardChallenge, hostChallenge);
                GPKey encKey = mSessionKeys.getKeyByUsage(GPKeyUsage.ENC);
                return GPCrypto.mac_3des_nulliv(encKey, hostContext02);
            case 3:
                byte[] hostContext03 = ArrayUtil.concatenate(hostChallenge, cardChallenge);
                GPKey macKey = mSessionKeys.getKeyByUsage(GPKeyUsage.MAC);
                return GPBouncy.scp03_kdf(macKey, (byte)0x01, hostContext03, 64);
            default:
                throw new RuntimeException("Unsupported SCP version " + mActiveProtocol);
        }
    }

    /**
     * Check and select the SCP protocol based on an INIT UPDATE response
     * <p/>
     * @param init response given by the card
     * @throws CardException when communication is denied
     */
    private void checkAndSelectProtocol(GPInitUpdateResponse init) throws CardException {
        // determine and check SCP protocol
        int scpProto = init.scpProtocol;
        if (mExpectedProtocol != 0 && scpProto != mExpectedProtocol) {
            throw new CardException("Unexpected SCP version " + HexUtil.hex8(scpProto)
                    + ", expected " + HexUtil.hex8(mExpectedProtocol));
        }

        // determine and check SCP parameters
        int scpParams = init.scp03Parameters;
        if (init.scpProtocol == 3) {
            // SCP03 has the parameters in the INIT UPDATE response
        } else {
            // check that we have been told the parameters
            if (mExpectedParameters == 0) {
                throw new CardException("SCP parameters not provided - required for SCP" + HexUtil.hex8(scpProto));
            }
            // use the expected parameters
            scpParams = mExpectedParameters;
        }

        // we now know the protocol to be used
        SCPParameters selected = SCPParameters.decode(scpProto, scpParams);

        // check against the protocol policy
        mProtocolPolicy.checkProtocol(selected);

        // check against security policy
        mSecurityPolicy.checkProtocol(selected);

        // decision to use the protocol
        mActiveProtocol = selected;
    }

    /**
     * Derive keys for an SCP session
     * <p/>
     * This method will first check for any indications of the
     * keys being the wrong keys, such as by key version mismatch.
     * <p/>
     * It will then diversify the key if specified and perform
     * the protocol-specific key derivation, producing a set of session keys.
     * <p/>
     * @param init response to INIT UPDATE
     * @param hostChallenge previously generated
     * @return a set of session keys
     * @throws CardException on error
     */
    private GPKeySet deriveKeys(GPInitUpdateResponse init, byte[] hostChallenge) throws CardException {
        // derivation might replace keys
        GPKeySet keys = mStaticKeys;

        // check key version
        int selectedKeyVersion = keys.getKeyVersion();
        if (selectedKeyVersion > 0 && selectedKeyVersion != init.keyVersion) {
            throw new CardException("Key version mismatch: host " + selectedKeyVersion + " card " + init.keyVersion);
        }

        // diversify keys
        if(mDiversification != GPKeyDiversification.NONE) {
            LOG.trace("diversification " + mDiversification + " data " + HexUtil.bytesToHex(init.diversificationData));
            keys = SCPDiversification.diversify(keys, mDiversification, init.diversificationData);
            if(mContext.isKeyLoggingEnabled()) {
                LOG.trace("diversified keys:\n" + keys.toString());
            }
        }

        // derive session keys
        switch (mActiveProtocol.scpVersion) {
            case 0:
            case 1:
                break;
            case 2:
                byte[] seq02 = Arrays.copyOfRange(init.cardChallenge, 0, 2);
                LOG.debug("card sequence " + HexUtil.bytesToHex(seq02));
                keys = SCP02Derivation.deriveSessionKeys((SCP0102Parameters)mActiveProtocol, keys, seq02);
                break;
            case 3:
                byte[] seq03 = init.scp03Sequence;
                LOG.debug("card sequence " + HexUtil.bytesToHex(seq03));
                keys = SCP03Derivation.deriveSessionKeys(keys, seq03, hostChallenge, init.cardChallenge);
                break;
            default:
                throw new CardException("Unsupported SCP version " + mActiveProtocol);
        }

        return keys;
    }

    /**
     * Build an APDU wrapper
     * <p/>
     * This will construct an APDU wrapper for the active
     * protocol using the current session keys.
     * <p/>
     * @return the wrapper
     * @throws CardException on error
     */
    private SCPWrapper buildWrapper() throws CardException {
        switch (mActiveProtocol.scpVersion) {
            case 0:
                return new SCP00Wrapper(mSessionKeys, ((SCP00Parameters) mActiveProtocol));
            case 1:
            case 2:
                return new SCP0102Wrapper(mSessionKeys, ((SCP0102Parameters) mActiveProtocol));
            case 3:
                return new SCP03Wrapper(mSessionKeys, ((SCP03Parameters) mActiveProtocol));
            default:
                throw new CardException("Unsupported SCP version " + mActiveProtocol);
        }
    }

    /**
     * Assemble and transact an INITIALIZE UPDATE command
     * <p/>
     * The command will be sent on the underlying unencrypted channel.
     * <p/>
     * @param keyVersion    to indicate
     * @param keyId         to indicate
     * @param hostChallenge to send
     * @return a decoded response to the command
     * @throws CardException on error
     */
    private GPInitUpdateResponse performInitializeUpdate(byte keyVersion, byte keyId, byte[] hostChallenge) throws CardException {
        LOG.trace("performInitializeUpdate()");
        // build the command
        CommandAPDU initCommand = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_INITIALIZE_UPDATE,
                keyVersion,
                keyId,
                hostChallenge
        );
        // and transmit it on the underlying channel
        ResponseAPDU initResponse = mBasicWrapper.transmitRaw(initCommand);
        // check the response
        checkResponse(initResponse);
        // parse the response
        byte[] responseData = initResponse.getData();
        GPInitUpdateResponse response = new GPInitUpdateResponse(responseData);
        // return the parsed response
        return response;
    }

    /**
     * Assemble and transact an EXTERNAL AUTHENTICATE command
     * <p/>
     * The command will be sent on the encrypted secure channel.
     * <p/>
     * @param hostCryptogram to send
     * @throws CardException on error
     */
    private void performExternalAuthenticate(byte[] hostCryptogram) throws CardException {
        LOG.trace("performExternalAuthenticate()");
        // determine session parameters
        byte authParam = 0;
        // even CMAC can be optional for us
        if (mSecurityPolicy.requireCMAC)
            authParam |= GP.EXTERNAL_AUTHENTICATE_P1_MAC;
        // ENC, RMAC and RENC are optional
        if (mSecurityPolicy.requireCENC)
            authParam |= GP.EXTERNAL_AUTHENTICATE_P1_ENC;
        if (mSecurityPolicy.requireRMAC)
            authParam |= GP.EXTERNAL_AUTHENTICATE_P1_RMAC;
        if (mSecurityPolicy.requireRENC)
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
        ResponseAPDU authResponse = transmitInternal(authCommand);
        // check for errors
        checkResponse(authResponse);
        // nothing to return
    }

    /**
     * Strictly check a response (and throw if it is an error)
     * <p/>
     * @param response to check
     * @throws CardException if the response is an error
     */
    private void checkResponse(ResponseAPDU response) throws CardException {
        int sw = response.getSW();
        if (sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error in secure channel authentication", sw);
        }
    }

}
