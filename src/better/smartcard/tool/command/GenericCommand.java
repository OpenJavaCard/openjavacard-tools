package better.smartcard.tool.command;

import better.smartcard.generic.GenericCard;
import better.smartcard.generic.GenericContext;
import better.smartcard.iso.AID;
import better.smartcard.util.ATRUtil;
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

    @Parameter(
            names = "--select",
            description = "Applet to select before performing the operation"
    )
    protected AID select = null;

    GenericContext mContext;
    GenericCard    mCard;

    public GenericCommand(GenericContext context) {
        mContext = context;
    }

    public void run() {
        PrintStream os = System.out;

        mCard = mContext.findSingleCard(reader);
        try {
            mCard.connect();
            Card card = mCard.getCard();
            os.println("CONNECTED " + card.getProtocol() + " ATR=" + ATRUtil.toString(card.getATR()));
            if (select != null) {
                os.println("SELECT " + select);
                mCard.performSelectByName(select.getBytes(), true);
            }
            performOperation(mCard);

            mCard.disconnect();
        } catch (Exception e) {
            throw new Error("Error performing operation", e);
        }
    }

    protected void performOperation(GenericCard card) throws CardException {
    }

}
