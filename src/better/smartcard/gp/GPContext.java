package better.smartcard.gp;

import better.smartcard.generic.GenericContext;
import better.smartcard.util.AID;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class GPContext extends GenericContext {

    public GPContext() {
        LOG = LoggerFactory.getLogger(GPContext.class);
    }

    public GPCard findSingleGPCard(String prefix) {
        return findSingleGPCard(prefix, null);
    }

    public GPCard findSingleGPCard(String prefix, AID sd) {
        LOG.debug("findSingleCard()");
        GPCard card;
        CardTerminal terminal = findSingleTerminal(prefix);
        try {
            // check card presence
            if (!terminal.isCardPresent()) {
                throw new Error("No card present in terminal");
            }
            // create GP client
            card = new GPCard(this, terminal);
            // tell the client if we know the SD AID
            if(sd != null) {
                card.setISD(sd);
            }
            // detect GP applet
            //boolean detected = card.detect();
            //if (!detected) {
            //    throw new Error("Could not find a GlobalPlatform applet on the card");
            //}
        } catch (CardException e) {
            throw new Error("Error detecting card", e);
        }
        return card;
    }

}
