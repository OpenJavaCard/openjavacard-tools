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

package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import org.openjavacard.generic.GenericCard;
import org.openjavacard.generic.GenericContext;
import org.openjavacard.iso.AID;
import org.openjavacard.util.ATRUtil;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import java.io.PrintStream;

public abstract class GenericCommand implements Runnable {

    @Parameter(
            names = "--reader",
            description = "Reader to use for the operation"
    )
    protected String reader = null;

    @Parameter(
            names = "--select",
            description = "Applet to select before performing the operation"
    )
    protected AID select = null;

    GenericContext mContext;
    GenericCard    mCard;

    public GenericCommand(GenericContext context) {
        mContext = context;
    }

    public void run() {
        PrintStream os = System.out;

        mCard = mContext.findSingleCard(reader);
        try {
            mCard.connect();
            Card card = mCard.getCard();
            os.println("CONNECTED " + card.getProtocol() + " ATR=" + ATRUtil.toString(card.getATR()));
            if (select != null) {
                os.println("SELECT " + select);
                mCard.performSelectByName(select.getBytes(), true);
            }
            performOperation(mCard);

            mCard.disconnect();
        } catch (Exception e) {
            throw new Error("Error performing operation", e);
        }
    }

    protected void performOperation(GenericCard card) throws CardException {
    }

}
