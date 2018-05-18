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
import org.openjavacard.cap.base.CapPackageReader;
import org.openjavacard.cap.file.CapFile;
import org.openjavacard.cap.file.CapFilePackage;
import org.openjavacard.cap.file.CapFileReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "cap-read",
        commandDescription = "CAP: EXPERIMENTAL - Read a CAP file"
)
public class CapRead implements Runnable {

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
                cap = CapFileReader.readFile(file);
            } catch (Exception ex) {
                throw new Error("Exception reading CAP file", ex);
            }

            CapFilePackage pkg = cap.getPackage();
            CapPackageReader capPkg = new CapPackageReader();
            try {
                capPkg.read(pkg);
            } catch (IOException e) {
                throw new Error("Error reading CAP file", e);
            }
        }
    }

}
