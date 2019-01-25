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
 * Base class for SCP wrappers
 * <p/>
 * These objects can wrap and unwrap APDUs to be sent to the card
 * or received back, encrypting and authenticating them as configured.
 * <p/>
 * NOTE This has not been thought through yet for SCP03, SCP10 and others.
 * Some stuff may have to move to implement them nicely.
 */
public abstract class SCPWrapper {

    /** Session keys in use */
    protected final GPKeySet mKeys;

    /** Command encryption currently enabled? */
    protected boolean mENC;
    /** Command authentication currently enabled? */
    protected boolean mMAC;
    /** Response authentication currently enabled? */
    protected boolean mRMAC;
    /** Response encryption currently enabled? */
    protected boolean mRENC;

    /**
     * Construct an SCP wrapper
     * @param keys to use for the session
     */
    SCPWrapper(GPKeySet keys) {
        mKeys = keys;
        // SCP0102 and SCP03 all start with MAC enabled
        mMAC = true;
        // other options must be enabled by client
        mENC = false;
        mRMAC = false;
        mRENC = false;
    }

    /**
     * Used to start command encryption when it is expected
     *
     * Subclasses may want to set up state.
     */
    protected void startENC() {
        mENC = true;
    }

    /**
     * Used to start response authentication when it is expected
     *
     * Subclasses may want to set up state.
     */
    protected void startRMAC() {
        mRMAC = true;
    }

    /**
     * Used to start response encryption when it is expected
     *
     * Subclasses may want to set up state.
     */
    protected void startRENC() {
        mRENC = true;
    }

    /**
     * Get the maximum command APDU size for the current mode
     *
     * This must be dynamic since it depends on the specific
     * protocol as well as the currently established mode.
     *
     * @return maximum size for commands
     */
    protected abstract int getMaxSize();

    /**
     * Encrypt sensitive data
     *
     * This is used to encrypt secret keys, protecting them
     * with an additional layer of encryption for added security.
     *
     * There are protocol-specific restrictions on the data.
     *
     * @param data to be encrypted
     * @return encrypted data
     * @throws CardException
     */
    public abstract byte[] encryptSensitiveData(byte[] data) throws CardException;

    /**
     * Wrap a command APDU using the current mode
     *
     * @param command to wrap
     * @return the wrapped command
     * @throws CardException
     */
    public abstract CommandAPDU wrap(CommandAPDU command) throws CardException;

    /**
     * Unwrap a response APDU using the current mode
     * @param response to unwrap
     * @return the unwrapped response
     * @throws CardException
     */
    public abstract ResponseAPDU unwrap(ResponseAPDU response) throws CardException;

}
