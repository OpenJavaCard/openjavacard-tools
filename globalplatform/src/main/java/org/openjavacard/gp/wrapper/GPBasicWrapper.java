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

import org.openjavacard.emv.CPLC;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.protocol.GP;
import org.openjavacard.gp.protocol.GPCardData;
import org.openjavacard.gp.protocol.GPKeyInfoTemplate;
import org.openjavacard.iso.AID;
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SWException;
import org.openjavacard.tlv.TLVPrimitive;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.IOException;

public class GPBasicWrapper {

    protected final Logger LOG;

    private CardChannel mChannel;

    public GPBasicWrapper(CardChannel channel) {
        LOG = LoggerFactory.getLogger(getClass());
        mChannel = channel;
    }

    public CardChannel getChannel() {
        return mChannel;
    }

    /**
     * Exchange APDUs on the channel
     *
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    public ResponseAPDU transmitRaw(CommandAPDU command) throws CardException {
        LOG.trace("apdu > " + APDUUtil.toString(command));
        ResponseAPDU response = mChannel.transmit(command);
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
    protected void checkResponse(ResponseAPDU response) throws CardException {
        int sw = response.getSW();
        if (sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error in transaction", sw);
        }
    }

    protected ResponseAPDU transact(CommandAPDU command) throws CardException {
        return transmitRaw(command);
    }

    protected ResponseAPDU transactAndCheck(CommandAPDU command) throws CardException {
        ResponseAPDU response = transmitRaw(command);
        checkResponse(response);
        return response;
    }

    /**
     * Perform an ISO SELECT FILE BY NAME operation
     *
     * @param name in the form of an AID
     * @return response data
     * @throws CardException on error
     */
    public byte[] selectFileByName(AID name) throws CardException {
        LOG.trace("selectFileByName(" + name + ")");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_ISO,
                GP.INS_SELECT,
                GP.SELECT_P1_BY_NAME,
                GP.SELECT_P2_FIRST_OR_ONLY,
                name.getBytes()
        );
        return transactAndCheck(command).getData();
    }

    /**
     * Perform a GlobalPlatform GET DATA operation
     *
     * @param p1p2 selecting the data
     * @return data retrieved
     * @throws CardException on error
     */
    public byte[] readData(short p1p2) throws CardException {
        LOG.trace("readData(" + HexUtil.hex16(p1p2) + ")");
        CommandAPDU command = APDUUtil.buildCommand(
                GP.CLA_GP,
                GP.INS_GET_DATA,
                p1p2
        );
        byte[] response = null;
        try {
            response = transactAndCheck(command).getData();
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
     * @throws CardException on error
     */
    public CPLC readCPLC() throws CardException {
        LOG.trace("readCPLC()");
        CPLC res = null;
        byte[] data = readData(GP.GET_DATA_P12_CPLC);
        if(data != null) {
            res = CPLC.read(data);
        }
        return res;
    }

    /**
     * Read the GlobalPlatform card data object
     *
     * @return the object
     * @throws CardException on error
     */
    public GPCardData readCardData() throws CardException {
        LOG.trace("readCardData()");
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

    private final static int TAG_ISSUER_ID_NUMBER = 0x4200;

    /**
     * Read the cards Issuer Identification Number (IIN)
     *
     * @return the IIN
     * @throws CardException on error
     */
    public byte[] readCardIIN() throws CardException {
        LOG.trace("readCardIIN()");
        byte[] data = readData(GP.GET_DATA_P12_ISSUER_ID_NUMBER);
        if(data == null) {
            return null;
        } else {
            try {
                return TLVPrimitive.readPrimitive(data)
                        .asPrimitive(TAG_ISSUER_ID_NUMBER)
                        .getValueBytes();
            } catch (IOException e) {
                throw new CardException("Error parsing IIN TLV", e);
            }
        }
    }

    private final static int TAG_CARD_IMG_NUMBER = 0x4500;

    /**
     * Read the cards Card Image Number (CIN)
     *
     * @return the CIN
     * @throws CardException on error
     */
    public byte[] readCardCIN() throws CardException {
        LOG.trace("readCardCIN()");
        byte[] data = readData(GP.GET_DATA_P12_CARD_IMG_NUMBER);
        if(data == null) {
            return null;
        } else {
            try {
                return TLVPrimitive.readPrimitive(data)
                        .asPrimitive(TAG_CARD_IMG_NUMBER)
                        .getValueBytes();
            } catch (IOException e) {
                throw new CardException("Error parsing CIN TLV", e);
            }
        }
    }

    /**
     * Read the GlobalPlatform Application Information object
     *
     * @return the object
     * @throws CardException on error
     */
    public byte[] readApplicationInfo() throws CardException {
        LOG.trace("readApplicationInfo()");
        return readData(GP.GET_DATA_P12_APPLICATION_INFO);
    }

    /**
     * Read the GlobalPlatform Key Information object
     *
     * @return the object
     * @throws CardException on error
     */
    public GPKeyInfoTemplate readKeyInfo() throws CardException {
        LOG.trace("readKeyInfo()");
        GPKeyInfoTemplate res;
        byte[] data = readData(GP.GET_DATA_P12_KEY_INFO_TEMPLATE);
        try {
            res = GPKeyInfoTemplate.fromBytes(data);
        } catch (IOException e) {
            throw new CardException("Error parsing key info", e);
        }
        return res;
    }

}
