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
import org.openjavacard.gp.keys.GPKeyUsage;

public class SCPDiversification {

    public static GPKey diversifyKeyEMV(GPKeyUsage usage, GPKey key, byte[] diversificationData) {
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

    public static GPKey diversifyKeyVisa2(GPKeyUsage usage, GPKey key, byte[] diversificationData) {
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
