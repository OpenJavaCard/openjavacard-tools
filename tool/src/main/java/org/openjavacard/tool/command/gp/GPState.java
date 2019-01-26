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
import org.openjavacard.iso.AID;

import javax.smartcardio.CardException;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "gp-state",
        commandDescription = "GlobalPlatform: set state of the card or applets"
)
public class GPState extends GPCommand {

    @Parameter(
            names = "--confirm-irreversible",
            description = "Confirm an irreversible action"
    )
    private boolean confirmIrreversible = false;
    @Parameter(
            names = "--confirm-destruction",
            description = "Confirm a destructive action"
    )
    private boolean confirmDestruction = false;

    @Parameter(
            names = "--card-initialized",
            description = "Set card state to INITIALIZED (irreversible)"
    )
    private boolean cardInitialized;
    @Parameter(
            names = "--card-secured",
            description = "Set card state to SECURED (irreversible)"
    )
    private boolean cardSecured;
    @Parameter(
            names = "--card-lock",
            description = "Lock the card (must be SECURED)"
    )
    private boolean cardLock;
    @Parameter(
            names = "--card-unlock",
            description = "Unlock the card (must be LOCKED)"
    )
    private boolean cardUnlock;
    @Parameter(
            names = "--card-terminate",
            description = "Terminate the card (destructive)"
    )
    private boolean cardTerminate;

    @Parameter(
            names = "--applet-lock",
            description = "Lock the indicated applet"
    )
    private List<AID> appLock;
    @Parameter(
            names = "--applet-unlock",
            description = "Unlock the indicated applet"
    )
    private List<AID> appUnlock;

    public GPState(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        GPIssuerDomain isd = card.getIssuerDomain();

        if(cardInitialized) {
            os.println("Transitioning card to state INITIALIZED");
            checkIrreversible();
            isd.cardInitialized();
        }
        if(cardSecured) {
            os.println("Transitioning card to state SECURED");
            checkIrreversible();
            isd.cardSecured();
        }
        if(cardLock) {
            os.println("Locking card");
            isd.lockCard();
        }
        if(cardUnlock) {
            os.println("Unlocking card");
            isd.unlockCard();
        }
        if(cardTerminate) {
            os.println("Terminating card");
            checkDestruction();
            isd.terminateCard();
        }

        for(AID app: appLock) {
            os.println("Locking applet " + app);
            isd.lockApplet(app);
        }
        for(AID app: appUnlock) {
            os.println("Unlocking applet " + app);
            isd.unlockApplet(app);
        }
    }

    private void checkIrreversible() {
        if(!confirmIrreversible) {
            throw new Error("Irreversible action: must confirm with --confirm-irreversible");
        }
    }

    private void checkDestruction() {
        if(!confirmDestruction) {
            throw new Error("Destructive action: must confirm with --confirm-destruction");
        }
    }

}
