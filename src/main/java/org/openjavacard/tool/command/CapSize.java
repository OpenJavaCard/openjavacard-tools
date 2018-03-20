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

import org.openjavacard.cap.*;
import org.openjavacard.gp.GPLoadFile;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "cap-size",
        commandDescription = "JavaCard CAP: Show size of a cap file, predict on-card size"
)
public class CapSize implements Runnable {

    @Parameter(
            description = "CAP files to show information about",
            required = true
    )
    List<File> capFiles;

    @Override
    public void run() {
        PrintStream os = System.out;

        for (File file : capFiles) {
            os.println();

            CapFile cap;
            try {
                os.println("Reading CAP file " + file);
                CapReader reader = new CapReader(file);
                cap = reader.open();
            } catch (Exception ex) {
                throw new Error("Exception reading CAP file", ex);
            }

            CapPackage pkg;
            GPLoadFile loadFile;
            try {
                pkg = cap.getPackages().get(0);
                loadFile = pkg.generateCombinedLoadFile(128);
            } catch (Exception e) {
                throw new Error("Could not generate load file", e);
            }

            os.println();
            List<CapComponent> components = pkg.getLoadComponents();
            for(CapComponent com : components) {
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
