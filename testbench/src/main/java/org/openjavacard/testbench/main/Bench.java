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

import org.openjavacard.generic.GenericContext;
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
    private GenericContext mGeneric;

    private List<BenchReader> mReaders;

    public Bench(BenchConfiguration config) {
        mConfig = config;
        mContext = new GPContext();
        mGeneric = new GenericContext();
        mReaders = new ArrayList<>();
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
            terminals = mGeneric.findTerminals();
        } else {
            // else filter for given names/prefixes
            terminals = new ArrayList<>();
            for(String readerName: mConfig.reader) {
                List<CardTerminal> found = mGeneric.findTerminals(readerName);
                if(found.isEmpty()) {
                    throw new Error("Could find any readers matching \"" + readerName + "\"");
                }
                for(CardTerminal terminal: found) {
                    terminals.add(terminal);
                }
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
            reader.runSession();
        }
    }

    private void executePoll() {
        for(BenchReader reader: mReaders) {
            reader.startPolling();
        }
    }

}
