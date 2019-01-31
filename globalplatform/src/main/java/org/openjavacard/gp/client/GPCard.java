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

package org.openjavacard.gp.client;

import org.openjavacard.emv.CPLC;
import org.openjavacard.gp.keys.GPKeyDiversification;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.protocol.GPCardData;
import org.openjavacard.gp.protocol.GPKeyInfoTemplate;
import org.openjavacard.gp.scp.GPSecureChannel;
import org.openjavacard.gp.scp.SCPParameters;
import org.openjavacard.gp.scp.SCPProtocolPolicy;
import org.openjavacard.gp.scp.SCPSecurityPolicy;
import org.openjavacard.gp.wrapper.GPBasicWrapper;
import org.openjavacard.gp.wrapper.GPSecureWrapper;
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
    private CardChannel mBasicChannel;
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
     * Key diversification to apply
     */
    private GPKeyDiversification mDiversification;
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
    private CPLC mCPLC;
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
     * Our GP plain command wrapper
     */
    private GPBasicWrapper mBasicWrapper;
    /**
     * Our GP secure command wrapper
     */
    private GPSecureWrapper mSecureWrapper;
    /**
     * Our SCP secure channel
     * <p/>
     * Created when secure session starts.
     */
    private GPSecureChannel mSecureChannel;
    /**
     * Registry instance
     * <p/>
     * Cached representation of ISD-managed on-card
     * objects such as installed applets and security
     * domains.
     */
    private GPRegistry mRegistry;
    /**
     * Issuer domain access object
     * <p/>
     * This is the interface to be used for communicating
     * with the ISD to perform the various operations
     * that it enables.
     */
    private GPIssuerDomain mIssuerDomain;
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
     * @param context to operate in
     * @param terminal to communicate on
     */
    public GPCard(GPContext context, CardTerminal terminal) {
        mContext = context;
        mTerminal = terminal;
        mKeys = GPKeySet.GLOBALPLATFORM;
        mDiversification = GPKeyDiversification.NONE;
        mProtocolPolicy = SCPProtocolPolicy.PERMISSIVE;
        mSecurityPolicy = SCPSecurityPolicy.CMAC;
    }

    /** @return the context of this client */
    public GPContext getContext() {
        return mContext;
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
    /** @return key diversification to use */
    public GPKeyDiversification getDiversification() {
        return mDiversification;
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
    public SCPParameters getProtocol() {
        if (mSecureChannel == null || !mSecureChannel.isEstablished()) {
            return null;
        }
        return mSecureChannel.getActiveProtocol();
    }

    /** @return the issuer identification number (IIN) of the card, null if not provided by card */
    public byte[]      getCardIIN() {
        return mCardIIN;
    }
    /** @return the card image number (CIN) of the card, null if not provided by card */
    public byte[]      getCardCIN() {
        return mCardCIN;
    }

    /** @return EMV lifecycle information of the card, null if not provided by card */
    public CPLC getCPLC() {
        return mCPLC;
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
        return mSecureChannel;
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
        if(mCPLC != null) {
            return mCPLC.getLifetimeIdentifier();
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
     * Set the key diversification to be used
     * @param diversification to be used
     */
    public void setDiversification(GPKeyDiversification diversification) {
        ensureNotConnected();
        mDiversification = diversification;
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
     * @throws CardException on error
     */
    public boolean detect() throws CardException {
        return detect(false);
    }

    private boolean detect(boolean stayConnected) throws CardException {
        // return if no need
        if(mISD != null) {
            return true;
        }

        // log about it
        LOG.debug("detecting ISD");

        // work exclusively
        try {
            // make sure we are connected
            ensureConnectedToCard();

            // find the issuer applet
            mISD = findISD();

            // log about it
            if (mISD != null) {
                LOG.debug("found ISD at " + mISD);
            } else {
                LOG.debug("could not find an ISD");
            }
        } finally {
            if(!stayConnected) {
                // disconnect everything
                disconnect();
            }
        }

        // return true if we found an ISD
        return mISD != null;
    }

    /**
     * Connect to the card
     *
     * @throws CardException on error
     */
    public void connect() throws CardException {
        // check already connected
        if (mIsConnected) {
            LOG.debug("already connected");
        }

        // disconnect on exception
        try {
            // connect to the card
            ensureConnectedToCard();

            mBasicWrapper = new GPBasicWrapper(this, mBasicChannel);

            // check for ISD and detect if needed
            if (mISD == null && !detect(true)) {
                throw new CardException("Could not determine ISD");
            }

            // select the ISD
            mBasicWrapper.selectFileByName(mISD);

            // log static keys
            if(mContext.isKeyLoggingEnabled()) {
                LOG.trace("static keys:\n" + mKeys.toString());
            }

            // get CPLC, can be used for identification if present
            mCPLC = mBasicWrapper.readCPLC();
            if (mCPLC == null) {
                // this is completely normal
                LOG.debug("card has no lifecycle data");
            } else {
                LOG.trace("card lifecycle:\n" + mCPLC.toString());
            }

            // get card data, needed to determine SCP parameters
            mCardData = mBasicWrapper.readCardData();
            if (mCardData == null) {
                // this is normal for SCP01
                LOG.debug("card has no card data");
            } else {
                LOG.trace("card data:\n" + mCardData.toString());
            }

            // get IIN and CIN if uniquely identifiable
            if(mCardData != null && mCardData.isUniquelyIdentifiable()) {
                mCardIIN = mBasicWrapper.readCardIIN();
                if (mCardIIN == null) {
                    LOG.debug("card has no IIN");
                } else {
                    LOG.debug("card IIN: " + HexUtil.bytesToHex(mCardIIN));
                }
                mCardCIN = mBasicWrapper.readCardCIN();
                if (mCardCIN == null) {
                    LOG.debug("card has no CIN");
                } else {
                    LOG.debug("card CIN: " + HexUtil.bytesToHex(mCardCIN));
                }
            } else {
                // unique identification is an optional feature
                LOG.debug("card is not uniquely identifiable");
            }

            // get key information, which must be present
            mCardKeyInfo = mBasicWrapper.readKeyInfo();
            if (mCardKeyInfo == null) {
                throw new CardException("Card returned no key information template");
            } else {
                LOG.trace("key information:\n" + mCardKeyInfo.toString());
            }

            // check key against key information
            LOG.debug("checking key compatibility");
            mCardKeyInfo.checkKeysetForUsage(mKeys);

            // create a secure channel object
            mSecureChannel = new GPSecureChannel(this, mBasicWrapper, mKeys, mDiversification, mProtocolPolicy, mSecurityPolicy);

            // set protocol expectation of secure channel
            if (mCardData != null) {
                mSecureChannel.expectProtocol(
                        mCardData.getSecurityProtocol(),
                        mCardData.getSecurityParameters());
            } else {
                int version = mProtocolPolicy.mScpVersion;
                int parameters = mProtocolPolicy.mScpParameters;
                if(version == 0 || parameters == 0) {
                    throw new CardException("Card has sent no card data. Must specify SCP protocol and parameters.");
                } else {
                    mSecureChannel.expectProtocol(version, parameters);
                }
            }

            // try to open the secure channel
            mSecureChannel.open();

            mSecureWrapper = new GPSecureWrapper(this, mSecureChannel);

            mIssuerDomain = new GPIssuerDomain(this, mSecureWrapper);
            mRegistry = new GPRegistry(this, mSecureWrapper);

            // mark as connected
            mIsConnected = true;
        } finally {
            if (!mIsConnected) {
                disconnect();
            }
        }
    }

    /**
     * Disconnect from the card
     *
     * @throws CardException on error
     */
    public void disconnect() throws CardException {
        // tear down the secure channel
        if (mSecureChannel != null) {
            mSecureChannel.close();
            mSecureChannel = null;
        }
        // tear down the basic channel
        if(mBasicChannel != null) {
            //mBasicChannel.close();
            mBasicChannel = null;
        }
        // disconnect and reset the card
        if (mCard != null) {
            mCard.endExclusive();
            mCard.disconnect(true);
            mCard = null;
        }
        // mark as disconnected
        mIsConnected = false;
        // log about it
        LOG.debug("disconnected");
    }

    private void ensureConnectedToCard() throws CardException {
        if(mCard == null) {
            // connect to the card
            mCard = mTerminal.connect("*");

            // log connection parameters
            LOG.debug("connected " + mCard.getProtocol() + " ATR="
                    + HexUtil.bytesToHex(mCard.getATR().getBytes()));

            // always work exclusive
            mCard.beginExclusive();

            // get the basic channel
            mBasicChannel = mCard.getBasicChannel();
        }
    }

    /**
     * Try to determine the ISD of the card
     *
     * @return AID of the ISD, if found
     * @throws CardException on error
     */
    private AID findISD() throws CardException {
        AID isd = mISD;
        if(isd == null) {
            for (AID name : PROBE_AIDS) {
                try {
                    mBasicWrapper.selectFileByName(name);
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

}
