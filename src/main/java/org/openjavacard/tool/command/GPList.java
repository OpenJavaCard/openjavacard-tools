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
import org.openjavacard.gp.GPCard;
import org.openjavacard.gp.GPContext;
import org.openjavacard.gp.GPRegistry;

import javax.smartcardio.CardException;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "gp-list",
        commandDescription = "GlobalPlatform: list objects on card"
)
public class GPList extends GPCommand {

    @Parameter(
            names = {"-a", "--all"},
            description = "Show all registry entries"
    )
    boolean showAll;

    @Parameter(
            names = {"--sd"},
            description = "Show issuer security domain (ISD)"
    )
    boolean showSD;

    @Parameter(
            names = {"--elf"},
            description = "Show executable files (ELF)"
    )
    boolean showELF;

    @Parameter(
            names = {"--app"},
            description = "Show applets (APP)"
    )
    boolean showAPP;

    public GPList(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        os.println();
        if (!showAll) {
            if (showSD || showELF || showAPP) {
                showAll = false;
            } else {
                showAll = true;
            }
        }
        if (showAll) {
            showSD = true;
            showELF = true;
            showAPP = true;
        }
        printRegistry(os, card, showSD, showELF, showAPP);
    }

    public static void printRegistry(PrintStream os, GPCard card,
                                     boolean showSD, boolean showELF, boolean showAPP) {
        GPRegistry registry = card.getRegistry();

        if (showSD) {
            GPRegistry.ISDEntry isd = registry.getISD();
            if (isd == null) {
                os.println("NO ISD FOUND!");
            } else {
                os.println(isd.toVerboseString());
            }
            os.println();
            List<GPRegistry.AppEntry> ssds = registry.getAllSSDs();
            if (!ssds.isEmpty()) {
                for (GPRegistry.AppEntry ssd : ssds) {
                    os.println(ssd.toVerboseString());
                    os.println();
                }
            }
        }

        if (showELF) {
            List<GPRegistry.ELFEntry> elfs = registry.getAllELFs();
            if (!elfs.isEmpty()) {
                for (GPRegistry.ELFEntry elf : elfs) {
                    os.println(elf.toVerboseString());
                    os.println();
                }
            }
        }

        if (showAPP) {
            List<GPRegistry.AppEntry> apps = registry.getAllApps();
            if (!apps.isEmpty()) {
                for (GPRegistry.AppEntry app : apps) {
                    os.println(app.toVerboseString());
                    os.println();
                }
            }
        }
    }

}
