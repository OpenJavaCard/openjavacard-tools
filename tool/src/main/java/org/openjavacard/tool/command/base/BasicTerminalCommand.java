package org.openjavacard.tool.command.base;

import com.beust.jcommander.Parameter;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import java.util.ArrayList;
import java.util.List;

public abstract class BasicTerminalCommand extends BasicCommand {

    @Parameter(
            names = "--terminal", order = 100,
            description = "Terminal to use for the operation (unique prefix)"
    )
    protected String aTerminal = null;

    protected CardTerminal mTerminal = null;

    @Override
    protected void prepare() throws Exception {
        super.prepare();
        mTerminal = findTerminal(aTerminal);
    }

    private CardTerminal findTerminal(String prefix) {
        LOG.trace("findTerminal()");
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

    private List<CardTerminal> findTerminals(String prefix) {
        LOG.trace("findTerminals()");
        ArrayList<CardTerminal> found = new ArrayList<>();
        TerminalFactory tf = TerminalFactory.getDefault();
        CardTerminals ts = tf.terminals();
        try {
            List<CardTerminal> terminals = ts.list();
            for (CardTerminal terminal : terminals) {
                String name = terminal.getName();
                LOG.trace("terminal \"" + name + "\"");
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
