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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.openjavacard.cap.base.CapComponent;
import org.openjavacard.cap.base.CapPackage;
import org.openjavacard.cap.base.CapPackageReader;
import org.openjavacard.cap.file.CapFile;
import org.openjavacard.cap.file.CapFilePackage;
import org.openjavacard.cap.file.CapFileReader;
import org.openjavacard.jackson.OJCJacksonModule;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "cap-dump",
        commandDescription = "CAP: EXPERIMENTAL - Dump a CAP file"
)
public class CapDump implements Runnable {

    @Parameter(
            description = "CAP files to show information about",
            required = true
    )
    List<File> capFiles;

    @Override
    public void run() {
        PrintStream os = System.out;

        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(true);
        XmlMapper xmlMap = new XmlMapper();
        xmlMap.registerModule(new OJCJacksonModule());
        xmlMap.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        for (File file : capFiles) {
            os.println();

            CapFile capFile;
            try {
                os.println("Reading CAP file " + file);
                capFile = CapFileReader.readFile(file);
            } catch (Exception ex) {
                throw new Error("Error reading CAP file", ex);
            }

            CapFilePackage capFilePkg = capFile.getPackage();
            CapPackageReader capReader = new CapPackageReader();
            CapPackage capPkg;
            try {
                capPkg = capReader.read(capFilePkg);
            } catch (IOException e) {
                throw new Error("Error parsing CAP file", e);
            }

            try {
                os.println(xmlMap.writeValueAsString(capPkg));
            } catch (IOException e) {
                throw new Error("Error dumping CAP component", e);
            }
        }
    }

}
