/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2019 Ingo Albrecht <copyright@promovicz.org>
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
import org.openjavacard.gp.keys.GPKeyDiversification;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.util.HexUtil;

public class SCPDiversification {

    private static final GPKeyUsage[] DIVERSIFICATION_KEYS = {
            GPKeyUsage.ENC, GPKeyUsage.MAC, GPKeyUsage.KEK, GPKeyUsage.RMAC
    };

    public static GPKeySet diversify(GPKeySet keys, GPKeyDiversification diversification, byte[] diversificationData) {
        if (keys.getDiversification() != GPKeyDiversification.NONE) {
            throw new IllegalArgumentException("Cannot diversify a diversified keyset");
        }
        if(diversification == GPKeyDiversification.NONE) {
            throw new IllegalArgumentException("No need to diversify for diversification NONE");
        }
        String diversifiedName = keys.getName() + "-" + diversification.name() + ":" + HexUtil.bytesToHex(diversificationData);
        GPKeySet diversifiedKeys = new GPKeySet(diversifiedName, keys.getKeyVersion(), diversification);
        for(GPKeyUsage type: DIVERSIFICATION_KEYS) {
            GPKey key = keys.getKeyByUsage(type);
            if(key != null) {
                GPKey diversifiedKey;
                switch (diversification) {
                    case EMV:
                        diversifiedKey = SCPDiversification.diversifyKeyEMV(type, key, diversificationData);
                        break;
                    case VISA2:
                        diversifiedKey = SCPDiversification.diversifyKeyVisa2(type, key, diversificationData);
                        break;
                    default:
                        throw new RuntimeException("Unsupported diversification " + diversification);
                }
                diversifiedKeys.putKey(diversifiedKey);
            }
        }
        return diversifiedKeys;
    }

    private static GPKey diversifyKeyEMV(GPKeyUsage usage, GPKey key, byte[] diversificationData) {
        byte[] data = new byte[16];
        System.arraycopy(diversificationData, 4, data, 0, 6);
        data[6] = (byte)0xF0;
        data[7] = usage.diversifyId;
        System.arraycopy(diversificationData, 4, data, 8, 6);
        data[14] = (byte)0x0F;
        data[15] = usage.diversifyId;
        byte[] dKey = GPCrypto.enc_3des_ecb(key, data);
        return new GPKey(key.getId(), usage, key.getCipher(), dKey);
    }

    private static GPKey diversifyKeyVisa2(GPKeyUsage usage, GPKey key, byte[] diversificationData) {
        byte[] data = new byte[16];
        System.arraycopy(diversificationData, 0, data, 0, 2);
        System.arraycopy(diversificationData, 4, data, 2, 4);
        data[6] = (byte)0xF0;
        data[7] = usage.diversifyId;
        System.arraycopy(diversificationData, 0, data, 8, 2);
        System.arraycopy(diversificationData, 4, data, 10, 4);
        data[14] = (byte)0x0F;
        data[15] = usage.diversifyId;
        byte[] dKey = GPCrypto.enc_3des_ecb(key, data);
        return new GPKey(key.getId(), usage, key.getCipher(), dKey);
    }

}
