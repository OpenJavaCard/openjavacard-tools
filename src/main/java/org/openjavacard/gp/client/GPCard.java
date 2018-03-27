/*
 * openjavacard-tools: OpenJavaCard Development Tools
 * Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de
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
 *
 */

package org.openjavacard.gp.client;

import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.protocol.GP;
import org.openjavacard.gp.protocol.GPCardData;
import org.openjavacard.gp.protocol.GPKeyInfoTemplate;
import org.openjavacard.gp.protocol.GPLifeCycle;
import org.openjavacard.gp.scp.GPSecureChannel;
import org.openjavacard.gp.scp.SCPProtocol;
import org.openjavacard.gp.scp.SCPProtocolPolicy;
import org.openjavacard.gp.scp.SCPSecurityPolicy;
import org.openjavacard.iso.AID;
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SWException;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service object for a GlobalPlatform card
 * <p/>
 * This is responsible for identifying cards and their ISD
 * and generally ties the other modules together.
 * <p/>
 * All card communication by the library goes through here.
 * <p/>
 * Information that an ISD provides without authentication
 * can be gotten to via this class. Other information can
 * be obtained through the GPIssuerDomain and GPRegistry
 * objects.
 */
public class GPCard {

    private static final Logger LOG = LoggerFactory.getLogger(GPCard.class);

    /** AID for ISD specified in GlobalPlatform */
    private static final AID AID_GP = new AID("A000000003000000");
    /** AID for ISD used by NXP cards */
    private static final AID AID_NXP = new AID("A000000151000000");
    /** AID for ISD used by Gemalto cards */
    private static final AID AID_GEMALTO = new AID("A000000018434D00");

    /** List of AIDs used to probe for the ISD of the card */
    private static final AID[] PROBE_AIDS = {
            AID_GP, AID_NXP, AID_GEMALTO
    };

    /** Library context in use */
    private final GPContext mContext;
    /**
     * SmartcardIO terminal handle */
    private final CardTerminal mTerminal;
    /**
     * SmartcardIO card handle */
    private Card mCard;
    /**
     * SmartcardIO basic channel */
    private CardChannel mBasic;
    /**
     * AID of the ISD
     * <p/>
     * Provided by user or detected automatically by probing.
     */
    private AID mISD;
    /**
     * Keys to use for secure channel
     */
    private GPKeySet mKeys;
    /**
     * Protocol policy for secure channel
     */
    private SCPProtocolPolicy mProtocolPolicy;
    /**
     * Security policy for secure channel
     */
    private SCPSecurityPolicy mSecurityPolicy;
    /**
     * Card issuer identification number
     * <p/>
     * Optional. Retrieved during connection process.
     * <p/>
     * May be present when "unique identification" is supported.
     */
    private byte[] mCardIIN;
    /**
     * Card image number
     * <p/>
     * Optional. Retrieved during connection process.
     * <p/>
     * May be present when "unique identification" is supported.
     */
    private byte[] mCardCIN;
    /**
     * Card life cycle data
     * <p/>
     * Optional. Retrieved during connection process.
     * <p/>
     * This contains information such as the serial number of the card
     * as well as ISO-specified identifiers for the manufacturers
     * involved in making the card.
     */
    private GPLifeCycle mCardLifeCycle;
    /**
     * Card data
     * <p/>
     * Required. Retrieved during connection process.
     * <p/>
     * Used to determine the security protocol.
     */
    private GPCardData mCardData;
    /**
     * Card key info
     * <p/>
     * Required. Retrieved during connection process.
     * <p/>
     * This indicates what keys the ISD wants to authenticate with.
     */
    private GPKeyInfoTemplate mCardKeyInfo;
    /**
     * Our SCP secure channel
     * <p/>
     * Created when secure session starts.
     */
    private GPSecureChannel mSecure;
    /**
     * Registry instance
     * <p/>
     * Cached representation of ISD-managed on-card
     * objects such as installed applets and security
     * domains.
     */
    private final GPRegistry mRegistry;
    /**
     * Issuer domain access object
     * <p/>
     * This is the interface to be used for communicating
     * with the ISD to perform the various operations
     * that it enables.
     */
    private final GPIssuerDomain mIssuerDomain;
    /**
     * True when we are connected to an ISD
     * <p/>
     * Being connected is defined as having a connection
     * to a card with authentication completed as required
     * by the active security policy.
     */
    private boolean mIsConnected;

