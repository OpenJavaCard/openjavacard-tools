/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.openjavacard.testbench.main;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.util.ATRUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class BenchReader {

    private static final Logger LOG = LoggerFactory.getLogger(BenchReader.class.getName());

    private final Bench mBench;
    private final CardTerminal mTerminal;

    private PollingThread mThread;

    public BenchReader(Bench bench, CardTerminal terminal) {
        mBench = bench;
        mTerminal = terminal;
    }

    public String getName() {
        return mTerminal.getName();
    }

    public CardTerminal getTerminal() {
        return mTerminal;
    }

    public void runSession() {
        LOG.info("runSession()");
        try {
            if(!mTerminal.isCardPresent()) {
                LOG.info("no card present");
            } else {
                GPContext context = mBench.getContext();
                GPCard card = new GPCard(context, mTerminal);
                LOG.info("connecting to card");
                card.connect();
                LOG.info("Protocol " + card.getCard().getProtocol());
                LOG.info("ATR " + ATRUtil.toString(card.getCard().getATR()));
                LOG.info("ISD " + card.getISD().toString());
                card.getRegistry().update();
                card.disconnect();
            }
        } catch (CardException e) {
            LOG.error("Error connecting to card", e);
        }
    }

    public synchronized void startPolling() {
        LOG.info("startPolling()");
        if(mThread == null) {
            mThread = new PollingThread(this);
            mThread.start();
        }
    }

    public synchronized void stopPolling() {
        LOG.info("stopPolling()");
        if(mThread != null) {
            mThread.interrupt();
        }
    }

    private class PollingThread extends Thread {
        BenchReader mReader;
        PollingThread(BenchReader reader) {
            setName(BenchReader.class.getSimpleName()
                    + "(" + reader.getName() + ")");
            mReader = reader;
        }
        @Override
        public void run() {
            LOG.debug("polling \"" + mTerminal.getName() + "\"");
            try {
                boolean present;
                while(true) {
                    // handle interruptions
                    if(Thread.interrupted()) {
                        return;
                    }
                    // check presence
                    present = mTerminal.isCardPresent();
                    if (present) {
                        // run session
                        LOG.debug("running test session");
                        runSession();
                        // wait for removal
                        do {
                            if(Thread.interrupted()) {
                                return;
                            }
                            //LOG.trace("waiting for removal");
                        } while(!mTerminal.waitForCardAbsent(100));
                        LOG.debug("card removed");
                    } else {
                        // wait for insertion
                        do {
                            if(Thread.interrupted()) {
                                return;
                            }
                            //LOG.trace("waiting for insertion");
                        } while(!mTerminal.waitForCardPresent(100));
                        LOG.debug("card inserted");
                    }
                }
            } catch (CardException e) {
                LOG.error("error polling for \"" + mTerminal.getName() + "\"", e);
            }
        }
    }

}
