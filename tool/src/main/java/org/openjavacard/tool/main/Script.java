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

package org.openjavacard.tool.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Parameters(
        commandNames = "script",
        commandDescription = "Run commands from a script"
)
public class Script implements Runnable {

    @Parameter(
            description = "Scripts to execute",
            required = true
    )
    List<File> scripts;

    private final Tool mTool;

    public Script(Tool tool) {
        mTool = tool;
    }

    @Override
    public void run() {
        try {
            for(File script: scripts) {
                FileReader fr = new FileReader(script);
                BufferedReader br = new BufferedReader(fr);
                for (String line; (line = br.readLine()) != null; ) {
                    // ignore comments
                    if(line.startsWith("#")) {
                        continue;
                    }
                    // tokenize the line
                    String[] tokens = line.split("\\s+");
                    // process command if there is one
                    if (tokens.length > 0) {
                        // build a fresh commander
                        JCommander jc = mTool.makeCommander();
                        jc.parse(tokens);
                        String command = jc.getParsedCommand();
                        if (command != null) {
                            JCommander commandJc = jc.getCommands().get(command);
                            mTool.runCommand(command, commandJc);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
