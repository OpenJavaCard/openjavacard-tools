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

import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.util.HexUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class GPKey {

    /** GlobalPlatform default master secret */
    public static final byte[] GLOBALPLATFORM_MASTER_SECRET = {
            0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,
            0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F
    };

    /** GlobalPlatform default master key */
    public static final GPKey GLOBALPLATFORM_MASTER = new GPKey(
            0, GPKeyUsage.MASTER, GPKeyCipher.GENERIC,
            GLOBALPLATFORM_MASTER_SECRET);

    /** Key id of this key */
    private final int mId;

    /** Usage type of this key */
    private final GPKeyUsage mUsage;

    /** Cipher that this key is intended for */
    private final GPKeyCipher mCipher;

    /** Secret corresponding to this key */
    private final byte[] mSecret;

    /**
     * Constructs a key object for the provided data
     *
     * @param keyId of the key
     * @param usage  of the key
     * @param cipher of the key
     * @param secret the key itself
     */
    public GPKey(int keyId, GPKeyUsage usage, GPKeyCipher cipher, byte[] secret) {
        GPKeyId.checkKeyId(keyId);
        mId = keyId;
        mUsage = usage;
        mCipher = cipher;
        mSecret = secret.clone();
        checkKeyLength();
    }

    /**
     * @return key id of this key
     */
    public int getId() {
        return mId;
    }

    /**
     * @return type of this key
     */
    public GPKeyUsage getUsage() {
        return mUsage;
    }

    /**
     * @return cipher of this key
     */
    public GPKeyCipher getCipher() {
        return mCipher;
    }

    /**
     * @return key bytes of this key
     */
    public byte[] getSecret() {
        return mSecret.clone();
    }

    /**
     * @return length of secret in bytes
     */
    public int getLength() {
        return mSecret.length;
    }

    /**
     * @return true if key is compatible with given cipher
     * @param cipher to check compatibility with
     */
    private boolean isCompatible(GPKeyCipher cipher) {
        return (mCipher == GPKeyCipher.GENERIC)
                || (mCipher == cipher)
                || (mCipher == GPKeyCipher.DES3 && cipher == GPKeyCipher.DES);
    }

    /**
     * Return the key check value (KCV) for the key
     * <p/>
     * The algorithm used depends on the cipher of the key.
     * <p/>
     * @param cipher to use
     * @return the key check value
     */
    public byte[] getCheckValue(GPKeyCipher cipher) {
        if(!isCompatible(cipher)) {
            throw new UnsupportedOperationException("Cannot use " + mCipher + " key with cipher " + cipher);
        }
        switch(cipher) {
            case DES3:
                return GPCrypto.kcv_3des(this);
            case AES:
                return GPCrypto.kcv_aes(this);
            default:
                throw new UnsupportedOperationException("Cannot generate KCV for cipher " + cipher);
        }
    }

    /**
     * Return a SecretKey for a specific cipher
     * <p/>
     * Will coerce the key if required, such as for GENERIC keys.
     * <p/>
     * @param cipher for the new key
     * @return the secret key
     */
    public SecretKey getSecretKey(GPKeyCipher cipher) {
        if(!isCompatible(cipher)) {
            throw new UnsupportedOperationException("Cannot use " + mCipher + " key with cipher " + cipher);
        }
        switch (cipher) {
            case DES:
                return new SecretKeySpec(enlarge(mSecret, 8), "DES");
            case DES3:
                return new SecretKeySpec(enlarge(mSecret, 24), "DESede");
            case AES:
                return new SecretKeySpec(mSecret, "AES");
            default:
                throw new IllegalArgumentException("Cannot make secret key for cipher " + cipher);
        }
    }

    private byte[] enlarge(byte[] key, int length) {
        int secretLen = key.length;
        if(length == secretLen) {
            return key;
        }
        if(length == 8) {
            byte[] key8 = new byte[8];
            switch(secretLen) {
                case 8:
                case 16:
                case 24:
                    System.arraycopy(key, 0, key8, 0, 8);
                    return key8;
            }
        }
        if(length == 24) {
            byte[] key24 = new byte[24];
            switch(secretLen) {
                case 8:
                    System.arraycopy(key, 0, key24,  0, 8);
                    System.arraycopy(key, 0, key24,  8, 8);
                    System.arraycopy(key, 0, key24, 16, 8);
                    return key24;
                case 16:
                    System.arraycopy(key, 0, key24,  0, 16);
                    System.arraycopy(key, 0, key24, 16,  8);
                    return key24;
                case 24:
                    System.arraycopy(key, 0, key24, 0, 24);
                    return key24;
            }
        }
        throw new Error("Do not know how to coerce DES key from length " + secretLen + " to length " + length);
    }

    /**
     * Internal: check that key length is appropriate
     */
    private void checkKeyLength() {
        int length = mSecret.length;
        switch (mCipher) {
            case GENERIC:
                if(length % 8 != 0) {
                    throw new IllegalArgumentException("Bad key length");
                }
                if(length > 32) {
                    throw new IllegalArgumentException("Key to long");
                }
                break;
            case DES:
                if(length != 8) {
                    throw new IllegalArgumentException("DES keys must be 8 bytes long");
                }
                break;
            case DES3:
                if(length != 8 && length != 16 && length != 24) {
                    throw new IllegalArgumentException("3DES keys must be [8,16,24] bytes long");
                }
                break;
            case AES:
                if(length != 16) {
                    throw new IllegalArgumentException("AES keys must be 16 bytes long");
                }
                break;
            default:
                throw new UnsupportedOperationException("Do not know how to check key length for cipher " + mCipher);
        }
    }

    public String toString() {
        return "key id " + (mId==0?"any":mId)
                + " usage " + mUsage
                + " cipher " + mCipher
                + " secret " + HexUtil.bytesToHex(mSecret);
    }

}
