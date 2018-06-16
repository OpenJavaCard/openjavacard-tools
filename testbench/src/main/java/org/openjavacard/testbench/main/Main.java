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
