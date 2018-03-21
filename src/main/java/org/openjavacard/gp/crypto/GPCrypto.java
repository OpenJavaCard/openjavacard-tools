/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.gp.crypto;

import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.util.Arrays;

/**
 * Wrappers for cryptographic functions used in GP
 */
public class GPCrypto {

    //private static final Logger LOG = LoggerFactory.getLogger(GPCrypto.class);

    private static final byte[] ZEROES_8 = new byte[]{
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    private static final byte[] ZEROES_16 = new byte[]{
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };


    private static final String AES_CBC_NOPAD = "AES/CBC/NoPadding";

    private static final String DES_ECB_NOPAD = "DES/ECB/NoPadding";
    private static final String DES3_ECB_NOPAD = "DESede/ECB/NoPadding";

    private static final String DES_CBC_NOPAD = "DES/CBC/NoPadding";
    private static final String DES3_CBC_NOPAD = "DESede/CBC/NoPadding";


    private static void checkKeyCipher(GPKey key, GPKeyCipher cipher) {
        GPKeyCipher keyCipher = key.getCipher();
        if (keyCipher != cipher) {
            throw new IllegalArgumentException("Key is for wrong cipher " + keyCipher + ", need " + cipher);
        }
    }

    /**
     * Encrypt using AES-CBC
     *
     * @param key
     * @param text
     * @return
     */
    public static byte[] enc_aes_cbc(GPKey key, byte[] text) {
        Key secretKey = key.getSecretKey(GPKeyCipher.AES);
        return enc(AES_CBC_NOPAD, secretKey, text, 0, text.length, null);
    }

    /**
     * Encrypt using DES-ECB
     *
     * @param key
     * @param text
     * @return
     */
    public static byte[] enc_des_ecb(GPKey key, byte[] text) {
        // XXX check cipher of key, we coerce 3DES to DES deliberately sometimes
        Key secretKey = key.getSecretKey(GPKeyCipher.DES);
        return enc(DES_ECB_NOPAD, secretKey, text, 0, text.length, null);
    }

    /**
     * Encrypt using 3DES-ECB
     *
     * @param key
     * @param text
     * @return
     */
    public static byte[] enc_3des_ecb(GPKey key, byte[] text) {
        checkKeyCipher(key, GPKeyCipher.DES3);
        return enc(DES3_ECB_NOPAD, key.getSecretKey(), text, 0, text.length, null);
    }

    /**
     * Encrypt using 3DES-CBC with a null IV
     *
     * @param key
     * @param text
     * @return
     */
    public static byte[] enc_3des_cbc_nulliv(GPKey key, byte[] text) {
        return enc_3des_cbc(key, text, ZEROES_8);
    }

    /**
     * Encrypt using 3DES-CBC
     *
     * @param key
     * @param text
     * @param iv
     * @return
     */
    public static byte[] enc_3des_cbc(GPKey key, byte[] text, byte[] iv) {
        checkKeyCipher(key, GPKeyCipher.DES3);
        return enc(DES3_CBC_NOPAD, key.getSecretKey(), text, 0, text.length, iv);
    }

    private static byte[] enc(String cipherSpec, Key key, byte[] text, int offset, int length, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(cipherSpec);
            if (iv == null) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            }
            return cipher.doFinal(text, offset, length);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static byte[] mac_aes_nulliv(GPKey key, byte[] d) {
        return mac_aes(key, d, ZEROES_8);
    }

    public static byte[] mac_aes(GPKey key, byte[] text, byte[] iv) {
        checkKeyCipher(key, GPKeyCipher.AES);
        return mac_aes(key.getSecretKey(GPKeyCipher.AES), text, 0, text.length, iv);
    }

    private static byte[] mac_aes(Key key, byte[] text, int offset, int length, byte[] iv) {
        final int macLength = 8;
        final int blockLength = 16;
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_NOPAD);

            // pad if required
            boolean padded = (text.length % blockLength) != 0;
            byte[] input = Arrays.copyOfRange(text, offset, offset + length);
            if (padded) {
                input = pad80(input, blockLength);
            }

            // derive padding secrets
            byte[] L = new byte[blockLength];
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            cipher.update(ZEROES_16, 0, L.length, L, 0);
            byte[] Lu1 = mac_aes_doubleLu(L);
            byte[] Lu2 = mac_aes_doubleLu(Lu1);

            // shadow last block
            int lastBlock = input.length - blockLength;
            byte[] Lu = Lu1;
            if (padded) {
                Lu = Lu2;
            }
            for (int i = 0; i < blockLength; i++) {
                input[lastBlock + i] ^= Lu[lastBlock + i];
            }

            // perform CBC operation
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] raw = cipher.doFinal(input, 0, input.length);

