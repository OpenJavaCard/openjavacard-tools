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

import org.openjavacard.gp.GPCard;
import org.openjavacard.gp.GPContext;
import org.openjavacard.gp.GPIssuerDomain;
import org.openjavacard.gp.GPRegistry;
import org.openjavacard.gp.protocol.GPPrivilege;
import org.openjavacard.iso.AID;
import org.openjavacard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Parameters(
        commandNames = "gp-install",
        commandDescription = "GlobalPlatform: install an applet"
)
public class GPInstall extends GPCommand {

    @Parameter(
            names = "--module",
            description = "Module to install",
            required = true
    )
    AID moduleAID;

    @Parameter(
            names = "--package",
            description = "Package to install",
            required = false
    )
    AID packageAID;

    @Parameter(
            names = "--aid",
            description = "Applet AID to install as",
            required = false
    )
    AID appletAID;

    @Parameter(
            names = "--parameters",
            description = "Pass the given install parameters to the applet",
            required = false
    )
    String appletParameters = "";

    @Parameter(
            names = "--privilege",
            description = "Grant the given privilege to the applet",
            required = false
    )
    List<GPPrivilege> appletPrivileges = new ArrayList<>();

    @Parameter(
            names = "--cap-file",
            description = "CAP file to load",
            required = false
    )
    List<File> capFiles;

    @Parameter(
            names = "--reload",
            description = "Reload provided packages, replacing old versions"
    )
    boolean reload;

    @Parameter(
            names = "--reinstall",
            description = "Reinstall the applet, replacing old instances"
    )
    boolean reinstall;

    public GPInstall(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        GPRegistry registry = card.getRegistry();
        GPIssuerDomain issuer = card.getIssuerDomain();

        byte[] appPrivs = GPPrivilege.toBytes(appletPrivileges);
        byte[] appParams = HexUtil.hexToBytes(appletParameters);

        // reload implies reinstall
        if(reload) {
            reinstall = true;
        }

        // check if we need to load
        boolean mustLoad = false;
        if(capFiles != null && !capFiles.isEmpty()) {
            mustLoad = true;
        }

        // determine install parameters
        AID pkgAID = packageAID;
        AID modAID = moduleAID;
        AID appAID = appletAID;

        // default for the app AID
        if(appAID == null) {
            appAID = modAID;
        }

        // delete old applet
        if(registry.hasApplet(appAID)) {
            if(reinstall) {
                os.println("Deleting old applet " + appAID);
                issuer.deleteObject(appAID);
                registry.update();
            } else {
                throw new Error("Card already has applet " + appAID);
            }
        }

        // load packages
        if(mustLoad) {
            os.println("Loading provided packages");
            GPLoad load = new GPLoad(context);
            load.setFiles(capFiles);
            load.setReload(reload);
            load.performOperation(context, card);
            registry.update();
        }

        // determine and check the package id
        if(pkgAID == null) {
            // determine package from module using registry
            os.println("Searching for module " + modAID);
            GPRegistry.ELFEntry elf = registry.findPackageForModule(modAID);
            if(elf == null) {
                throw new Error("Could not find module " + modAID + " on card");
            }
            pkgAID = elf.getAID();
            os.println("Found module in package " + pkgAID);
        } else {
            // otherwise the module must be present at this point
            if(!registry.hasPackage(pkgAID)) {
                throw new Error("Card does not have package " + pkgAID);
            }
        }

        // print major parameters
        os.println("Installing applet " + appAID);
        os.println("  package " + pkgAID);
        os.println("  module " + modAID);
        os.println("  privileges " + HexUtil.bytesToHex(appPrivs));
        os.println("  parameters " + HexUtil.bytesToHex(appParams));
        os.println();

        // perform the installation
        issuer.installApplet(pkgAID, modAID, appAID, appPrivs, appParams);

        // happy happy joy joy
        os.println("Installation complete");
    }

}