    /**
     * Main constructor
     *
     * @param context
     * @param terminal
     */
    public GPCard(GPContext context, CardTerminal terminal) {
        mContext = context;
        mTerminal = terminal;
        mKeys = GPKeySet.GLOBALPLATFORM;
        mProtocolPolicy = SCPProtocolPolicy.PERMISSIVE;
        mSecurityPolicy = SCPSecurityPolicy.CMAC;
        mRegistry = new GPRegistry(this);
        mIssuerDomain = new GPIssuerDomain(this);
    }

    /** @return the terminal in use */
    public CardTerminal getTerminal() {
        return mTerminal;
    }
    /** @return the connected card */
    public Card getCard() {
        return mCard;
    }
    /** @return ISD AID in use */
    public AID getISD() {
        return mISD;
    }
    /** @return keyset in use */
    public GPKeySet getKeys() {
        return mKeys;
    }
    /** @return protocol policy in use */
    public SCPProtocolPolicy getProtocolPolicy() {
        return mProtocolPolicy;
    }
    /** @return security policy in use */
    public SCPSecurityPolicy getSecurityPolicy() {
        return mSecurityPolicy;
    }

    /** @return the active security protocol */
    public SCPProtocol getProtocol() {
        if (mSecure == null || !mSecure.isEstablished()) {
            return null;
        }
        return mSecure.getActiveProtocol();
    }

    /** @return the issuer identification number (IIN) of the card, null if not provided by card */
    public byte[]      getCardIIN() {
        return mCardIIN;
    }
    /** @return the card image number (CIN) of the card, null if not provided by card */
    public byte[]      getCardCIN() {
        return mCardCIN;
    }

    /** @return ISO lifecycle information of the card, null if not provided by card */
    public GPLifeCycle getCardLifeCycle() {
        return mCardLifeCycle;
    }
    /** @return GlobalPlatform card data of the card, null if not provided by card */
    public GPCardData getCardData() {
        return mCardData;
    }
    /** @return GlobalPlatform key information of the card, null if not provided by card */
    public GPKeyInfoTemplate getCardKeyInfo() {
        return mCardKeyInfo;
    }

    /** @return secure channel client */
    public GPSecureChannel getSecureChannel() {
        return mSecure;
    }
    /** @return registry client */
    public GPRegistry getRegistry() {
        return mRegistry;
    }
    /** @return issuer domain client */
    public GPIssuerDomain getIssuerDomain() {
        return mIssuerDomain;
    }

    /**
     * Returns a lifetime identifier for the card
     * <p/>
     * This is constructed from information in the lifecycle structure.
     * <p/>
     * XXX revisit identity strategy
     * <p/>
     * @return a uniquely identifying string
     */
    public String getLifetimeIdentifier() {
        if(mCardLifeCycle != null) {
            return mCardLifeCycle.getLifetimeIdentifier();
        }
        return null;
    }

    /**
     * Set the ISD to use for talking to the card
     * @param isd to be used
     */
    public void setISD(AID isd) {
        ensureNotConnected();
        mISD = isd;
    }

    /**
     * Set the keyset to use for talking to the card
     * @param keys to be used
     */
    public void setKeys(GPKeySet keys) {
        ensureNotConnected();
        mKeys = keys;
    }

    /**
     * Set the protocol policy to use for talking to the card
     * @param policy to be used
     */
    public void setProtocolPolicy(SCPProtocolPolicy policy) {
        ensureNotConnected();
        mProtocolPolicy = policy;
    }

    /**
     * Set the security policy to use for talking to the card
     * @param policy to be used
     */
    public void setSecurityPolicy(SCPSecurityPolicy policy) {
        ensureNotConnected();
        mSecurityPolicy = policy;
    }

