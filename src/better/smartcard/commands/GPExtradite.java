package better.smartcard.commands;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;
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
    String domainAID;

    @Parameter(
            names = "--applet",
            description = "Applet to extradite",
            required = true
    )
    String appletAID;

    public GPExtradite(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) {
        PrintStream os = System.out;

        os.println("Extraditing applet " + appletAID + " to domain " + domainAID);
    }

}
