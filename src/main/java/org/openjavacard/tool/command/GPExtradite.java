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
    AID domainAID;

    @Parameter(
            names = "--applet",
            description = "Applet to extradite",
            required = true
    )
    AID appletAID;

    public GPExtradite(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) {
        PrintStream os = System.out;

        os.println("Extraditing applet " + appletAID + " to domain " + domainAID);
    }

}
