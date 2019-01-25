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
import org.openjavacard.cap.file.CapFile;
import org.openjavacard.cap.file.CapFileComponent;
import org.openjavacard.cap.file.CapFilePackage;
import org.openjavacard.cap.file.CapFileReader;
import org.openjavacard.gp.client.GPLoadFile;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "cap-size",
        commandDescription = "CAP: Show size of a cap file, including load size"
)
public class CapSize implements Runnable {

    @Parameter(
            description = "CAP files to show information about",
            required = true
    )
    private List<File> capFiles;

    @Override
    public void run() {
        PrintStream os = System.out;

        for (File file : capFiles) {
            os.println();

            CapFile cap;
            try {
                os.println("Reading CAP file " + file);
                cap = CapFileReader.readFile(file);
            } catch (Exception ex) {
                throw new Error("Exception reading CAP file", ex);
            }

            CapFilePackage pkg;
            GPLoadFile loadFile;
            try {
                pkg = cap.getPackages().get(0);
                loadFile = GPLoadFile.generateCombinedLoadFile(pkg, 128);
            } catch (Exception e) {
                throw new Error("Could not generate load file", e);
            }

            os.println();
            List<CapFileComponent> components = pkg.getLoadComponents();
            for(CapFileComponent com : components) {
                os.println("  Component " + com.getFilename());
                os.println("    Type: " + com.getName());
                os.println("    Size: " + com.getSize());
            }

            os.println();
            os.println("Load size: " + loadFile.getTotalSize()
                    + " (" + loadFile.getNumBlocks() + " blocks of "
                    + loadFile.getBlockSize() + " bytes)");
        }
    }

}