    /**
     * Internal: Ensure that the client is not connected
     */
    private void ensureNotConnected() {
        if(mIsConnected) {
            throw new IllegalStateException("Already in a session");
        }
    }

    /**
     * Determine the ISD of the card
     * <p/>
     * This will determine the ISD, just falling through if one has been provided.
     * <p/>
     * If no ISD has been provided this will scan several well-known AIDs.
     * <p/>
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
     * Connect to the card
     *
     * @return true if connected
     * @throws CardException on error
     */
    public boolean connect() throws CardException {
        // check already connected
        if (mIsConnected) {
            LOG.debug("already connected");
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
            LOG.trace("static keys:\n" + mKeys.toString());

            // get the CPLC for reference
            try {
                mCardLifeCycle = readCPLC();
            } catch (SWException ex) {
                LOG.warn("error trying to read CPLC", ex);
            }
            if (mCardLifeCycle == null) {
                LOG.warn("card did not return CPLC");
            } else {
                LOG.trace("card production info:\n" + mCardLifeCycle.toString());
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
                LOG.trace("card data:\n" + mCardData.toString());
            }

            // get IIN and CIN if uniquely identifiable
            if(mCardData != null && mCardData.isUniquelyIdentifiable()) {
                mCardIIN = readCardIIN();
                if (mCardIIN == null) {
                    LOG.debug("card has no IIN");
                } else {
                    LOG.debug("card IIN: " + HexUtil.bytesToHex(mCardIIN));
                }
                mCardCIN = readCardCIN();
                if (mCardCIN == null) {
                    LOG.debug("card has no CIN");
                } else {
                    LOG.debug("card CIN: " + HexUtil.bytesToHex(mCardCIN));
                }
            }

            // get key information and check it against keys
            mCardKeyInfo = readKeyInfo();
            if (mCardKeyInfo == null) {
                throw new CardException("Card did not return a GP key information template");
            } else {
                LOG.trace("key information:\n" + mCardKeyInfo.toString());
                if (mCardKeyInfo.matchesKeysetForUsage(mKeys)) {
                    LOG.debug("keys match key information");
                } else {
                    throw new CardException("Keys do not match key information from card");
                }
            }

            // create a secure channel object
            mSecure = new GPSecureChannel(this, mBasic, mKeys, mProtocolPolicy, mSecurityPolicy);

            // set protocol expectation of secure channel
            if (mCardData != null) {
                mSecure.expectProtocol(
                        mCardData.getSecurityProtocol(),
                        mCardData.getSecurityParameters());
            } else {
                int version = mProtocolPolicy.mScpVersion;
                int parameters = mProtocolPolicy.mScpParameters;
                if(version == 0 || parameters == 0) {
                    throw new CardException("Card has sent no card data. Must specify SCP protocol and parameters.");
                } else {
                    mSecure.expectProtocol(
                            mProtocolPolicy.mScpVersion,
                            mProtocolPolicy.mScpParameters);
                }
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

    /**
     * Disconnect from the card
     *
     * @throws CardException
     */
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

    /**
     * Try to determine the ISD of the card
     *
     * @return AID of the ISD, if found
     * @throws CardException
     */
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

    /**
     * Perform an ISO SELECT FILE BY NAME operation
     *
     * @param name
     * @return response data
     * @throws CardException
     */
    private byte[] selectFileByName(AID name) throws CardException {
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_ISO,
                GP.INS_SELECT,
                GP.SELECT_P1_BY_NAME,
                GP.SELECT_P2_FIRST_OR_ONLY,
                name.getBytes()
        );
        return transactPlainAndCheck(command).getData();
    }

    /**
     * Perform a GlobalPlatform GET DATA operation
     *
     * @param p1p2
     * @return data retrieved
     * @throws CardException
     */
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

    /**
     * Read the ISO life cycle data object
     *
     * @return the object
     * @throws CardException
     */
    private GPLifeCycle readCPLC() throws CardException {
        LOG.debug("readCPLC()");
        GPLifeCycle res = null;
        byte[] data = readData(GP.GET_DATA_P12_CPLC);
        if(data != null) {
            res = GPLifeCycle.read(data);
        }
        return res;
    }

    /**
     * Read the GlobalPlatform card data object
     *
     * @return the object
     * @throws CardException
     */
    private GPCardData readCardData() throws CardException {
        LOG.debug("readCardData()");
        GPCardData res = null;
        byte[] data = readData(GP.GET_DATA_P12_CARD_DATA);
        if (data != null) {
            try {
                res = GPCardData.fromBytes(data);
            } catch (IOException e) {
                throw new CardException("Error parsing card data", e);
            }
        }
        return res;
    }

    /**
     * Read the cards Issuer Identification Number (IIN)
     *
     * @return the IIN
     * @throws CardException
     */
    private byte[] readCardIIN() throws CardException {
        LOG.debug("readCardIIN()");
        return readData(GP.GET_DATA_P12_ISSUER_ID_NUMBER);
    }

    /**
     * Read the cards Card Image Number (CIN)
     *
     * @return the CIN
     * @throws CardException
     */
    private byte[] readCardCIN() throws CardException {
        LOG.debug("readCardCIN()");
        return readData(GP.GET_DATA_P12_CARD_IMG_NUMBER);
    }

    /**
     * Read the GlobalPlatform Application Information object
     *
     * @return the object
     * @throws CardException
     */
    private byte[] readApplicationInfo() throws CardException {
        LOG.debug("readApplicationInfo()");
        return readData(GP.GET_DATA_P12_APPLICATION_INFO);
    }

    /**
     * Read the GlobalPlatform Key Information object
     *
     * @return the object
     * @throws CardException
     */
    private GPKeyInfoTemplate readKeyInfo() throws CardException {
        LOG.debug("readKeyInfo()");
        GPKeyInfoTemplate res;
        byte[] data = readData(GP.GET_DATA_P12_KEY_INFO_TEMPLATE);
        try {
            res = GPKeyInfoTemplate.fromBytes(data);
        } catch (IOException e) {
            throw new CardException("Error parsing key info", e);
        }
        return res;
    }

    /**
     * Read status information for the ISD
     *
     * @return the object
     * @throws CardException
     */
    byte[] readStatusISD() throws CardException {
        return readStatus(GP.GET_STATUS_P1_ISD_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    /**
     * Read status information for all Apps and SDs
     *
     * @return the object
     * @throws CardException
     */
    byte[] readStatusAppsAndSD() throws CardException {
        return readStatus(GP.GET_STATUS_P1_APP_AND_SD_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    /**
     * Read status information for all ELF
     *
     * @return the object
     * @throws CardException
     */
    byte[] readStatusELF() throws CardException {
        return readStatus(GP.GET_STATUS_P1_ELF_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    /**
     * Read status information for all ExM and ELF
     *
     * @return the object
     * @throws CardException
     */
    byte[] readStatusEXMandELF() throws CardException {
        return readStatus(GP.GET_STATUS_P1_EXM_AND_ELF_ONLY, GP.GET_STATUS_P2_FORMAT_TLV);
    }

    /**
     * Perform a GlobalPlatform READ STATUS operation
     * <p/>
     * Convenience form. XXX: document
     * <p/>
     * @param p1Subset
     * @param p2Format
     * @return data retrieved
     * @throws CardException
     */
    private byte[] readStatus(byte p1Subset, byte p2Format) throws CardException {
        byte[] criteria = {0x4F, 0x00}; // XXX !?
        return readStatus(p1Subset, p2Format, criteria);
    }

    /**
     * Perform a GlobalPlatform SET STATUS operation
     *
     * @param p1Subset
     * @param p2Format
     * @param criteria
     * @return data retrieved
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
            //   XXX rethink this loop
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
     * <p/>
     * GlobalPlatform is pleasantly simplistic in its error behaviour.
     * <p/>
     * Everything except for 0x9000 is an error.
     * <p/>
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
