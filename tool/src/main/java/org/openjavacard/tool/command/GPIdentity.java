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

package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import java.io.PrintStream;

@Parameters(
        commandNames = "gp-identity",
        commandDescription = "GlobalPlatform: show or change card identity"
)
public class GPIdentity extends GPCommand {

    @Parameter(
            names = "--new-iin",
            description = "New Issuer Identification Number (IIN)"
    )
    private byte[] iin;

    @Parameter(
            names = "--new-cin",
            description = "New Card Identification Number (CIN)"
    )
    private byte[] cin;

    @Parameter(
            names = "--new-isd",
            description = "New AID for the ISD of the card"
    )
    private byte[] isd;

    public GPIdentity(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        if(iin == null && cin == null && isd == null) {
            os.println("Card identity:");
            String identifier = card.getLifetimeIdentifier();
            if(identifier != null) {
                os.println("  LID " + identifier);
            }
            byte[] iin = card.getCardIIN();
            if(iin != null) {
                os.println("  IIN " + HexUtil.bytesToHex(iin));
            }
            byte[] cin = card.getCardCIN();
            if(cin != null) {
                os.println("  CIN " + HexUtil.bytesToHex(cin));
            }
            os.println("  ISD " + card.getISD());
            os.println();
        } else {
            card.getIssuerDomain().changeIdentity(iin, cin, isd);
        }
    }

}
