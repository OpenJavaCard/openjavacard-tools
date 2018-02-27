package better.smartcard.gp;

import better.smartcard.util.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import java.util.ArrayList;
import java.util.List;

public class GPContext {

    private static final Logger LOG = LoggerFactory.getLogger(GPContext.class);

    public GPContext() {
    }

    public GPCard findSingleCard(String prefix) {
        return findSingleCard(prefix, null);
    }

    public GPCard findSingleCard(String prefix, AID sd) {
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

    public CardTerminal findSingleTerminal(String prefix) {
        LOG.debug("findSingleTerminal()");
        List<CardTerminal> terminals = findTerminals(prefix);
        if (terminals.isEmpty()) {
            throw new Error("No terminals found");
        } else if (terminals.size() > 1) {
            if (prefix == null) {
                throw new Error("More than one terminal found");
            } else {
                throw new Error("More than one terminal found matching \"" + prefix + "\"");
            }
        }
        return terminals.get(0);
    }

    public List<CardTerminal> findTerminals(String prefix) {
        LOG.debug("findTerminals()");
        ArrayList<CardTerminal> found = new ArrayList<>();
        TerminalFactory tf = TerminalFactory.getDefault();
        CardTerminals ts = tf.terminals();
        try {
            List<CardTerminal> terminals = ts.list(CardTerminals.State.CARD_PRESENT);
            for (CardTerminal terminal : terminals) {
                String name = terminal.getName();
                LOG.trace("checking terminal \"" + name + "\"");
                if (prefix == null || name.startsWith(prefix)) {
                    found.add(terminal);
                }
            }
        } catch (CardException e) {
            throw new Error("Error detecting terminals", e);
        }
        return found;
    }

}
