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

package org.openjavacard.testbench.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Main {

    @Parameter(
            names = {"--help", "-h"},
            help = true,
            description = "Show help"
    )
    boolean showHelp;

    public static void main(String[] arguments) {
        // object for help handling
        Main main = new Main();
        // create configuration object
        BenchConfiguration config = new BenchConfiguration();
        // create commander
        JCommander jc = JCommander.newBuilder()
                .addObject(main)
                .addObject(config)
                .build();
        // parse arguments
        jc.parse(arguments);
        // check for help
        if(main.showHelp) {
            jc.usage();
            return;
        }
        // execute bench
        Bench bench = new Bench(config);
        bench.configure();
        bench.execute();
    }

}
