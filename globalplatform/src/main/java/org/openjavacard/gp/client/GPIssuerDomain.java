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

import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.protocol.GP;
import org.openjavacard.gp.protocol.GPKeyInfo;
import org.openjavacard.gp.protocol.GPKeyInfoTemplate;
import org.openjavacard.gp.scp.GPSecureChannel;
import org.openjavacard.gp.structure.GPInstallForInstallRequest;
import org.openjavacard.gp.structure.GPInstallForInstallResponse;
import org.openjavacard.gp.structure.GPInstallForLoadRequest;
import org.openjavacard.gp.structure.GPInstallForLoadResponse;
import org.openjavacard.gp.structure.GPStoreDataRequest;
import org.openjavacard.gp.wrapper.GPSecureWrapper;
import org.openjavacard.iso.AID;
import org.openjavacard.tlv.TLVPrimitive;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Client for GlobalPlatform issuer domain functionality
 * <p/>
 * Provides access to card management functionality.
 */
public class GPIssuerDomain {

    private static final Logger LOG = LoggerFactory.getLogger(GPIssuerDomain.class);

    private GPCard mCard;
    private GPSecureWrapper mWrapper;

    public GPIssuerDomain(GPCard card, GPSecureWrapper wrapper) {
        mCard = card;
        mWrapper = wrapper;
    }

    /**
     * Change the identity values of the card
     * <p/>
     * This allows setting of the CIN, IIN as well as the AID of the ISD.
     * <p/>
     * Note that all parameters are optional.
     * <p/>
     * @param cin to set or null
     * @param iin to set or null
     * @param isd to set or null
     * @throws CardException on error
     */
    public void changeIdentity(byte[] iin, byte[] cin, byte[] isd) throws CardException {
        LOG.debug("setting card identity");
        // log new values
        if(iin != null) {
            LOG.trace("new IIN " + HexUtil.bytesToHex(iin));
        }
        if(cin != null) {
            LOG.trace("new CIN " + HexUtil.bytesToHex(cin));
        }
        if(isd != null) {
            LOG.trace("new ISD " + HexUtil.bytesToHex(isd));
        }
        // build the request
        GPStoreDataRequest req = new GPStoreDataRequest();
        req.cardIIN = iin;
        req.cardCIN = cin;
        req.cardISD = isd;
        // split the request
        byte[][] blocks = ArrayUtil.splitBlocks(req.toBytes(), 128); // TODO arbitrary
        // transmit the request as a chain of STORE DATA commands
        for(byte i = 0; i < blocks.length; i++) {
            boolean lastBlock = i == (blocks.length - 1);
            mWrapper.performStoreData(blocks[i], i, lastBlock);
        }
    }

    /**
     * Load the given load file onto the card
     * <p/>
     * Will create a package on the card.
     * <p/>
     * @param file to load
     * @throws CardException on error
     */
    public void loadFile(GPLoadFile file) throws CardException {
        LOG.debug("loading package " + file.getPackageAID());
        // prepare parameters
        GPInstallForLoadRequest request = new GPInstallForLoadRequest();
        request.packageAID = file.getPackageAID();
        // perform INSTALL [for LOAD]
        GPInstallForLoadResponse response = mWrapper.performInstallForLoad(request);
        // load blocks using LOAD
        List<byte[]> blocks = file.getBlocks();
        int count = blocks.size();
        int last = count - 1;
        for(int i = 0; i < count; i++) {
            byte[] data = blocks.get(i);
            LOG.debug("loading block " + (i+1) + "/" + count + ", " + data.length + " bytes");
            mWrapper.performLoad(data, i, i == last);
        }
        // finish up
        LOG.debug("load complete");
    }

