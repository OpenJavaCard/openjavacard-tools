/*
 *  GlobalPlatformPro - GlobalPlatform tool
 *  Copyright (C) 2015-2017 Martin Paljak, martin@martinpaljak.net
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

/*
 * Large parts of this code where copy-and-pasted from GlobalPlatformPro.
 * Some parts where developed for this project.
 * Most has been modified.
 */
package org.openjavacard.gp.crypto;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.KDFCounterBytesGenerator;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KDFCounterParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class GPBouncy {

    private static final Logger LOG = LoggerFactory.getLogger(GPBouncy.class);

    public static byte[] scp03_mac(GPKey key, byte[] msg, int lengthbits) {
        return scp03_mac(key.getSecret(), msg, lengthbits);
    }

    private static byte[] scp03_mac(byte[] keybytes, byte[] msg, int lengthBits) {
        // Use BouncyCastle light interface.
        BlockCipher cipher = new AESEngine();
        CMac cmac = new CMac(cipher);
        cmac.init(new KeyParameter(keybytes));
        cmac.update(msg, 0, msg.length);
        byte[] out = new byte[cmac.getMacSize()];
        cmac.doFinal(out, 0);
        return Arrays.copyOf(out, lengthBits / 8);
    }

    public static byte[] scp03_kdf(GPKey key, byte constant, byte[] context, int blocklen_bits) {
        return scp03_kdf(key.getSecret(), constant, context, blocklen_bits);
    }

    private static byte[] scp03_kdf(byte[] key, byte constant, byte[] context, int blocklen_bits) {
        // 11 bytes
        byte[] label = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            bo.write(label); // 11 bytes of label
            bo.write(constant); // constant for the last byte
            bo.write(0x00); // separator
            bo.write((blocklen_bits >> 8) & 0xFF); // block size in two bytes
            bo.write(blocklen_bits & 0xFF);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        byte[] blocka = bo.toByteArray();
        byte[] blockb = context;
        return scp03_kdf(key, blocka, blockb, blocklen_bits/8);
    }

    private static byte[] scp03_kdf(byte[] key, byte[] a, byte[] b, int bytes) {
        LOG.info("scp03_kdf key=" + HexUtil.bytesToHex(key) + " a=" + HexUtil.bytesToHex(a) + " b=" + HexUtil.bytesToHex(b) + " bytes=" + bytes);
        BlockCipher cipher = new AESEngine();
        CMac cmac = new CMac(cipher);
        KDFCounterBytesGenerator kdf = new KDFCounterBytesGenerator(cmac);
        kdf.init(new KDFCounterParameters(key, a, b, 8));
        byte[] cgram = new byte[bytes];
        kdf.generateBytes(cgram, 0, cgram.length);
        return cgram;
    }


}
