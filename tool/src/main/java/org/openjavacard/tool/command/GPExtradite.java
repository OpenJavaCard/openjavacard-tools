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
import org.openjavacard.iso.AID;

import java.io.PrintStream;

@Parameters(
        commandNames = "gp-extradite",
        commandDescription = "GlobalPlatform: extradite an application to an SD"
)
public class GPExtradite extends GPCommand {

    @Parameter(
            names = "--domain",
            description = "Domain to extradite to",
            required = true
    )
    private AID domainAID;

    @Parameter(
            names = "--applet",
            description = "Applet to extradite",
            required = true
    )
    private AID appletAID;

    public GPExtradite(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) {
        PrintStream os = System.out;

        os.println("Extraditing applet " + appletAID + " to domain " + domainAID);
    }

}