    /**
     * Install an applet using the specified package and module
     * <p/>
     * Will create an actual, potentially selectable, applet on the card.
     * <p/>
     * If no appletAID is specified the moduleAID will be used.
     * If no appletPrivs is specified the applet will have no privileges.
     * If no appletParams is specified the install data will be empty.
     * <p/>
     * @param packageAID AID of the package to install
     * @param moduleAID AID of the module to install
     * @param appletAID AID for the new applet (optional)
     * @param appletPrivs privileges for the new applet (optional)
     * @param appletParams parameters for the new applet (optional)
     * @throws CardException on error
     */
    public void installApplet(AID packageAID, AID moduleAID,
                              AID appletAID, byte[] appletPrivs, byte[] appletParams)
            throws CardException {
        // use module AID as default for applet AID
        if(appletAID == null) {
            appletAID = moduleAID;
        }
        // default to no privileges
        if(appletPrivs == null) {
            appletPrivs = new byte[1];
        }
        // default to empty applet params
        if(appletParams == null) {
            appletParams = new byte[0];
        }
        // log verbosely
        LOG.debug("installing applet " + appletAID);
        LOG.debug("using package " + packageAID + " module " + moduleAID);
        LOG.debug("using privileges " + HexUtil.bytesToHex(appletPrivs));
        if(appletParams.length > 0) {
            LOG.debug("using parameters " + HexUtil.bytesToHex(appletParams));
        }
        // prepare parameters
        GPInstallForInstallRequest request = new GPInstallForInstallRequest();
        request.packageAID = packageAID;
        request.moduleAID = moduleAID;
        request.appletAID = appletAID;
        request.privileges = appletPrivs;
        request.installParameters = appletParams;
        // perform the request
        GPInstallForInstallResponse response = mWrapper.performInstallForInstall(request);
        // finish up
        LOG.debug("install complete");
    }

    /**
     * Delete an object on the card
     * <p/>
     * Can be used for applets and packages.
     * <p/>
     * Related/dependent objects will not be deleted. Attempting
     * to delete an object that is still in use will fail.
     * <p/>
     * @param aid of the object to be deleted
     * @throws CardException on error
     */
    public void deleteObject(AID aid) throws CardException {
        deleteObject(aid, false);
    }

    /**
     * Delete an object on the card
     * <p/>
     * Can be used for applets and packages.
     * <p/>
     * This variant allows the deletion of related/dependent objects.
     * <p/>
     * @param aid of the object to be deleted
     * @param deleteRelated true if dependent objects should be deleted
     * @throws CardException on error
     */
    public void deleteObject(AID aid, boolean deleteRelated) throws CardException {
        LOG.debug("deleting object " + aid + (deleteRelated?" and related":""));
        // perform the operation
        mWrapper.performDelete(aid, deleteRelated);
        // log about it
        LOG.debug("deletion finished");
    }

    /**
     * Replace secure messaging keys
     * <p/>
     * This will irreversibly replace the secure messaging keys.
     * <p/>
     * Compatibility of the keys will be checked before the operation.
     * <p/>
     * This method is only suitable for replacing the whole set of required keys in one pass.
     * <p/>
     * @param newKeys to set
     * @throws CardException on error
     */
    public void replaceKeys(GPKeySet newKeys) throws CardException {
        boolean logKeys = mCard.getContext().isKeyLoggingEnabled();
        // we always try to replace what the KIT describes
        GPKeyInfoTemplate kit = mCard.getCardKeyInfo();
        // determine various bits of information
        int keyVersion = newKeys.getKeyVersion();
        byte firstKeyId = (byte)(kit.getKeyInfos().get(0).getKeyId());
        int keyCount = kit.getKeyInfos().size();
        boolean multipleKeys = (keyCount > 1);
        // log about it
        LOG.debug("replacing " + keyCount + " keys"
                + " starting at id " + firstKeyId
                + " with version " + keyVersion);
        // do a safety check on the keys first
        kit.checkKeySetForReplacement(newKeys);
        // build a key block for the set
        byte[] data = buildKeyBlock(kit, newKeys);
        // log the encrypted key block
        if(logKeys) {
            LOG.trace("key block " + HexUtil.bytesToHex(data) + " length " + data.length);
        }
        // upload the key block
        mWrapper.performPutKey(firstKeyId, keyVersion, data, multipleKeys);
    }

