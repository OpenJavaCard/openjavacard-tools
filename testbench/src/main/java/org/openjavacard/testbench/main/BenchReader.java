package org.openjavacard.testbench.main;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.util.ATRUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class BenchReader {

    private static final Logger LOG = LoggerFactory.getLogger(BenchReader.class.getName());

    private Bench mBench;
    private CardTerminal mTerminal;

    private ReaderThread mThread;

    public BenchReader(Bench bench, CardTerminal terminal) {
        mBench = bench;
        mTerminal = terminal;
        mThread = null;
    }

    public String getName() {
        return mTerminal.getName();
    }

    public CardTerminal getTerminal() {
        return mTerminal;
    }

    public boolean isCardPresent() {
        try {
            return mTerminal.isCardPresent();
        } catch (CardException e) {
            LOG.error("Error checking for card presence", e);
            return false;
        }
    }

    public void onCardInserted() {
        LOG.info("onCardInserted()");
        start();
    }

    public void onCardRemoved() {
        LOG.info("onCardRemoved()");
        stop();
    }

    private synchronized void start() {
        mThread = new ReaderThread();
        mThread.start();
    }

    private synchronized void stop() {
        if(mThread != null) {
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                LOG.info("Join interrupted", e);
            }
            mThread = null;
        }
    }

    private void runSession() {
        LOG.info("runSession()");
        GPCard card = new GPCard(mBench.getContext(), mTerminal);
        try {
            card.connect();
            LOG.info("Protocol " + card.getCard().getProtocol());
            LOG.info("ATR " + ATRUtil.toString(card.getCard().getATR()));
            LOG.info("ISD " + card.getISD().toString());
            card.getRegistry().update();
        } catch (CardException e) {
            LOG.error("Error connecting to card", e);
        }
    }

    private class ReaderThread extends Thread {
        ReaderThread() {
            setName(BenchReader.class.getSimpleName() + "(" + mTerminal.getName() + ")");
        }
        @Override
        public void run() {
            runSession();
        }
    }

}
