package better.smartcard.commands;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import java.io.PrintStream;

public class CardCommand extends GPCommand {

    public CardCommand(GPContext context) {
        super(context);
    }

    @Override
    public void run() {
        PrintStream os = System.out;

        GPCard gpCard = mContext.findSingleCard(reader);
        try {
            gpCard.detect();
            Card card = gpCard.getCard();
            performOperation(card);
        } catch (Exception e) {
            throw new Error("Error performing operation", e);
        }
    }

    protected void performOperation(Card card) throws CardException {
    }

}
