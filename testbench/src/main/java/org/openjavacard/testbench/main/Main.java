package org.openjavacard.testbench.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.openjavacard.testbench.core.BenchMode;

import java.util.List;

public class Main {

    @Parameter(
            names = {"--help", "-h"},
            help = true,
            description = "Show help"
    )
    private boolean showHelp;

    @Parameter(
            names = "--all-readers",
            description = "Use all available readers"
    )
    private boolean allReaders;

    @Parameter(
            names = "--reader",
            description = "Use the specified reader"
    )
    private List<String> reader;

    @Parameter(
            names = "--mode",
            description = "Specify testbench mode"
    )
    private BenchMode mode = BenchMode.ONCE;

    private void run(JCommander jc) {
        if(showHelp) {
            jc.usage();
            return;
        }
    }

    public static void main(String[] arguments) {
        Main main = new Main();
        JCommander jc = JCommander.newBuilder()
                .addObject(main)
                .build();
        jc.parse(arguments);
        main.run(jc);
    }

}
