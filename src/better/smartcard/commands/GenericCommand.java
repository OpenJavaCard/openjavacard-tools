package better.smartcard.commands;

import better.smartcard.generic.GenericCard;
import better.smartcard.generic.GenericContext;
import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;
import com.beust.jcommander.Parameter;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import java.io.PrintStream;

public abstract class GenericCommand implements Runnable {

    @Parameter(
            names = "--reader",
            description = "Reader to use for the operation"
    )
    protected String reader = null;

    GenericContext mContext;

    public GenericCommand(GenericContext context) {
        mContext = context;
    }

    public void run() {
        PrintStream os = System.out;

        GenericCard card = mContext.findSingleCard(reader);
        try {
            os.println("CONNECTING");
            card.connect();
            os.println("PERFORMING");
            performOperation(card);
        } catch (Exception e) {
            throw new Error("Error performing operation", e);
        }
    }

    protected void performOperation(GenericCard card) throws CardException {
    }

}
