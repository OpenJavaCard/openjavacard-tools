package better.smartcard.commands;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;
import better.smartcard.gp.protocol.GPCardData;
import better.smartcard.gp.protocol.GPKeyInfo;
import better.smartcard.gp.protocol.GPLifeCycle;
import better.smartcard.gp.scp.SCPParameters;
import better.smartcard.util.ATRUtil;
import com.beust.jcommander.Parameters;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import java.io.PrintStream;

@Parameters(
        commandNames = "gp-info",
        commandDescription = "GlobalPlatform: show information about card"
)
public class GPInfo extends GPCommand {

    public GPInfo(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) {
        PrintStream os = System.out;

        os.println();

        CardTerminal terminal = card.getTerminal();
        os.println("Terminal information:");
        os.println("  Name \"" + terminal.getName() + "\"");
        os.println("  Class " + terminal.getClass().getName());
        os.println();

        Card scard = card.getCard();
        os.println("Card information:");
        os.println("  Protocol " + scard.getProtocol());
        os.println("  ATR " + ATRUtil.toString(scard.getATR()));
        os.println("  Class " + scard.getClass().getName());
        os.println("  ISD " + card.getCardISD());
        String identifier = card.getCardIdentifier();
        if(identifier != null) {
            os.println("  Identifier: " + identifier);
        }
        os.println();

        GPLifeCycle lifeCycle = card.getCardLifeCycle();
        if (lifeCycle == null) {
            os.println("Card did not provide life cycle data");
        } else {
            os.println(lifeCycle.toString());
        }
        os.println();

        GPCardData cardData = card.getCardData();
        if (cardData == null) {
            os.println("Card did not provide card data");
        } else {
            os.println(cardData.toString());
        }
        os.println();

        SCPParameters scpParameters = card.getProtocol();
        if (scpParameters == null) {
            os.println("No SCP parameters");
        } else {
            os.println(scpParameters.toVerboseString());
        }
        os.println();

        GPKeyInfo keyInfo = card.getCardKeyInfo();
        if (keyInfo == null) {
            os.println("Card did not provide a key info template");
        } else {
            os.println(keyInfo.toString());
        }
        os.println();

        GPList.printRegistry(os, card, true, true, true, true);
    }

}
