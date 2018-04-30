/*
 * openjavacard-tools: OpenJavaCard Development Tools
 * Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de
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
 *
 */

package org.openjavacard.gp.keys;

import org.openjavacard.gp.protocol.GP;

/**
 * Key cipher type
 * <p/>
 * Used to indicate what ciphers a key may be used with.
 */
public enum GPKeyCipher {
    /** Key for any symmetric cipher */
    GENERIC,
    /** Key for single-DES */
    DES,
    /** Key for triple-DES */
    DES3,
    /** Key for AES */
    AES,
    ;

    /**
     * Get the cipher for a GlobalPlatform key type
     * @param keyType to interpret
     * @return cipher corresponding to type
     */
    public static GPKeyCipher getCipherForKeyType(byte keyType) {
        switch (keyType) {
            case GP.KEY_TYPE_DES:
            case GP.KEY_TYPE_DES_CBC:
            case GP.KEY_TYPE_DES_ECB:
                return DES;
            case GP.KEY_TYPE_3DES_CBC:
                return DES3;
            case GP.KEY_TYPE_AES:
                return AES;
            default:
                throw new IllegalArgumentException("Unsupported key type " + keyType);
        }
    }

}
