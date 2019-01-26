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

import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.util.HexUtil;

import java.util.Hashtable;

/**
 * SCP02 session key derivation
 */
public class SCP02Derivation {

    /** All key types in SCP02 */
    private static final GPKeyUsage[] KEYS = {
            GPKeyUsage.ENC, GPKeyUsage.MAC, GPKeyUsage.KEK, GPKeyUsage.RMAC
    };

    /** Derivation constants */
    private static final Hashtable<GPKeyUsage, byte[]> CONSTANTS = new Hashtable<>();
    private static final byte[] SCP02_DERIVE_MAC  = {0x01, 0x01};
    private static final byte[] SCP02_DERIVE_RMAC = {0x01, 0x02};
    private static final byte[] SCP02_DERIVE_KEK = {0x01, (byte) 0x81};
    private static final byte[] SCP02_DERIVE_ENC = {0x01, (byte) 0x82};
    static {
        CONSTANTS.put(GPKeyUsage.MAC,  SCP02_DERIVE_MAC);
        CONSTANTS.put(GPKeyUsage.RMAC, SCP02_DERIVE_RMAC);
        CONSTANTS.put(GPKeyUsage.KEK,  SCP02_DERIVE_KEK);
        CONSTANTS.put(GPKeyUsage.ENC,  SCP02_DERIVE_ENC);
    }

    /**
     * Derive SCP02 session keys
     *
     * @param staticKeys to derive from
     * @param sequence for derivation
     * @return keyset containing session keys
     */
    public static GPKeySet deriveSessionKeys(GPKeySet staticKeys, byte[] sequence) {
        // synthesize a name for the new keyset
        String name = staticKeys.getName() + "-SCP02:" + HexUtil.bytesToHex(sequence);
        // create the new set
        GPKeySet derivedSet = new GPKeySet(name, staticKeys.getKeyVersion(), staticKeys.getDiversification());
        // initialize buffer for derivation
        byte[] buffer = new byte[16];
        System.arraycopy(sequence, 0, buffer, 2, 2);
        // go through all keys
        for (GPKeyUsage usage : KEYS) {
            // get the static base key
            GPKey staticKey = staticKeys.getKeyByUsage(usage);
            if(staticKey != null) {
                // insert derivation data
                byte[] derivation = CONSTANTS.get(usage);
                System.arraycopy(derivation, 0, buffer, 0, 2);
                // derive using DES
                byte[] derived = GPCrypto.enc_3des_cbc_nulliv(staticKey, buffer);
                // construct the new key
                GPKey sessionKey = new GPKey(
                        staticKey.getId(), usage, GPKeyCipher.DES3, derived);
                // insert key into new set
                derivedSet.putKey(sessionKey);
            }
        }
        // return the new set
        return derivedSet;
    }

}
