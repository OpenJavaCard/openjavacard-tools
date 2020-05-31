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

import com.beust.jcommander.Parameters;
import org.openjavacard.emv.CPLC;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.gp.protocol.GPCardData;
import org.openjavacard.gp.protocol.GPKeyInfoTemplate;
import org.openjavacard.gp.scp.SCPParameters;
import org.openjavacard.tool.command.base.BasicGPCommand;
import org.openjavacard.util.ATRUtil;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import java.io.PrintStream;

@Parameters(
        commandNames = "gp-info",
        commandDescription = "GlobalPlatform: show information about card"
)
public class GPInfo extends BasicGPCommand {

    @Override
    protected void performOperation(GPContext context, GPCard card) {
        PrintStream os = System.out;

        CardTerminal terminal = card.getTerminal();
        os.println("Terminal information:");
        os.println("  Name \"" + terminal.getName() + "\"");
        os.println("  Class " + terminal.getClass().getName());
        os.println();

        Card scard = card.getCard();
        os.println("Card information:");
        os.println("  Protocol " + scard.getProtocol());
        os.println("  ATR " + ATRUtil.toString(scard.getATR()));
        os.println("  Class " + scard.getClass().getName());
        os.println();

        CPLC lifeCycle = card.getCPLC();
        if (lifeCycle == null) {
            os.println("Card has no CPLC");
        } else {
            os.println(lifeCycle.toString());
        }
        os.println();

        os.println("Card identity:");
        os.println("  ISD " + card.getISD());
        byte[] iin = card.getCardIIN();
        if(iin != null) {
            os.println("  IIN " + HexUtil.bytesToHex(iin));
        }
        byte[] cin = card.getCardCIN();
        if(cin != null) {
            os.println("  CIN " + HexUtil.bytesToHex(cin));
        }
        String identifier = card.getLifetimeIdentifier();
        if(identifier != null) {
            os.println("  LID " + identifier);
        }
        os.println();

        GPCardData cardData = card.getCardData();
        if (cardData == null) {
            os.println("Card has no GlobalPlatform card data");
        } else {
            os.println(cardData.toString());
        }
        os.println();

        SCPParameters scpProtocol = card.getProtocol();
        if (scpProtocol == null) {
            os.println("Could not determine SCP protocol");
        } else {
            os.println(scpProtocol.toVerboseString());
        }
        os.println();

        GPKeyInfoTemplate keyInfo = card.getCardKeyInfo();
        if (keyInfo == null) {
            os.println("Card has no GlobalPlatform key information");
        } else {
            os.println(keyInfo.toString());
        }
        os.println();

        GPList.printRegistry(os, card, true, true, true);
    }

}
