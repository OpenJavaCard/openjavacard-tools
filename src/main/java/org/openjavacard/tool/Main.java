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

package org.openjavacard.tool;

import org.openjavacard.tool.command.*;
import org.openjavacard.gp.GPContext;
import org.openjavacard.tool.converter.ConverterFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.Map;

public class Main {

    @Parameter(
            names = {"--help", "-h"},
            help = true,
            description = "Show available commands"
    )
    boolean help = false;

    @Parameter(
            names = {"--log", "-l"},
            description = "Set log level for debugging (info, debug, trace)"
    )
    String log = null;

    public void execute(JCommander jc) {
        Map<String, JCommander> commands = jc.getCommands();

        if (log != null) {
            /*
            Level level = Level.valueOf(log);
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(level);
            */
        }

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
