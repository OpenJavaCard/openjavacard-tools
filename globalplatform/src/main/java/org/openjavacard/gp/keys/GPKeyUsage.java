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

package org.openjavacard.gp.keys;

/**
 * Representation of key usage types
 * <p/>
 * This is used to describe and restrict the usage of keys.
 * <p/>
 */
public enum GPKeyUsage {
    /** Master keys may be used for anything. Used for default keys and single-key scenarios. */
    MASTER(0),
    /** ENC means use for command and/or response encryption */
    ENC(1),
    /** MAC means use for command authentication */
    MAC(2),
    /** KEK means use for encryption of key material */
    KEK(3),
    /** RMAC means use for response authentication */
    RMAC(4);

    /** Identifier of this usage type during key diversification */
    public final byte diversifyId;

    /**
     * Internal constructor
     * @param diversifyId for the usage type
     */
    GPKeyUsage(int diversifyId) {
        this.diversifyId = (byte)diversifyId;
    }

}
