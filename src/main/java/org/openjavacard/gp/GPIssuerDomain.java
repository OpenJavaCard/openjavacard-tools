/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.gp;

import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.protocol.GP;
import org.openjavacard.gp.protocol.GPKeyInfo;
import org.openjavacard.gp.scp.GPSecureChannel;
import org.openjavacard.iso.AID;
import org.openjavacard.tlv.TLV;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.HexUtil;
import org.openjavacard.util.ReadBytes;
import org.openjavacard.util.ToBytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Client for GlobalPlatform issuer domain functionality
 * <p/>
 * Provides access to card management functionality.
 */
public class GPIssuerDomain {

    private static final Logger LOG = LoggerFactory.getLogger(GPIssuerDomain.class);

    private GPCard mCard;

    public GPIssuerDomain(GPCard card) {
        mCard = card;
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
     * @throws CardException
     */
    public void changeIdentity(byte[] iin, byte[] cin, byte[] isd) throws CardException {
        LOG.debug("setting card identity");
        StoreDataRequest req = new StoreDataRequest();
        req.cardIIN = iin;
        req.cardCIN = cin;
        req.cardISD = isd;
        byte[][] blocks = ArrayUtil.splitBlocks(req.toBytes(), 128);
        for(byte i = 0; i < blocks.length; i++) {
            boolean lastBlock = i == (blocks.length - 1);
            performStoreData(blocks[i], i, lastBlock);
        }
    }

    /**
     * Load the given load file onto the card
     * <p/>
     * Will create a package on the card.
     * <p/>
     * @param file to load
     * @throws CardException
     */
    public void loadFile(GPLoadFile file) throws CardException {
        LOG.debug("loading package " + file.getPackageAID());
        // prepare parameters
        InstallForLoadRequest request = new InstallForLoadRequest();
        request.packageAID = file.getPackageAID();
        // perform INSTALL [for LOAD]
        InstallForLoadResponse response = performInstallForLoad(request);
        // load blocks using LOAD
        List<byte[]> blocks = file.getBlocks();
        int count = blocks.size();
        int last = count - 1;
        for(int i = 0; i < count; i++) {
            byte[] data = blocks.get(i);
            LOG.debug("loading block " + (i+1) + "/" + count + ", " + data.length + " bytes");
            performLoad(data, i, i == last);
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
     * @throws CardException
     */
    public void installApplet(AID packageAID, AID moduleAID,
                              AID appletAID, byte[] appletPrivs, byte[] appletParams)
            throws CardException {
        // use module AID as default for applet AID
        if(appletAID == null) {
            appletAID = moduleAID;
        }
        if(appletPrivs == null) {
            appletPrivs = new byte[1];
        }
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
        InstallForInstallRequest request = new InstallForInstallRequest();
        request.packageAID = packageAID;
        request.moduleAID = moduleAID;
        request.appletAID = appletAID;
        request.privileges = appletPrivs;
        request.installParameters = appletParams;
        // perform the request
        InstallForInstallResponse response = performInstallForInstall(request);
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
     * @throws CardException
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
     * @param related true if dependent objects should be deleted
     * @throws CardException
     */
    public void deleteObject(AID aid, boolean related) throws CardException {
        LOG.debug("deleting object " + aid + (related?" and related":""));
        int aidLen = aid.getLength();
        // pack up the AID in a TLV
        byte[] tlv = new byte[2 + aidLen];
        tlv[0] = 0x4F;
        tlv[1] = (byte) (aidLen & 0xFF);
        System.arraycopy(aid.getBytes(), 0, tlv, 2, aidLen);
        // build the command
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_DELETE,
                (byte) 0,
                related ? GP.DELETE_P2_DELETE_RELATED
                        : GP.DELETE_P2_DELETE_INDICATED,
                tlv);
        // and execute it
        mCard.transactSecureAndCheck(command);
        // log about it
        LOG.debug("deletion finished");
    }

    /**
     * Check compatibility of keys
     * <p/>
     * This can and should be used to check keys before uploading
     * them to the card. It verifies if the given keys comply with
     * the GPKeyInfo supplied by the card.
     * <p/>
     * Service methods in this library will perform this check
     * automatically. This method is provided for use in client
     * application logic.
     * <p/>
     * @param newKeys to check
     * @throws CardException
     */
    public void checkKeys(GPKeySet newKeys) throws CardException {
        GPKeyInfo keyInfo = mCard.getCardKeyInfo();
        if(!keyInfo.matchesKeysetForReplacement(newKeys)) {
            throw new CardException("Keys inappropriate for card");
        }
    }

    /**
     * Replace secure messaging keys
     * <p/>
     * This will irreversibly replace the secure messaging keys.
     * <p/>
     * Compatibility of the keys will be checked before the operation.
     * <p/>
     * @param newKeys to set
     * @throws CardException
     */
    public void replaceKeys(GPKeySet newKeys) throws CardException {
        // do a safety check on the keys first
        checkKeys(newKeys);
        // check that we have keys (paranoid?)
        List<GPKey> keys = newKeys.getKeys();
        if(keys.isEmpty()) {
            throw new CardException("No keys provided");
        }
        // determine various parameters
        byte keyVersion = (byte)newKeys.getKeyVersion();
        boolean multipleKeys = keys.size() > 1;
        GPKey firstKey = keys.get(0);
        byte firstKeyId = firstKey.getId();
        // build a key block for the set
        byte[] data = buildKeyBlock(keyVersion, keys);
        // upload the key block
        //performPutKey(firstKeyId, keyVersion, data, multipleKeys);
    }

    private byte[] buildKeyBlock(byte keyVersion, List<GPKey> keys) throws CardException {
        GPSecureChannel secureChannel = mCard.getSecureChannel();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // first the common version
        bos.write(keyVersion);
        // then the keys
        for(GPKey key: keys) {
            // encrypt the key
            byte[] secret = key.getSecret();
            byte[] encrypted = secureChannel.encryptSensitiveData(secret);
            // add the encrypted data
            bos.write(encrypted, 0, encrypted.length);
        }
        return bos.toByteArray();
    }

    /**
     * Change the card state to INITIALIZED
     *
     * @throws CardException
     */
    public void cardInitialized() throws CardException {
        LOG.debug("cardInitialized()");
        performSetStatusISD(mCard.getISD(), GP.CARD_STATE_INITIALIZED);
    }

    /**
     * Change the card state to SECURED
     *
     * @throws CardException
     */
    public void cardSecured() throws CardException {
        LOG.debug("cardSecured()");
        performSetStatusISD(mCard.getISD(), GP.CARD_STATE_SECURED);
    }

    /**
     * Lock the card
     *
     * @throws CardException
     */
    public void lockCard() throws CardException {
        LOG.debug("cardInitialized()");
        performSetStatusISD(mCard.getISD(), GP.CARD_STATE_LOCKED);
    }

    /**
     * Unlock locked card
     *
     * @throws CardException
     */
    public void unlockCard() throws CardException {
        LOG.debug("unlockCard()");
        performSetStatusISD(mCard.getISD(), GP.CARD_STATE_SECURED);
    }

    /**
     * Terminate the card
     *
     * @throws CardException
     */
    public void terminateCard() throws CardException {
        LOG.debug("terminateCard()");
        performSetStatusISD(mCard.getISD(), GP.CARD_STATE_TERMINATED);
    }

    private void performStoreData(byte[] block, byte blockNumber, boolean lastBlock) throws CardException {
        LOG.trace("performStoreData()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_STORE_DATA,
                lastBlock ? GP.STORE_DATA_P1_LAST_BLOCK : GP.STORE_DATA_P1_MORE_BLOCKS,
                blockNumber,
                block
        );
        mCard.transactSecureAndCheck(command);
    }

    private void performSetStatusISD(AID aid, byte state) throws CardException {
        LOG.trace("performSetStatusISD()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_SET_STATUS,
                GP.SET_STATUS_FOR_ISD,
                state,
                aid.getBytes()
        );
        mCard.transactSecureAndCheck(command);
    }

    private void performSetStatusApp(AID aid, byte state) throws CardException {
        LOG.trace("performSetStatusApp()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_SET_STATUS,
                GP.SET_STATUS_FOR_SSD_OR_APP,
                state,
                aid.getBytes()
        );
        mCard.transactSecureAndCheck(command);
    }

    private void performSetStatusDomain(AID aid, byte state) throws CardException {
        LOG.trace("performSetStatusDomain()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_SET_STATUS,
                GP.SET_STATUS_FOR_SD_AND_APPS,
                state,
                aid.getBytes()
        );
        mCard.transactSecureAndCheck(command);
    }

    private void performPutKey(byte keyId, byte keyVersion, byte[] keyData, boolean multipleKeys) throws CardException {
        LOG.trace("performPutKey()");
        boolean moreCommands = false;
        byte p1 = (byte)(keyVersion | (moreCommands?0x80:0x00));
        byte p2 = (byte)(keyId | (multipleKeys?0x80:0x00));
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_PUT_KEY,
                p1, p2,
                keyData
        );
        mCard.transactSecureAndCheck(command);
    }

    private void performLoad(byte[] blockData, int blockNumber, boolean lastBlock) throws CardException {
        LOG.trace("performLoad()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_LOAD,
                lastBlock ? GP.LOAD_P1_LAST_BLOCK
                          : GP.LOAD_P1_MORE_BLOCKS,
                (byte)blockNumber,
                blockData
        );
        mCard.transactSecureAndCheck(command);
    }

    private InstallForLoadResponse performInstallForLoad(InstallForLoadRequest request) throws CardException {
        LOG.trace("performInstallForLoad()");
        byte[] requestBytes = request.toBytes();
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_INSTALL,
                GP.INSTALL_P1_FOR_LOAD,
                GP.INSTALL_P2_NO_INFORMATION,
                requestBytes);
        ResponseAPDU responseAPDU = mCard.transactSecureAndCheck(command);
        InstallForLoadResponse response = new InstallForLoadResponse();
        response.readBytes(responseAPDU.getData());
        return response;
    }

    private InstallForInstallResponse performInstallForInstall(InstallForInstallRequest request) throws CardException {
        LOG.trace("performInstallForInstall()");
        byte[] requestBytes = request.toBytes();
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_INSTALL,
                (byte)(GP.INSTALL_P1_FOR_INSTALL|GP.INSTALL_P1_FOR_MAKE_SELECTABLE),
                GP.INSTALL_P2_NO_INFORMATION,
                requestBytes);
        ResponseAPDU responseAPDU = mCard.transactSecureAndCheck(command);
        InstallForInstallResponse response = new InstallForInstallResponse();
        response.readBytes(responseAPDU.getData());
        return response;
    }

