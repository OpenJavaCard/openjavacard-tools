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

package org.openjavacard.tool.command.gp;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.tool.command.base.BasicGPCommand;
import org.openjavacard.iso.AID;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import java.io.PrintStream;

@Parameters(
        commandNames = "gp-identity",
        commandDescription = "GlobalPlatform: show or change card identity"
)
public class GPIdentity extends BasicGPCommand {

    @Parameter(
            names = "--new-iin",
            description = "New Issuer Identification Number (IIN)"
    )
    private byte[] newIIN;

    @Parameter(
            names = "--new-cin",
            description = "New Card Identification Number (CIN)"
    )
    private byte[] newCIN;

    @Parameter(
            names = "--new-isd",
            description = "New AID for the ISD of the card"
    )
    private byte[] newISD;

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        if(newIIN == null && newCIN == null && newISD == null) {
            String lid = card.getLifetimeIdentifier();
            byte[] iin = card.getCardIIN();
            byte[] cin = card.getCardCIN();
            AID isd = card.getISD();

            os.println("Card identity:");
            if(isd != null) {
                os.println("  ISD " + isd);
            }
            if(iin != null) {
                os.println("  IIN " + HexUtil.bytesToHex(iin));
            }
            if(cin != null) {
                os.println("  CIN " + HexUtil.bytesToHex(cin));
            }
            if(lid != null) {
                os.println("  LID " + lid);
            }
            os.println();
        } else {
            card.getIssuerDomain().changeIdentity(newIIN, newCIN, newISD);
        }
    }

}
