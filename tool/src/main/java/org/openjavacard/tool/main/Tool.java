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
import org.openjavacard.tool.command.*;
import org.openjavacard.tool.converter.ConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Tool {

    private static final Logger LOG = LoggerFactory.getLogger(Tool.class);

    @Parameter(
            names = {"--help", "-h"},
            help = true,
            description = "Show available commands"
    )
    boolean help = false;

    private final GPContext mContext;

    Tool() {
        mContext = new GPContext();
    }

    void run(String[] arguments) {
        // build commander
        JCommander jc = makeCommander();
        // add tool object
        jc.addObject(this);
        // core commands
        jc.addCommand(new Help(jc));
        jc.addCommand(new Script(this));
        // parse the command
        jc.parse(arguments);
        // execute the command
        runMainCommand(jc);
    }

    private void runMainCommand(JCommander jc) {
        // get defined commands
        Map<String, JCommander> commands = jc.getCommands();
        // get the command
        String commandName = jc.getParsedCommand();
        // help if there was no command
        if (commandName == null) {
            help = true;
        }
        // run help if appropriate
        JCommander helpCommand = commands.get("help");
        if (help || commandName == "help") {
            runCommand("help", helpCommand);
            return;
        }
        // run the command
        JCommander command = commands.get(commandName);
        runCommand(commandName, command);
    }

    void runCommand(String name, JCommander jc) {
        LOG.debug("running command " + name);
        for (Object o : jc.getObjects()) {
            if (o instanceof Runnable) {
                Runnable r = (Runnable) o;
                r.run();
            }
        }
    }

    JCommander makeCommander() {
        JCommander jc = new JCommander();
        jc.addConverterFactory(new ConverterFactory());

        jc.addCommand(new AIDInfo());
        jc.addCommand(new AIDNow());

        jc.addCommand(new GenericAPDU(mContext));

        jc.addCommand(new CapInfo());
        jc.addCommand(new CapSize());
        jc.addCommand(new CapDump());

        jc.addCommand(new GPInfo(mContext));
        jc.addCommand(new GPList(mContext));
        jc.addCommand(new GPLoad(mContext));
        jc.addCommand(new GPInstall(mContext));
        jc.addCommand(new GPDelete(mContext));
        jc.addCommand(new GPExtradite(mContext));
        jc.addCommand(new GPState(mContext));
        jc.addCommand(new GPIdentity(mContext));
        jc.addCommand(new GPKeys(mContext));

        jc.addCommand(new ScanName(mContext));

        return jc;
    }

}
