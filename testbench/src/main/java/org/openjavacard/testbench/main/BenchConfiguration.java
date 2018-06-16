package org.openjavacard.testbench.main;

import com.beust.jcommander.Parameter;

import java.util.List;

public class BenchConfiguration {

    @Parameter(
            names = "--all-readers",
            description = "Use all available readers"
    )
    boolean allReaders;

    @Parameter(
            names = "--reader",
            description = "Use the specified reader"
    )
    List<String> reader;

    @Parameter(
            names = "--mode",
            description = "Specify testbench mode"
    )
    BenchMode mode = BenchMode.ONCE;

}
