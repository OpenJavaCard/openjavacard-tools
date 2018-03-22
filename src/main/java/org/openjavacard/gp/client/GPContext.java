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

package org.openjavacard.gp.client;

import org.openjavacard.generic.GenericContext;
import org.openjavacard.iso.AID;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class GPContext extends GenericContext {

    public GPContext() {
        LOG = LoggerFactory.getLogger(GPContext.class);
    }

    public GPCard findSingleGPCard(String prefix) {
        return findSingleGPCard(prefix, null);
    }

    public GPCard findSingleGPCard(String prefix, AID sd) {
        LOG.debug("findSingleCard()");
        GPCard card;
        CardTerminal terminal = findSingleTerminal(prefix);
        try {
            // check card presence
            if (!terminal.isCardPresent()) {
                throw new Error("No card present in terminal");
            }
            // create GP client
            card = new GPCard(this, terminal);
            // tell the client if we know the SD AID
            if(sd != null) {
                card.setISD(sd);
            }
            // detect GP applet
            //boolean detected = card.detect();
            //if (!detected) {
            //    throw new Error("Could not find a GlobalPlatform applet on the card");
            //}
        } catch (CardException e) {
            throw new Error("Error detecting card", e);
        }
        return card;
    }

}
