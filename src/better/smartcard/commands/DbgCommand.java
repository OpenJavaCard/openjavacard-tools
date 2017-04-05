package better.smartcard.commands;

import better.smartcard.debug.DebugCard;
import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import java.io.PrintStream;

public class DbgCommand extends GPCommand {

    public DbgCommand(GPContext context) {
        super(context);
    }

    @Override
    public void run() {
        PrintStream os = System.out;

        GPCard gpCard = mContext.findSingleCard(reader);
        try {
            gpCard.detect();
            Card card = gpCard.getCard();
            DebugCard dbgCard = new DebugCard(card.getBasicChannel());
            performOperation(dbgCard);
        } catch (Exception e) {
            throw new Error("Error performing operation", e);
        }
    }

    protected void performOperation(DebugCard card) throws CardException {
    }

}