    private static class StoreDataRequest implements ToBytes {
        private static final int TAG_ISSUER_IDENTIFICATION_NUMBER = 0x42;
        private static final int TAG_CARD_IMAGE_NUMBER = 0x45;
        private static final int TAG_ISD_AID = 0x4F;

        byte[] cardIIN;
        byte[] cardCIN;
        byte[] cardISD;
        byte[] cardData;

        @Override
        public byte[] toBytes() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                if(cardIIN != null) {
                    bos.write(TLV.encode(TAG_ISSUER_IDENTIFICATION_NUMBER, cardIIN));
                }
                if(cardCIN != null) {
                    bos.write(TLV.encode(TAG_CARD_IMAGE_NUMBER, cardCIN));
                }
                if(cardISD != null) {
                    bos.write(TLV.encode(TAG_ISD_AID, cardISD));
                }
            } catch (IOException e) {
                throw new Error("Error serializing INSTALL [for  LOAD] request", e);
            }
            return bos.toByteArray();
        }
    }

    private static class InstallForLoadRequest implements ToBytes {
        AID packageAID;
        AID sdAID;
        byte[] loadHash;
        byte[] loadParameters;
        byte[] loadToken;

        @Override
        public byte[] toBytes() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                if(packageAID == null) {
                    throw new IOException("Load file AID is mandatory");
                } else {
                    bos.write(packageAID.getLength());
                    bos.write(packageAID.getBytes());
                }
                if(sdAID == null) {
                    bos.write(0);
                } else {
                    bos.write(sdAID.getLength());
                    bos.write(sdAID.getBytes());
                }
                if(loadHash == null) {
                    bos.write(0);
                } else {
                    bos.write(loadHash.length);
                    bos.write(loadHash);
                }
                if(loadParameters == null) {
                    bos.write(0);
                } else {
                    bos.write(loadParameters.length);
                    bos.write(loadParameters);
                }
                if(loadToken == null) {
                    bos.write(0);
                } else {
                    bos.write(loadToken.length);
                    bos.write(loadToken);
                }
            } catch (IOException e) {
                throw new Error("Error serializing INSTALL [for  LOAD] request", e);
            }
            return bos.toByteArray();
        }
    }

    private static class InstallForLoadResponse implements ReadBytes {

        @Override
        public void readBytes(byte[] bytes) {
        }
    }

    private static class InstallForInstallRequest implements ToBytes {
        public AID packageAID;
        public AID moduleAID;
        public AID appletAID;
        public byte[] privileges;
        public byte[] installParameters;
        public byte[] installToken;

        @Override
        public byte[] toBytes() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                if(packageAID == null) {
                    throw new IOException("Package AID is mandatory");
                } else {
                    bos.write(packageAID.getLength());
                    bos.write(packageAID.getBytes());
                }
                if(moduleAID == null) {
                    throw new IOException("Module AID is mandatory");
                } else {
                    bos.write(moduleAID.getLength());
                    bos.write(moduleAID.getBytes());
                }
                if(appletAID == null) {
                    throw new IOException("Applet AID is mandatory");
                } else {
                    bos.write(appletAID.getLength());
                    bos.write(appletAID.getBytes());
                }
                if(privileges == null) {
                    throw new IOException("Privileges are mandatory");
                } else {
                    bos.write(privileges.length);
                    bos.write(privileges);
                }
                if(installParameters == null) {
                    bos.write(new byte[] { (byte)0x02, (byte)0xC9, (byte)0x00 });
                } else {
                    bos.write(installParameters.length + 2);
                    bos.write((byte)0xC9);
                    bos.write(installParameters.length);
                    bos.write(installParameters);
                }
                if(installToken == null) {
                    bos.write(0);
                } else {
                    bos.write(installToken.length);
                    bos.write(installToken);
                }
            } catch (IOException e) {
                throw new Error("Error serializing INSTALL [for INSTALL] request", e);
            }
            return bos.toByteArray();
        }
    }

    private static class InstallForInstallResponse implements ReadBytes {

        @Override
        public void readBytes(byte[] bytes) {
        }
    }

}
