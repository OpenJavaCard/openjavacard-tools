package better.smartcard.tool.command;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;

@Parameters(
        commandNames = "gp-identity",
        commandDescription = "GlobalPlatform: set card identity"
)
public class GPIdentity extends GPCommand {

    @Parameter(
            names = "--new-iin",
            description = "New Issuer Identification Number (IIN)"
    )
    byte[] iin;

    @Parameter(
            names = "--new-cin",
            description = "New Card Identification Number (CIN)"
    )
    byte[] cin;

    @Parameter(
            names = "--new-isd",
            description = "New AID for the ISD of the card"
    )
    byte[] isd;

    public GPIdentity(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        card.getIssuerDomain().changeIdentity(iin, cin, isd);
    }

}
