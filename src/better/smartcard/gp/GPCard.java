package better.smartcard.gp;

import better.smartcard.gp.keys.GPKeySet;
import better.smartcard.gp.protocol.GP;
import better.smartcard.gp.protocol.GPCardData;
import better.smartcard.gp.protocol.GPKeyInfo;
import better.smartcard.gp.protocol.GPLifeCycle;
import better.smartcard.gp.scp.GPSecureChannel;
import better.smartcard.gp.scp.SCPProtocol;
import better.smartcard.gp.scp.SCPProtocolPolicy;
import better.smartcard.gp.scp.SCPSecurityPolicy;
import better.smartcard.protocol.ISO7816;
import better.smartcard.protocol.SWException;
import better.smartcard.util.AID;
import better.smartcard.util.APDUUtil;
import better.smartcard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;
import java.io.ByteArrayOutputStream;

public class GPCard {

    private static final Logger LOG = LoggerFactory.getLogger(GPCard.class);

    /** AID for ISD specified in GlobalPlatform */
    static final AID AID_GP = new AID("A000000003000000");
    /** AID for ISD used by NXP cards */
    static final AID AID_NXP = new AID("A000000151000000");
    /** AID for ISD used by Gemalto cards */
    static final AID AID_GEMALTO = new AID("A000000018434D00");

    /** List of AIDs used to probe for the ISD of the card */
    static final AID[] PROBE_AIDS = {
            AID_GP, AID_NXP, AID_GEMALTO
    };

    /**
     * Library context used for this card
     *
     * Not currently used but should be the source of keys.
     */
    GPContext mContext;
    /**
     * Terminal handle
     */
    CardTerminal mTerminal;
    /**
     * Card handle
     */
    Card mCard;
    /**
     * Keys to use for secure channel
     */
    GPKeySet mKeys;
    /**
     * Protocol policy for the secure channel
     */
    SCPProtocolPolicy mProtocolPolicy;
    /**
     * Security policy for the secure channel
     */
    SCPSecurityPolicy mSecurityPolicy;
    /**
     * Plain basic channel
     */
    CardChannel mBasic;
    /**
     * Our SCP secure channel
     *
     * Initialized when secure session starts.
     */
    GPSecureChannel mSecure;
    /**
     * AID of the ISD
     *
     * Provided by used or detected.
     */
    AID mISD;
    /**
     * Card life cycle data
     *
     * Optional. Retrieved during connection process.
     *
     * This contains information such as the serial number of the card
     * as well as ISO-specified identifiers for the manufacturers
     * involved in making the card.
     */
    GPLifeCycle mCardLifeCycle;
    /**
     * Card data
     *
     * Retrieved during connection process.
     *
     * Used to determine the security protocol to use
     * when detection is permitted. Otherwise will be
     * used to verify the protocol.
     */
    GPCardData mCardData;
    /**
     * Card issuer identification number
     *
     * Optional. Retrieved during connection process
     * if the card indicates in the card data that
     * unique identification is supported.
     */
    byte[] mCardIIN;
    /**
     * Card image number
     *
     * Optional. Retrieved during connection process
     * if the card indicates in the card data that
     * unique identification is supported.
     */
    byte[] mCardCIN;
    /**
     * Card key info
     *
     * Retrieved during connection process and used
     * to set up the secure session.
     */
    GPKeyInfo mCardKeyInfo;
    /**
     * Registry instance
     *
     * Cached representation of ISD-managed on-card
     * objects such as installed applets and security
     * domains.
     */
    GPRegistry mRegistry;
    /**
     * Issuer domain access object
     *
     * This is the interface to be used for communicating
     * with the ISD to perform the various operations
     * that it enables.
     */
    GPIssuerDomain mIssuerDomain;

    /**
     * True when we are connected to an ISD
     *
     * Being connected is defined as having a connection
     * to a card with the ISD being selected on some channel,
     * ready for performing operations on it.
     */
    boolean mIsConnected;

    public GPCard(GPContext context, CardTerminal terminal) {
        mContext = context;
        mTerminal = terminal;
        mProtocolPolicy = SCPProtocolPolicy.PERMISSIVE;
        mKeys = GPKeySet.GLOBALPLATFORM;
        mRegistry = new GPRegistry(this);
        mIssuerDomain = new GPIssuerDomain(this);
    }

