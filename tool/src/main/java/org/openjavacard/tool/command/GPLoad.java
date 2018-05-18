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
import com.beust.jcommander.Parameters;
import org.openjavacard.cap.file.CapFile;
import org.openjavacard.cap.file.CapFilePackage;
import org.openjavacard.cap.file.CapFileReader;
import org.openjavacard.gp.client.*;
import org.openjavacard.iso.AID;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Parameters(
        commandNames = "gp-load",
        commandDescription = "GlobalPlatform: load objects onto the card"
)
public class GPLoad extends GPCommand {

    @Parameter(
            description = "CAP files to load",
            required = true
    )
    List<File> files;

    @Parameter(
            names = "--reload",
            description = "Reload by deleting previous package"
    )
    boolean reload = false;

    @Parameter(
            names = "--lazy",
            description = "Only load if not present"
    )
    boolean lazy = false;

    public GPLoad(GPContext context) {
        super(context);
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public boolean getReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public boolean getLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        GPRegistry registry = card.getRegistry();

        // load all CAP files specified
        List<CapFile> capFiles = loadFiles(files);

        // iterate packages in reverse during prepare
        // so that they can be given in install-dep order
        // and still be deleted correctly during prepare
        List<CapFile> capFilesToPrepare = new ArrayList<>(capFiles);
        Collections.reverse(capFilesToPrepare);

        // prepare each package and check if it needs loading
        List<CapFile> capFilesToLoad = new ArrayList<>();
        for(CapFile capFile: capFilesToPrepare) {
            CapFilePackage pkg = capFile.getPackage();
            if(prepareOne(card, pkg.getPackageAID())) {
                capFilesToLoad.add(capFile);
            }
        }

        // prepare might have deleted packages, so update registry
        registry.update();

        // prepare was reverse-order, load the other way around
        Collections.reverse(capFilesToLoad);

        // load the packages that we have decided to load
        for(CapFile capFile: capFilesToLoad) {
            CapFilePackage pkg = capFile.getPackage();
            loadOne(card, pkg);
        }
    }

    private List<CapFile> loadFiles(List<File> files) {
        PrintStream os = System.out;
        ArrayList<CapFile> capFiles = new ArrayList<>();
        for(File file: files) {
            CapFile capFile;
            try {
                os.println("Reading file " + file);
                capFile = CapFileReader.readFile(file);
                CapFilePackage pkg = capFile.getPackage();
                os.println("  aid " + pkg.getPackageAID());
                os.println("  package " + pkg.getPackageName());
                os.println("  version " + pkg.getPackageVersion());
            } catch (Exception e) {
                throw new Error("Error loading CAP file", e);
            }
            capFiles.add(capFile);
        }
        return capFiles;
    }

    private boolean prepareOne(GPCard card, AID pkgAID) throws CardException {
        PrintStream os = System.out;
        boolean shouldLoad = true;
        GPRegistry registry = card.getRegistry();
        GPIssuerDomain issuer = card.getIssuerDomain();

        if(registry.hasPackage(pkgAID)) {
            if(reload) {
                os.println("Deleting old package " + pkgAID);
                issuer.deleteObject(pkgAID, true);
            } else {
                if(lazy) {
                    shouldLoad = false;
                } else {
                    throw new Error("Card already has package " + pkgAID);
                }
            }
        }

        return shouldLoad;
    }

    private void loadOne(GPCard card, CapFilePackage pkg) throws CardException {
        PrintStream os = System.out;
        GPIssuerDomain issuer = card.getIssuerDomain();

        os.println("Loading package " + pkg.getPackageAID());

        GPLoadFile loadFile;
        try {
            loadFile = GPLoadFile.generateCombinedLoadFile(pkg, 128);
        } catch (Exception e) {
            throw new Error("Error slicing CAP file", e);
        }

        issuer.loadFile(loadFile);

        os.println("Load complete");
    }

}