    private byte[] buildKeyBlock(GPKeyInfoTemplate keyInfoTemplate, GPKeySet newKeys)
            throws CardException {
        int keyVersion = newKeys.getKeyVersion();
        LOG.trace("building format 1 key block for key version " + keyVersion);
        boolean logKeys = mCard.getContext().isKeyLoggingEnabled();
        // need the secure channel for key encryption
        GPSecureChannel secureChannel = mCard.getSecureChannel();
        // start building the key block
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // first common key version
        bos.write(keyVersion);
        // then the keys
        for(GPKeyInfo keyInfo: keyInfoTemplate.getKeyInfos()) {
            int keyId = keyInfo.getKeyId();
            LOG.trace("template " + keyInfo);
            GPKey key = newKeys.getKeyById(keyId);
            LOG.trace("found " + key);
            // encrypt the secret
            byte[] secret = key.getSecret();
            byte[] encrypted = secureChannel.encryptSensitiveData(secret);
            if (logKeys) {
                LOG.trace("plain secret " + HexUtil.bytesToHex(secret));
                LOG.trace("encrypted secret " + HexUtil.bytesToHex(encrypted));
            }
            // compute the key check value
            byte[] kcv = key.getCheckValue(key.getCipher());
            if (logKeys) {
                LOG.trace("key check value " + HexUtil.bytesToHex(kcv));
            }
            // encode the key as TLV
            int keyType = keyInfo.getKeyTypes()[0];
            TLVPrimitive tlv = new TLVPrimitive(GP.keyTypeTag(keyType), encrypted);
            if(logKeys) {
                LOG.trace("encoded " + tlv);
            }
            byte[] wrapped = tlv.getEncoded();
            // encode key and check value
            bos.write(wrapped, 0, wrapped.length);
            bos.write(kcv.length);
            bos.write(kcv, 0, kcv.length);
        }
        // return the whole block
        return bos.toByteArray();
    }

    /**
     * Change the card state to INITIALIZED
     *
     * @throws CardException on error
     */
    public void cardInitialized() throws CardException {
        LOG.debug("cardInitialized()");
        mWrapper.performSetStatusISD(mCard.getISD(), GP.CARD_STATE_INITIALIZED);
    }

    /**
     * Change the card state to SECURED
     *
     * @throws CardException on error
     */
    public void cardSecured() throws CardException {
        LOG.debug("cardSecured()");
        mWrapper.performSetStatusISD(mCard.getISD(), GP.CARD_STATE_SECURED);
    }

    /**
     * Lock the card
     *
     * @throws CardException on error
     */
    public void lockCard() throws CardException {
        LOG.debug("cardInitialized()");
        mWrapper.performSetStatusISD(mCard.getISD(), GP.CARD_STATE_LOCKED);
    }

    /**
     * Unlock locked card
     *
     * @throws CardException on error
     */
    public void unlockCard() throws CardException {
        LOG.debug("unlockCard()");
        mWrapper.performSetStatusISD(mCard.getISD(), GP.CARD_STATE_SECURED);
    }

    /**
     * Terminate the card
     *
     * @throws CardException on error
     */
    public void terminateCard() throws CardException {
        LOG.debug("terminateCard()");
        mWrapper.performSetStatusISD(mCard.getISD(), GP.CARD_STATE_TERMINATED);
    }

    /**
     * Lock the specified applet
     * @param appletAID of the applet
     * @throws CardException on error
     */
    public void lockApplet(AID appletAID) throws CardException {
        LOG.debug("lockApplet()");
        throw new RuntimeException("Not implemented");
    }

    /**
     * Unlock the specified applet
     * @param appletAID of the applet
     * @throws CardException on error
     */
    public void unlockApplet(AID appletAID) throws CardException {
        LOG.debug("unlockApplet()");
        throw new RuntimeException("Not implemented");
    }

}