    public CardTerminal getTerminal() {
        return mTerminal;
    }

    public Card getCard() {
        return mCard;
    }

    public AID getCardISD() {
        return mISD;
    }

    public void setCardISD(AID isd) {
        mISD = isd;
    }

    public byte[] getCardIIN() {
        return mCardIIN;
    }

    public byte[] getCardCIN() {
        return mCardCIN;
    }

    public String getCardIdentifier() {
        if(mCardLifeCycle == null) {
            return null;
        } else {
            return mCardLifeCycle.getCardIdentifier();
        }
    }

    public GPLifeCycle getCardLifeCycle() {
        return mCardLifeCycle;
    }

    public GPCardData getCardData() {
        return mCardData;
    }

    public GPKeyInfo getCardKeyInfo() {
        return mCardKeyInfo;
    }

    public GPSecureChannel getSecureChannel() {
        return mSecure;
    }

    public GPRegistry getRegistry() {
        return mRegistry;
    }

    public GPIssuerDomain getIssuerDomain() {
        return mIssuerDomain;
    }

    public SCPProtocolPolicy getPolicy() {
        return mProtocolPolicy;
    }

    public SCPProtocol getProtocol() {
        if (mSecure == null || !mSecure.isEstablished()) {
            return null;
        }
        return mSecure.getActiveProtocol();
    }

    public void setProtocolPolicy(SCPProtocolPolicy policy) {
        mProtocolPolicy = policy;
    }

    public void setSecurityPolicy(SCPSecurityPolicy policy) {
        mSecurityPolicy = policy;
    }

    /**
     * Determine the ISD of the card
     * <p/>
     * This will attempt to determine the ISD of the card
     * by trying several well-known AIDs.
     *
     * @return true if we think we have an ISD
     * @throws CardException
     */
    public boolean detect() throws CardException {
        // connect to the card
        mCard = mTerminal.connect("*");

        // log connection parameters
        LOG.debug("connected " + mCard.getProtocol() + " ATR="
                + HexUtil.bytesToHex(mCard.getATR().getBytes()));

        // work exclusively
        try {
            mCard.beginExclusive();

            // get the basic channel
            mBasic = mCard.getBasicChannel();

            // find the issuer applet
            if (mISD == null) {
                mISD = findISD();
            }

            // log about it
            if (mISD != null) {
                LOG.debug("found ISD at " + mISD);
            } else {
                LOG.debug("could not find an ISD");
            }

        } finally {
            mCard.endExclusive();
        }

        return mISD != null;
    }

    /**
     * Connect (and authenticate) to the card
     *
     * @return
     * @throws CardException
     */
    public boolean connect() throws CardException {
        // check already connected
        if (mIsConnected) {
            LOG.info("already connected");
            return true;
        }

        // determine and check ISD
        if (!detect()) {
            throw new CardException("Could not determine ISD");
        }

        // we want to do this exclusively
        try {
            mCard.beginExclusive();

            // select the ISD
            selectFileByName(mISD);

            // XXX
            LOG.info("static keys:\n" + mKeys.toString());

            // get the CPLC for reference
            try {
                mCardLifeCycle = readCPLC();
            } catch (SWException ex) {
                LOG.warn("error trying to read CPLC", ex);
            }
            if (mCardLifeCycle == null) {
                LOG.warn("card did not return CPLC");
            } else {
                LOG.debug("card production info:\n" + mCardLifeCycle.toString());
            }

            // get card data, needed to determine SCP parameters
            try {
                mCardData = readCardData();
            } catch (SWException ex) {
                LOG.warn("error trying to read card data", ex);
            }
            if (mCardData == null) {
                LOG.warn("card did not return GP card data");
            } else {
                LOG.debug("card data:\n" + mCardData.toString());
            }

            // get IIN and CIN if uniquely identifiable
            if(mCardData != null && mCardData.isUniquelyIdentifiable()) {
                mCardIIN = readCardIIN();
                if (mCardIIN == null) {
                    LOG.info("card has no IIN");
                } else {
                    LOG.debug("card IIN: " + HexUtil.bytesToHex(mCardIIN));
                }
                mCardCIN = readCardCIN();
                if (mCardCIN == null) {
                    LOG.info("card has no CIN");
                } else {
                    LOG.debug("card CIN: " + HexUtil.bytesToHex(mCardCIN));
                }
            }

            // get key information and check it against keys
            mCardKeyInfo = readKeyInfo();
            if (mCardKeyInfo == null) {
                throw new CardException("Card did not return a GP key information template");
            } else {
                LOG.debug("key information:\n" + mCardKeyInfo.toString());
                if (mCardKeyInfo.matchesKeysetForUsage(mKeys)) {
                    LOG.debug("keys match key information");
                } else {
                    throw new CardException("Keys do not match key information from card");
                }
            }

            // create a secure channel object
            mSecure = new GPSecureChannel(this, mBasic, mKeys, mProtocolPolicy, mSecurityPolicy);

            // set up policy of the secure channel
            if (mCardData != null) {
                mSecure.expectProtocol(
                        mCardData.getSecurityProtocol(),
                        mCardData.getSecurityParameters());
            }

            // try to open the secure channel
            mSecure.open();

            // fetch registry information
            mRegistry.update();

            // mark as connected
            mIsConnected = true;
        } finally {
            if (!mIsConnected) {
                mCard.endExclusive();
            }
        }

        return mIsConnected;
    }

