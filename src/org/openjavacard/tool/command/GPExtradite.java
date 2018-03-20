package org.openjavacard.tool.command;

import org.openjavacard.gp.GPCard;
import org.openjavacard.gp.GPContext;
import org.openjavacard.iso.AID;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.PrintStream;

@Parameters(
        commandNames = "gp-extradite",
        commandDescription = "GlobalPlatform: extradite an application to an SD"
)
public class GPExtradite extends GPCommand {

    @Parameter(
            names = "--domain",
            description = "Domain to extradite to",
            required = true
    )
    AID domainAID;

    @Parameter(
            names = "--applet",
            description = "Applet to extradite",
            required = true
    )
    AID appletAID;

    public GPExtradite(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) {
        PrintStream os = System.out;

        os.println("Extraditing applet " + appletAID + " to domain " + domainAID);
    }

}
