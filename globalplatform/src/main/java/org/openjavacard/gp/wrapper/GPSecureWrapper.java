/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2019 Ingo Albrecht <copyright@promovicz.org>
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

package org.openjavacard.gp.wrapper;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.protocol.GP;
import org.openjavacard.gp.scp.GPSecureChannel;
import org.openjavacard.gp.structure.GPInstallForInstallRequest;
import org.openjavacard.gp.structure.GPInstallForInstallResponse;
import org.openjavacard.gp.structure.GPInstallForLoadRequest;
import org.openjavacard.gp.structure.GPInstallForLoadResponse;
import org.openjavacard.iso.AID;
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SWException;
import org.openjavacard.util.APDUUtil;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;
import java.util.List;

public class GPSecureWrapper extends GPBasicWrapper {

    private GPSecureChannel mSecure;

    public GPSecureWrapper(GPCard card, GPSecureChannel channel) {
        super(card, channel);
        mSecure = channel;
    }

    @Override
    public ResponseAPDU transmitRaw(CommandAPDU command) throws CardException {
        if (!mSecure.isEstablished()) {
            throw new CardException("Secure channel not available");
        }
        return super.transmitRaw(command);
    }

    private ResponseAPDU transactSecureAndCheck(CommandAPDU command) throws CardException {
        return transactAndCheck(command);
    }

    /**
     * Perform a GlobalPlatform READ STATUS operation
     * <p/>
     * Convenience form. XXX: document
     * <p/>
     * @param p1Subset
     * @param p2Format
     * @return data retrieved
     * @throws CardException on error
     */
    public List<byte[]> performReadStatus(byte p1Subset, byte p2Format) throws CardException {
        byte[] criteria = {0x4F, 0x00}; // XXX !?
        return performReadStatus(p1Subset, p2Format, criteria);
    }

    /**
     * Perform a GlobalPlatform SET STATUS operation
     *
     * @param p1Subset
     * @param p2Format
     * @param criteria
     * @return data retrieved
     * @throws CardException on error
     */
    private ArrayList<byte[]> performReadStatus(byte p1Subset, byte p2Format, byte[] criteria) throws CardException {
        LOG.trace("performReadStatus()");
        ArrayList<byte[]> res = new ArrayList<>();
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
            ResponseAPDU response = transactSecureAndCheck(command);
            // get SW and data
            int sw = response.getSW();
            byte[] data = response.getData();
            // append data, no matter the SW
            if (data != null && data.length > 0) {
                res.add(data);
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
        return res;
    }

    public void performStoreData(byte[] block, byte blockNumber, boolean lastBlock) throws CardException {
        LOG.trace("performStoreData()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_STORE_DATA,
                lastBlock ? GP.STORE_DATA_P1_LAST_BLOCK : GP.STORE_DATA_P1_MORE_BLOCKS,
                blockNumber,
                block
        );
        transactSecureAndCheck(command);
    }

    public void performSetStatusISD(AID aid, byte state) throws CardException {
        LOG.trace("performSetStatusISD()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_SET_STATUS,
                GP.SET_STATUS_FOR_ISD,
                state,
                aid.getBytes()
        );
        transactSecureAndCheck(command);
    }

    public void performSetStatusApp(AID aid, byte state) throws CardException {
        LOG.trace("performSetStatusApp()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_SET_STATUS,
                GP.SET_STATUS_FOR_SSD_OR_APP,
                state,
                aid.getBytes()
        );
        transactSecureAndCheck(command);
    }

    public void performSetStatusDomain(AID aid, byte state) throws CardException {
        LOG.trace("performSetStatusDomain()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_SET_STATUS,
                GP.SET_STATUS_FOR_SD_AND_APPS,
                state,
                aid.getBytes()
        );
        transactSecureAndCheck(command);
    }

    public void performPutKey(int keyId, int keyVersion, byte[] keyData, boolean multipleKeys) throws CardException {
        LOG.trace("performPutKey()");
        boolean moreCommands = false;
        byte p1 = (byte)((keyVersion & 0x7F) | (moreCommands?0x80:0x00));
        byte p2 = (byte)((keyId & 0x7F) | (multipleKeys?0x80:0x00));
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_PUT_KEY,
                p1, p2,
                keyData
        );
        transactSecureAndCheck(command);
    }

    public void performLoad(byte[] blockData, int blockNumber, boolean lastBlock) throws CardException {
        LOG.trace("performLoad()");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_LOAD,
                lastBlock ? GP.LOAD_P1_LAST_BLOCK
                        : GP.LOAD_P1_MORE_BLOCKS,
                (byte)blockNumber,
                blockData
        );
        transactSecureAndCheck(command);
    }

    public GPInstallForLoadResponse performInstallForLoad(GPInstallForLoadRequest request) throws CardException {
        LOG.trace("performInstallForLoad()");
        byte[] requestBytes = request.toBytes();
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_INSTALL,
                GP.INSTALL_P1_FOR_LOAD,
                GP.INSTALL_P2_NO_INFORMATION,
                requestBytes);
        ResponseAPDU responseAPDU = transactSecureAndCheck(command);
        GPInstallForLoadResponse response = new GPInstallForLoadResponse();
        response.readBytes(responseAPDU.getData());
        return response;
    }

    public GPInstallForInstallResponse performInstallForInstall(GPInstallForInstallRequest request) throws CardException {
        LOG.trace("performInstallForInstall()");
        byte[] requestBytes = request.toBytes();
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_INSTALL,
                (byte)(GP.INSTALL_P1_FOR_INSTALL|GP.INSTALL_P1_FOR_MAKE_SELECTABLE),
                GP.INSTALL_P2_NO_INFORMATION,
                requestBytes);
        ResponseAPDU responseAPDU = transactSecureAndCheck(command);
        GPInstallForInstallResponse response = new GPInstallForInstallResponse();
        response.readBytes(responseAPDU.getData());
        return response;
    }

}
