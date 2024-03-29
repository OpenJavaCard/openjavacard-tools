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
import org.openjavacard.gp.client.GPIssuerDomain;
import org.openjavacard.gp.client.GPRegistry;
import org.openjavacard.iso.AID;
import org.openjavacard.iso.AIDInfo;
import org.openjavacard.tool.command.base.BasicGPCommand;

import javax.smartcardio.CardException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Parameters(
        commandNames = "gp-delete",
        commandDescription = "GlobalPlatform: delete applets or packages from the card"
)
public class GPDelete extends BasicGPCommand {

    @Parameter(
            names = "--related",
            description = "Delete dependent modules"
    )
    private boolean related = false;

    @Parameter(
            names = "--present",
            description = "Fail when objects don't exist"
    )
    private boolean present = false;

    @Parameter(
            description = "AIDs of objects to delete",
            required = true
    )
    private List<AID> objectAIDs = null;

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        // check protection
        for(AID aid: objectAIDs) {
            AIDInfo info = AIDInfo.get(aid);
            if(info != null && info.protect) {
                if(!forceProtected) {
                    throw new CardException("Object " + aid + " is protected");
                }
            }
        }
        // check presence
        ArrayList<AID> presentAIDS = new ArrayList<>();
        GPRegistry reg = card.getRegistry();
        for (AID aid : objectAIDs) {
            GPRegistry.Entry entry = reg.findAppletOrPackage(aid);
            if(entry != null) {
                os.println("Object " + aid + ": " + entry);
                presentAIDS.add(aid);
            } else {
                if(present) {
                    throw new CardException("Object " + aid + " is not present on the card");
                } else {
                    os.println("Object " + aid + " is not present");
                }
            }
        }
        // perform deletions
        GPIssuerDomain isd = card.getIssuerDomain();
        for (AID aid : presentAIDS) {
            os.println("Deleting object " + aid + (related?" and related":""));
            isd.deleteObject(aid, related);
        }
    }

}
