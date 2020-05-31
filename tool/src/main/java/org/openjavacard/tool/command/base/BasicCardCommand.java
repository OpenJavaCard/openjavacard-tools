package org.openjavacard.tool.command.base;

import com.beust.jcommander.Parameter;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;

public abstract class BasicCardCommand extends BasicTerminalCommand {

    @Parameter(
            names = "--wait-for-card", order = 100,
            description = "Wait for card presence before start"
    )
    protected boolean aWaitForCard;

    protected Card mCard;
    protected CardChannel mBasicChannel;

    @Override
    protected void prepare() throws Exception {
        // super will prepare the terminal
        super.prepare();
        // wait for card presence if requested
        if(aWaitForCard) {
            while(!mTerminal.isCardPresent()) {
                LOG.debug("Waiting for card...");
                mTerminal.waitForCardPresent(5000);
            }
        }
        // final check for card presence
        if(!mTerminal.isCardPresent()) {
            throw new Error("No card present in terminal");
        }
        // connect to the card
        LOG.debug("Connecting to card");
        mCard = mTerminal.connect("*");
        mBasicChannel = mCard.getBasicChannel();
    }

}
