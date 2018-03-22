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

package org.openjavacard.generic;

import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SWException;
import org.openjavacard.util.APDUUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class GenericCard {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCard.class);

    GenericContext mContext;
    CardTerminal mTerminal;
    Card mCard;
    CardChannel mBasic;

    public GenericCard(GenericContext context, CardTerminal terminal) {
        mContext = context;
        mTerminal = terminal;
        mCard = null;
    }

    public CardTerminal getTerminal() {
        return mTerminal;
    }

    public boolean isConnected() {
        return mCard != null;
    }

    public Card getCard() {
        return mCard;
    }

    public CardChannel getBasicChannel() {
        return mCard.getBasicChannel();
    }

    public void connect() throws CardException {
        mCard = mTerminal.connect("*");
        mBasic = mCard.getBasicChannel();
    }

    public void reconnect(boolean reset) throws CardException {
        mCard.disconnect(reset);
        connect();
    }

    public void disconnect() {
        try {
            mCard.disconnect(false);
            mCard = null;
        } catch (CardException e) {
        }
    }

    public ResponseAPDU performSelectByName(byte[] name, boolean first) throws CardException {
        byte p2 = first ? ISO7816.SELECT_P2_FIRST_OR_ONLY : ISO7816.SELECT_P2_NEXT;
        return performSelect(ISO7816.SELECT_P1_BY_NAME, p2, name);
    }

    private ResponseAPDU performSelect(byte p1, byte p2, byte[] data) throws CardException {
        CommandAPDU scapdu = APDUUtil.buildCommand(
                ISO7816.CLA_ISO7816, ISO7816.INS_SELECT,
                p1, p2, data);
        return transmitAndCheck(scapdu);
    }

    public ResponseAPDU transmit(CommandAPDU command) throws CardException {
        return transmit(mBasic, command);
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
        LOG.debug("apdu > " + APDUUtil.toString(command));
        ResponseAPDU response = channel.transmit(command);
        LOG.debug("apdu < " + APDUUtil.toString(response));
        return response;
    }

    public ResponseAPDU transmitAndCheck(CommandAPDU command) throws CardException {
        return transmitAndCheck(mBasic, command);
    }

    public ResponseAPDU transmitAndCheck(CardChannel channel, CommandAPDU command) throws CardException {
        ResponseAPDU response = transmit(channel, command);
        checkResponse(response);
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
