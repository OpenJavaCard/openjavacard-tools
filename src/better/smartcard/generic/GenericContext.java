package better.smartcard.generic;

import better.smartcard.iso.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import java.util.ArrayList;
import java.util.List;

public class GenericContext {

    protected Logger LOG;

    public GenericContext() {
        LOG = LoggerFactory.getLogger(GenericContext.class);
    }

    public GenericCard findSingleCard(String prefix) {
        return findSingleCard(prefix, null);
    }

    public GenericCard findSingleCard(String prefix, AID application) {
        LOG.debug("findSingleCard()");
        GenericCard card;
        CardTerminal terminal = findSingleTerminal(prefix);
        try {
            // check card presence
            if (!terminal.isCardPresent()) {
                throw new Error("No card present in terminal");
            }
            // create the client
            card = new GenericCard(this, terminal);
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
