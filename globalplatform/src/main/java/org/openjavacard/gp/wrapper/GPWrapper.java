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
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SWException;
import org.openjavacard.util.APDUUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public abstract class GPWrapper {

    protected final Logger LOG;

    protected GPCard mCard;
    protected CardChannel mChannel;

    protected GPWrapper(GPCard card, CardChannel channel) {
        LOG = LoggerFactory.getLogger(getClass());
        mCard = card;
        mChannel = channel;
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

    /**
     * Exchange APDUs on the channel
     *
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    protected ResponseAPDU transmitRaw(CommandAPDU command) throws CardException {
        LOG.trace("apdu > " + APDUUtil.toString(command));
        ResponseAPDU response = mChannel.transmit(command);
        LOG.trace("apdu < " + APDUUtil.toString(response));
        return response;
    }

}
