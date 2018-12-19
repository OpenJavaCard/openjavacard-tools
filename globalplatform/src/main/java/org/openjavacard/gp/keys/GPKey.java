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

import org.openjavacard.util.HexUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class GPKey {

    private final byte mId;

    private final GPKeyType mType;

    private final GPKeyCipher mCipher;

    private final byte[] mSecret;

    /**
     * Constructs a key object for the provided data
     *
     * @param type   of the key object
     * @param cipher of the key object
     * @param secret the key itself
     */
    public GPKey(GPKeyType type, byte id, GPKeyCipher cipher, byte[] secret) {
        mType = type;
        mId = id;
        mCipher = cipher;
        mSecret = secret.clone();
        checkKeyLength();
    }

    /**
     * @return key id of this key
     */
    public byte getId() {
        return mId;
    }

    /**
     * @return type of this key
     */
    public GPKeyType getType() {
        return mType;
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
     * Return a SecretKey for a specific cipher
     * <p/>
     * Will coerce the key if required.
     *
     * @param cipher for the new key
     * @return new SecretKey corresponding to this key
     */
    public SecretKey getSecretKey(GPKeyCipher cipher) {
        switch (cipher) {
            case DES:
                return new SecretKeySpec(enlarge(mSecret, 8), "DES");
            case DES3:
                return new SecretKeySpec(enlarge(mSecret, 24), "DESede");
            case AES:
                return new SecretKeySpec(mSecret, "AES");
            default:
                throw new IllegalArgumentException("Do not know how to handle cipher " + cipher);
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
        }
    }

    public String toString() {
        String idString = "";
        if(mId != 0) {
            idString = " id " + mId;
        }
        return "key " + mType + idString + " cipher " + mCipher
                + " secret " + HexUtil.bytesToHex(mSecret);
    }

}
