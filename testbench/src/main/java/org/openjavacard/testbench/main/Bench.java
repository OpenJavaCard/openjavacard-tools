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

import org.openjavacard.gp.client.GPContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.util.ArrayList;
import java.util.List;

public class Bench {

    private static final Logger LOG = LoggerFactory.getLogger(Bench.class);

    private BenchConfiguration mConfig;

    private GPContext mContext;

    private List<BenchReader> mReaders;

    private List<DetectThread> mReaderPollThreads;

    public Bench(BenchConfiguration config) {
        mConfig = config;
        mContext = new GPContext();
        mReaders = new ArrayList<>();
        mReaderPollThreads = new ArrayList<>();
    }

    public GPContext getContext() {
        return mContext;
    }

    public void configure() {
        LOG.debug("configure()");
        // determine readers to use
        List<CardTerminal> terminals;
        if(mConfig.allReaders) {
            // all means all
            terminals = mContext.findTerminals();
        } else {
            // else filter for given names/prefixes
            terminals = new ArrayList<>();
            for(String readerName: mConfig.reader) {
                terminals.add(mContext.findSingleTerminal(readerName));
            }
        }
        // complain if no readers at all
        if(terminals.isEmpty()) {
            throw new Error("No readers found/configured");
        }
        // set up each reader
        List<BenchReader> readers = new ArrayList<>();
        for(CardTerminal reader: terminals) {
            LOG.info("using terminal \"" + reader.getName() + "\"");
            readers.add(new BenchReader(this, reader));
        }
        // remember readers
        mReaders = readers;
    }

    public void execute() {
        LOG.debug("execute()");

        LOG.info("running in mode " + mConfig.mode);
        switch(mConfig.mode) {
            case ONCE:
                executeOnce();
                break;
            case POLL:
                executePoll();
                break;
        }
    }

    private void executeOnce() {
        for(BenchReader reader: mReaders) {
            if (!reader.isCardPresent()) {
                throw new Error("No card in reader \"" + reader.getName() + "\"");
            }
            reader.onCardInserted();
        }
    }

    private void executePoll() {
        for(BenchReader reader: mReaders) {
            DetectThread thread = new DetectThread(reader);
            thread.start();
            mReaderPollThreads.add(thread);
        }
    }

    private class DetectThread extends Thread {
        BenchReader mReader;
        CardTerminal mTerminal;
        boolean mPresent;
        DetectThread(BenchReader reader) {
            setName(Bench.class.getSimpleName()
                    + "(" + reader.getName() + ")");
            mReader = reader;
            mTerminal = reader.getTerminal();
            mPresent = false;
        }
        @Override
        public void run() {
            LOG.debug("polling \"" + mTerminal.getName() + "\"");
            try {
                // initialize state
                mPresent = mTerminal.isCardPresent();
                // initial presence treated as insertion
                if(mPresent) {
                    mReader.onCardInserted();
                }
                // need to track previous state to detect changes
                boolean previouslyPresent = mPresent;
                while(true) {
                    // wait for event
                    boolean event;
                    if(mPresent) {
                        event = mTerminal.waitForCardAbsent(30000);
                    } else {
                        event = mTerminal.waitForCardPresent(30000);
                    }
                    // handle interruptions
                    if(Thread.interrupted()) {
                        LOG.debug("interrupted");
                        if(previouslyPresent) {
                            mReader.onCardRemoved();
                        }
                        return;
                    }
                    // wait again if no event
                    if(!event) {
                        continue;
                    }
                    // update state
                    mPresent = mTerminal.isCardPresent();
                    // handle state changes
                    if(previouslyPresent && !mPresent) {
                        mReader.onCardRemoved();
                    }
                    if(mPresent && !previouslyPresent) {
                        mReader.onCardInserted();
                    }
                    // remember state
                    previouslyPresent = mPresent;
                }
            } catch (CardException e) {
                LOG.error("error polling for \"" + mTerminal.getName() + "\"", e);
            }
        }
    }

}
