package better.smartcard.gp;

import better.smartcard.gp.protocol.GP;
import better.smartcard.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class GPIssuerDomain {

    private static final Logger LOG = LoggerFactory.getLogger(GPIssuerDomain.class);

    private GPCard mCard;

    public GPIssuerDomain(GPCard card) {
        mCard = card;
    }

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
        request.installParameters = new byte[] { (byte)0xC9, (byte)0x00 };
        // perform the request
        InstallForInstallResponse response = performInstallForInstall(request);
        // finish up
        LOG.debug("install complete");
    }

    public void deleteObject(AID aid) throws CardException {
        deleteObject(aid, false);
    }

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

    public void cardInitialized() throws CardException {
        LOG.debug("cardInitialized()");
        performSetStatusISD(mCard.getCardISD(), GP.CARD_STATE_INITIALIZED);
    }

    public void cardSecured() throws CardException {
        LOG.debug("cardSecured()");
        performSetStatusISD(mCard.getCardISD(), GP.CARD_STATE_SECURED);
    }

    public void lockCard() throws CardException {
        LOG.debug("cardInitialized()");
        performSetStatusISD(mCard.getCardISD(), GP.CARD_STATE_LOCKED);
    }

    public void unlockCard() throws CardException {
        LOG.debug("unlockCard()");
        performSetStatusISD(mCard.getCardISD(), GP.CARD_STATE_SECURED);
    }

    public void terminateCard() throws CardException {
        LOG.debug("terminateCard()");
        performSetStatusISD(mCard.getCardISD(), GP.CARD_STATE_TERMINATED);
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
                    throw new IOException("Install parameters are mandatory");
                } else {
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
