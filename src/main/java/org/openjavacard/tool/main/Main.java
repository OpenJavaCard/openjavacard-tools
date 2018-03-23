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
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.tool.command.CapInfo;
import org.openjavacard.tool.command.CapSize;
import org.openjavacard.tool.command.GPDelete;
import org.openjavacard.tool.command.GPExtradite;
import org.openjavacard.tool.command.GPIdentity;
import org.openjavacard.tool.command.GPInfo;
import org.openjavacard.tool.command.GPInstall;
import org.openjavacard.tool.command.GPKeys;
import org.openjavacard.tool.command.GPList;
import org.openjavacard.tool.command.GPLoad;
import org.openjavacard.tool.command.GPState;
import org.openjavacard.tool.command.GenericAPDU;
import org.openjavacard.tool.command.GenericScanName;
import org.openjavacard.tool.command.Script;
import org.openjavacard.tool.converter.ConverterFactory;

import java.util.Map;

public class Main {

    @Parameter(
            names = {"--help", "-h"},
            help = true,
            description = "Show available commands"
    )
    boolean help = false;

    public void execute(JCommander jc) {
        Map<String, JCommander> commands = jc.getCommands();

        String commandName = jc.getParsedCommand();
        if (commandName == null) {
            help = true;
        }

        JCommander helpCommand = commands.get("help");
        if (help || commandName == "help") {
            runCommand(helpCommand);
            return;
        }

        JCommander command = commands.get(commandName);
        runCommand(command);
    }

    private void runCommand(JCommander jc) {
        for (Object o : jc.getObjects()) {
            if (o instanceof Runnable) {
                Runnable r = (Runnable) o;
                r.run();
            }
        }
    }

    public static JCommander makeCommander(GPContext gpContext) {
        JCommander jc = new JCommander();
        jc.addConverterFactory(new ConverterFactory());

        jc.addCommand(new CapInfo());
        jc.addCommand(new CapSize());

        jc.addCommand(new GenericAPDU(gpContext));
        jc.addCommand(new GenericScanName(gpContext));

        jc.addCommand(new GPInfo(gpContext));
        jc.addCommand(new GPList(gpContext));
        jc.addCommand(new GPLoad(gpContext));
        jc.addCommand(new GPInstall(gpContext));
        jc.addCommand(new GPDelete(gpContext));
        jc.addCommand(new GPExtradite(gpContext));
        jc.addCommand(new GPState(gpContext));
        jc.addCommand(new GPIdentity(gpContext));
        jc.addCommand(new GPKeys(gpContext));

        return jc;
    }

    public static void main(String[] arguments) {
        GPContext gpContext = new GPContext();
        JCommander jc = makeCommander(gpContext);
        Main main = new Main();
        Help help = new Help(jc);
        Script script = new Script(gpContext);
        jc.addObject(main);
        jc.addCommand(help);
        jc.addCommand(script);
        jc.parse(arguments);
        main.execute(jc);
    }

}