    public void disconnect() throws CardException {
        // tear down the secure channel
        if (mSecure != null) {
            mSecure.close();
            mSecure = null;
        }
        // end exclusive if connected
        if (mIsConnected) {
            mCard.endExclusive();
        }
        // disconnect and reset the card
        if (mCard != null) {
            mCard.disconnect(true);
            mCard = null;
        }
        // mark as disconnected
        mIsConnected = false;
        // log about it
        LOG.debug("disconnected");
    }

    private AID findISD() throws CardException {
        AID isd = mISD;
        if(isd == null) {
            for (AID name : PROBE_AIDS) {
                try {
                    selectFileByName(name);
                    isd = name;
                    break;
                } catch (SWException e) {
                    switch (e.getCode()) {
                        case ISO7816.SW_FILE_NOT_FOUND:
                        case ISO7816.SW_REFERENCED_DATA_NOT_FOUND:
                            continue;
                        default:
                            throw e;
                    }
                }
            }
        }
        return isd;
    }

    private byte[] selectFileByName(AID name) throws CardException {
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_ISO,
                GP.INS_SELECT,
                GP.SELECT_P1_BY_NAME,
                GP.SELECT_P2_FIRST,
                name.getBytes()
        );
        return transactPlainAndCheck(command).getData();
    }

    private byte[] readData(short p1p2) throws CardException {
        LOG.trace("readData(" + HexUtil.hex16(p1p2) + ")");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_GET_DATA,
                p1p2
        );
        byte[] response = null;
        try {
            response = transactPlainAndCheck(command).getData();
        } catch (SWException e) {
            switch (e.getCode()) {
                case ISO7816.SW_FILE_NOT_FOUND:
                case ISO7816.SW_REFERENCED_DATA_NOT_FOUND:
                    break;
                default:
                    throw e;
            }
        }
        return response;
    }

    private GPLifeCycle readCPLC() throws CardException {
        LOG.debug("readCPLC()");
        GPLifeCycle res = new GPLifeCycle();
        byte[] data = readData(GP.GET_DATA_P12_CPLC);
        res.read(data);
        return res;
    }

    private GPCardData readCardData() throws CardException {
        LOG.debug("readCardData()");
        GPCardData res = null;
        byte[] data = readData(GP.GET_DATA_P12_CARD_DATA);
        if (data != null) {
            res = new GPCardData();
            res.read(data);
        }
        return res;
    }

    private byte[] readCardIIN() throws CardException {
        LOG.debug("readCardIIN()");
        return readData(GP.GET_DATA_P12_ISSUER_ID_NUMBER);
    }

    private byte[] readCardCIN() throws CardException {
        LOG.debug("readCardCIN()");
        return readData(GP.GET_DATA_P12_CARD_IMG_NUMBER);
    }

    private byte[] readApplicationInfo() throws CardException {
        LOG.debug("readApplicationInfo()");
        return readData(GP.GET_DATA_P12_APPLICATION_INFO);
    }

    private GPKeyInfo readKeyInfo() throws CardException {
        LOG.debug("readKeyInfo()");
        GPKeyInfo res = new GPKeyInfo();
        byte[] data = readData(GP.GET_DATA_P12_KEY_INFO_TEMPLATE);
        res.read(data);
        return res;
    }

    byte[] readStatusISD() throws CardException {
        return readStatus(GP.GET_STATUS_P1_ISD_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    byte[] readStatusAppsAndSD() throws CardException {
        return readStatus(GP.GET_STATUS_P1_APP_AND_SD_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    byte[] readStatusELF() throws CardException {
        return readStatus(GP.GET_STATUS_P1_ELF_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    byte[] readStatusEXMandELF() throws CardException {
        return readStatus(GP.GET_STATUS_P1_EXM_AND_ELF_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    /**
     *
     * @param p1Subset
     * @param p2Format
     * @return
     * @throws CardException
     */
    private byte[] readStatus(byte p1Subset, byte p2Format) throws CardException {
        byte[] criteria = {0x4F, 0x00};
        return readStatus(p1Subset, p2Format, criteria);
    }

    /**
     * Perform a GlobalPlatform SET STATUS operations
     *
     * @param p1Subset
     * @param p2Format
     * @param criteria
     * @return
     * @throws CardException
     */
    private byte[] readStatus(byte p1Subset, byte p2Format, byte[] criteria) throws CardException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        boolean first = true;
        do {
            // determine first/next parameter
            byte getParam = GP.GET_STATUS_P2_GET_NEXT;
            if (first) {
                getParam = GP.GET_STATUS_P2_GET_FIRST_OR_ALL;
            }
            first = false;
            // build the command
            CommandAPDU command = APDUUtil.buildCommand(
                    GP.CLA_GP,
                    GP.INS_GET_STATUS,
                    p1Subset, (byte) (getParam | p2Format), criteria);
            // run the command
            ResponseAPDU response = transactSecure(command);
            // get SW and data
            int sw = response.getSW();
            byte[] data = response.getData();
            // append data, no matter the SW
            if (data != null && data.length > 0) {
                bos.write(data, 0, data.length);
            }
            // continue if SW says that we should
            //   XXX extract this constant
            if (sw == 0x6310) {
                continue;
            }
            // check for various cases of "empty"
            //   XXX rethink this
            if (sw == ISO7816.SW_NO_ERROR
                    || sw == ISO7816.SW_FILE_NOT_FOUND
                    || sw == ISO7816.SW_REFERENCED_DATA_NOT_FOUND) {
                break;
            } else {
                throw new SWException("Error in GET STATUS", sw);
            }
        } while (true);
        return bos.toByteArray();
    }

    /**
     * Perform an APDU exchange using a secure channel and check the result for errors
     *
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    public ResponseAPDU transactSecureAndCheck(CommandAPDU command) throws CardException {
        ResponseAPDU response = transactSecure(command);
        checkResponse(response);
        return response;
    }

    /**
     * Perform an APDU exchange using a secure channel
     *
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    public ResponseAPDU transactSecure(CommandAPDU command) throws CardException {
        if (mSecure == null || !mSecure.isEstablished()) {
            throw new CardException("Secure channel not available");
        }
        return mSecure.transmit(command);
    }

    /**
     * Perform an APDU exchange WITH OPTIONAL SECURITY and check the result for errors
     *
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    public ResponseAPDU transactPlainAndCheck(CommandAPDU command) throws CardException {
        ResponseAPDU response = transactPlain(command);
        checkResponse(response);
        return response;
    }

    /**
     * Perform an APDU exchange WITH OPTIONAL SECURITY
     *
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    public ResponseAPDU transactPlain(CommandAPDU command) throws CardException {
        if (mSecure != null && mSecure.isEstablished()) {
            return mSecure.transmit(command);
        } else {
            return transmit(mBasic, command);
        }
    }

    /**
     * Exchange APDUs on the given channel
     *
     * @param channel to use
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    public ResponseAPDU transmit(CardChannel channel, CommandAPDU command) throws CardException {
        LOG.trace("apdu > " + APDUUtil.toString(command));
        ResponseAPDU response = channel.transmit(command);
        LOG.trace("apdu < " + APDUUtil.toString(response));
        return response;
    }

    /**
     * Check the given R-APDU for error codes
     *
     * GlobalPlatform is pleasantly simplistic in its error behaviour.
     *
     * Everything except for 0x9000 is an error.
     *
     * @param response to check
     * @throws CardException when the response is an error
     */
    private void checkResponse(ResponseAPDU response) throws CardException {
        int sw = response.getSW();
        if (sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error in transaction", sw);
        }
    }

}