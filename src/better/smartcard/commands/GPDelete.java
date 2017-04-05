package better.smartcard.commands;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;
import better.smartcard.gp.GPIssuerDomain;
import better.smartcard.util.AID;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "gp-delete",
        commandDescription = "GlobalPlatform: delete applets or packages from the card"
)
public class GPDelete extends GPCommand {

    @Parameter(
            names = "--related",
            description = "Delete dependent modules"
    )
    private boolean related = false;

    @Parameter(
            description = "AIDs of objects to delete",
            required = true
    )
    private List<AID> objectAIDs = null;

    public GPDelete(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        GPIssuerDomain isd = card.getIssuerDomain();
        for (AID aid : objectAIDs) {
            os.println("Deleting object " + aid + (related?" and related":""));
            isd.deleteObject(aid, related);
        }
    }

}
