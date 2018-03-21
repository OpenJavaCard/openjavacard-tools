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

import org.openjavacard.gp.GPContext;
import org.openjavacard.tool.main.Main;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.*;
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

    GPContext mContext;

    public Script(GPContext context) {
        mContext = context;
    }

    @Override
    public void run() {
        PrintStream os = System.out;
        try {
            for(File script: scripts) {
                FileReader fr = new FileReader(script);
                BufferedReader br = new BufferedReader(fr);
                for (String line; (line = br.readLine()) != null; ) {
                    if(!line.startsWith("#")) {
                        String[] toks = line.split("\\s+");
                        if (toks.length > 0) {
                            JCommander jc = Main.makeCommander(mContext);
                            jc.parse(toks);
                            String scmd = jc.getParsedCommand();
                            if (scmd != null) {
                                JCommander jcmd = jc.getCommands().get(scmd);
                                for (Object o : jcmd.getObjects()) {
                                    if (o instanceof Runnable) {
                                        ((Runnable) o).run();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