            // truncate result and return
            return Arrays.copyOfRange(raw, 0, macLength);
        } catch (Exception e) {
            throw new RuntimeException("MAC computation failed", e);
        }
    }

    private static final byte MAC_AES_POLY_BLOCK16 = (byte) 0x87;

    private static byte[] mac_aes_doubleLu(byte[] k) {
        byte[] ret = new byte[k.length];

        int carry = mac_aes_shiftLeft(k, ret);
        int mask = (-carry) & 0xff;

        ret[ret.length - 1] ^= MAC_AES_POLY_BLOCK16 & mask;

        return ret;
    }

    private static int mac_aes_shiftLeft(byte[] block, byte[] output) {
        int i = block.length;
        int carry = 0;
        while (--i >= 0) {
            int b = block[i] & 0xff;
            output[i] = (byte) ((b << 1) | carry);
            carry = (b >>> 7) & 1;
        }
        return carry;
    }

    /**
     * MAC function using 3DES-CBC with a null IV
     * <p/>
     * Used by SCP01/SCP02 for authentication.
     *
     * @param key
     * @param d
     * @return
     */
    public static byte[] mac_3des_nulliv(GPKey key, byte[] d) {
        return mac_3des(key, d, ZEROES_8);
    }

    /**
     * MAC function using 3DES-CBC
     * <p/>
     * Used by SCP01 for C-MAC.
     *
     * @param key
     * @param text
     * @param iv
     * @return
     */
    public static byte[] mac_3des(GPKey key, byte[] text, byte[] iv) {
        checkKeyCipher(key, GPKeyCipher.DES3);
        byte[] d = pad80(text, 8);
        return mac_3des(key.getSecretKey(), d, 0, d.length, iv);
    }

    private static byte[] mac_3des(Key key, byte[] text, int offset, int length, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(DES3_CBC_NOPAD);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] result = new byte[8];
            byte[] res = cipher.doFinal(text, offset, length);
            System.arraycopy(res, res.length - 8, result, 0, 8);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("MAC computation failed", e);
        }
    }

    /**
     * MAC function using DES+3DES-CBC
     * <p/>
     * Used by SCP02 as a more efficient version of the
     * earlier full 3DES-CBC MAC used in SCP01.
     * <p/>
     * This uses DES in CBC mode for all but the last round.
     * The last round is computed using 3DES for extra security.
     *
     * @param key
     * @param text
     * @param iv
     * @return
     */
    public static byte[] mac_des_3des(GPKey key, byte[] text, byte[] iv) {
        checkKeyCipher(key, GPKeyCipher.DES3);
        byte[] padded = pad80(text, 8);
        return mac_des_3des(key, padded, 0, padded.length, iv);
    }

    private static byte[] mac_des_3des(GPKey key, byte[] text, int offset, int length, byte[] iv) {
        try {
            // get ciphers
            Cipher cipherA = Cipher.getInstance(DES3_CBC_NOPAD);
            Cipher cipherB = Cipher.getInstance(DES_CBC_NOPAD);
            // get keys (truncated version for DES)
            SecretKey keyA = key.getSecretKey(GPKeyCipher.DES3);
            SecretKey keyB = key.getSecretKey(GPKeyCipher.DES);

            // IV for final round depends on whether we have previous rounds
            byte[] finalIV = iv;

            // pre-final rounds
            if (length > 8) {
                // long messages get all but the last block hashed using DES
                cipherB.init(Cipher.ENCRYPT_MODE, keyB, new IvParameterSpec(iv));
                byte[] partial = cipherB.doFinal(text, offset, length - 8);
                // and use the output from that as the IV for the 3DES final round
                finalIV = Arrays.copyOfRange(partial, partial.length - 8, partial.length);
            }

            // final round
            cipherA.init(Cipher.ENCRYPT_MODE, keyA, new IvParameterSpec(finalIV));
            byte[] finalCipher = cipherA.doFinal(text, (offset + length) - 8, 8);

            // copy result and return
            return Arrays.copyOfRange(finalCipher, finalCipher.length - 8, finalCipher.length);
        } catch (Exception e) {
            throw new RuntimeException("MAC computation failed", e);
        }
    }

    /**
     * PAD80 padding function
     */
    public static byte[] pad80(byte[] text, int blocksize) {
        return pad80(text, 0, text.length, blocksize);
    }

    private static byte[] pad80(byte[] text, int offset, int length, int blocksize) {
        int totalLength = length;
        for (totalLength++; (totalLength % blocksize) != 0; totalLength++) {
            ;
        }
        int padlength = totalLength - length;
        byte[] result = new byte[totalLength];
        System.arraycopy(text, offset, result, 0, length);
        result[length] = (byte) 0x80;
        for (int i = 1; i < padlength; i++) {
            result[length + i] = (byte) 0x00;
        }
        return result;
    }

    /**
     * Special padding function for SCP01 encryption
     * <p/>
     * SCP01 uses its own padding scheme where the length
     * of the payload is prepended. If this block is now
     * a multiple of the blocksize then no further padding
     * is applied, otherwise pad80 is used.
     * <p/>
     * @param text
     * @param blocksize
     * @return
     */
    public static byte[] pad80_scp01(byte[] text, int blocksize) {
        // SCP01 has optional padding, and it will
        // also prepend the length to the plaintext
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        int textLen = text.length;
        int totalSize = textLen + 1;
        plain.write(((byte) (textLen & 0xFF)));
        plain.write(text, 0, textLen);
        // check if padding required
        if ((totalSize % blocksize) != 0) {
            // perform padding (note this includes the length)
            byte[] padded = GPCrypto.pad80(plain.toByteArray(), blocksize);
            int paddedLen = padded.length;
            // recompose the plaintext
            plain.reset();
            plain.write(padded, 0, paddedLen);
        }
        return plain.toByteArray();
    }

}
