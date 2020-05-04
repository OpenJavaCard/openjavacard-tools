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

package org.openjavacard.gp.scp;

import org.openjavacard.gp.keys.GPKeySet;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Host-side APDU wrapper for SCP00
 */
public class SCP00Wrapper extends SCPWrapper {

    private SCP00Parameters mParameters;

    public SCP00Wrapper(GPKeySet keys, SCP00Parameters parameters) {
        super(keys);
        // remember protocol parameters
        mParameters = parameters;
        // check that we have an empty keyset
        if(!keys.getKeys().isEmpty()) {
            throw new UnsupportedOperationException("SCP00 should be used with an empty keyset");
        }
    }

    @Override
    protected SCPParameters getParameters() {
        return mParameters;
    }

    @Override
    protected int getMaxSize() {
        return 255;
    }

    @Override
    public byte[] encryptSensitiveData(byte[] data) throws CardException {
        return data;
    }

    @Override
    public CommandAPDU wrap(CommandAPDU command) throws CardException {
        return command;
    }

    @Override
    public ResponseAPDU unwrap(ResponseAPDU response) throws CardException {
        return response;
    }

}
