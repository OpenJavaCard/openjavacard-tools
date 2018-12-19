package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.iso.AID;

import java.io.PrintStream;

@Parameters(
        commandNames = "aid-info",
        commandDescription = "AID: Show information about an AID"
)
public class AIDInfo implements Runnable {

    @Parameter(
            required = true,
            description = "AID"
    )
    AID aid;

    @Override
    public void run() {
        PrintStream os = System.out;

        os.println("AID: " + aid);

        org.openjavacard.iso.AIDInfo info =
            org.openjavacard.iso.AIDInfo.get(aid);

        if(info != null) {

        }
    }

}
