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

package org.openjavacard.gp.protocol;

import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.tlv.TLV;
import org.openjavacard.tlv.TLVPrimitive;

import java.io.IOException;

/**
 * GlobalPlatform Key Information
 * <p/>
 * Normally the member of a Key Information Template.
 * <p/>
 * Each of these describes a single key with given
 * version and ID. Size and type are also specified.
 * <p/>
 */
public class GPKeyInfo {

    /** Tag for GP Key Information objects */
    private static final int TAG_KEY_INFO = 0xC000;

    /** Key id */
    private final int mKeyId;
    /** Key version */
    private final int mKeyVersion;
    /** Key types */
    private final int[] mKeyTypes;
    /** Key sizes */
    private final int[] mKeySizes;

    /**
     * Construct a Key Information object for one key component
     */
    public GPKeyInfo(int keyId, int keyVersion, int keyType, int keySize) {
        mKeyId = keyId;
        mKeyVersion = keyVersion;
        mKeyTypes = new int[] {keyType};
        mKeySizes = new int[] {keySize};
    }

    /**
     * Construct a Key Information object for multiple key components
     */
    public GPKeyInfo(int keyId, int keyVersion, int[] keyTypes, int[] keySizes) {
        if(keyTypes.length != keySizes.length) {
            throw new IllegalArgumentException("Number of key types and sizes must be equal");
        }
        if(keyTypes.length > 1) {
            throw new IllegalArgumentException("Keys with multiple components are not supported");
        }
        mKeyId = keyId;
        mKeyVersion = keyVersion;
        mKeyTypes = keyTypes.clone();
        mKeySizes = keySizes.clone();
    }

    /** @return the key id */
    public int getKeyId() {
        return mKeyId;
    }

    /** @return the key version */
    public int getKeyVersion() {
        return mKeyVersion;
    }

    /** @return the key component types */
    public int[] getKeyTypes() {
        return mKeyTypes.clone();
    }

    /** @return the key component sizes */
    public int[] getKeySizes() {
        return mKeySizes.clone();
    }

    /**
     * Return true of the given key matches this object
     *
     * @param key to check
     * @return true if key matches
     */
    public boolean matchesKey(GPKey key) {
        if (mKeyTypes.length != 1) {
            throw new UnsupportedOperationException("Keys with multiple components are not supported");
        }

        // check the key id
        int keyId = key.getId();
        if (keyId != 0 && keyId != mKeyId) {
            return false;
        }

        // check the cipher
        byte keyType = (byte) (mKeyTypes[0] & 0xFF);
        GPKeyCipher kiCipher = GPKeyCipher.getCipherForKeyType(keyType);
        GPKeyCipher keyCipher = key.getCipher();

        // check the type unless key is generic
        if (keyCipher != GPKeyCipher.GENERIC && kiCipher != keyCipher) {
            return false;
        }

        // check the size of the secret
        if (mKeySizes[0] != key.getLength()) {
            return false;
        }

        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Key id ");
        sb.append(mKeyId);
        sb.append(" version ");
        sb.append(mKeyVersion);
        int numKeys = mKeyTypes.length;
        if (numKeys > 0) {
            sb.append(" (");
        }
        for (int i = 0; i < numKeys; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("type ");
            sb.append(GP.keyTypeString((byte) (mKeyTypes[i] & 0xFF)));
            sb.append(" size ");
            sb.append(mKeySizes[i]);
        }
        if (numKeys > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Parse Key Information object from bytes
     * @param data to parse
     * @return a Key Information object
     */
    public static GPKeyInfo fromBytes(byte[] data) throws IOException {
        return fromTLV(TLVPrimitive.readPrimitive(data));
    }

    /**
     * Parse Key Information object from TLV
     * @param tlv to parse
     * @return a Key Information object
     */
    public static GPKeyInfo fromTLV(TLV tlv) {
        return fromValue(tlv.asPrimitive(TAG_KEY_INFO).getValueBytes());
    }

    /**
     * Internal: parse raw value of a Key Information object
     * @param buf to parse
     * @return a Key Information object
     */
    private static GPKeyInfo fromValue(byte[] buf) {
        int off = 0, length = buf.length;
        if (length < 4) {
            throw new IllegalArgumentException("Invalid key info - too short");
        }
        int keyId = buf[off++] & 0xFF;
        int keyVersion = buf[off++] & 0xFF;
        int numKeys = (length - 2) / 2;
        if (numKeys > 1) {
            throw new UnsupportedOperationException("Keys with multiple components are not supported");
        }
        int[] keyTypes = new int[numKeys];
        int[] keySizes = new int[numKeys];
        for (int i = 0; i < numKeys; i++) {
            keyTypes[i] = buf[off++] & 0xFF;
            keySizes[i] = buf[off++] & 0xFF;
        }
        if(off != length) {
            throw new IllegalArgumentException("Invalid key info -unknown trailing data");
        }
        return new GPKeyInfo(keyId, keyVersion, keyTypes, keySizes);
    }

}
