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

import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.util.HexUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Set of GlobalPlatform keys
 * <p/>
 * Such a set can be used to represent a single master key,
 * a static set of keys, a set of diversified keys,
 * derived session keys or anything else.
 */
public class GPKeySet {

    /**
     * GlobalPlatform default master key
     */
    private static final byte[] GLOBALPLATFORM_MASTER_KEY = {
            0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,
            0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F
    };

    /**
     * GlobalPlatform default key set
     */
    public static final GPKeySet GLOBALPLATFORM =
            buildGeneric("GlobalPlatform", GLOBALPLATFORM_MASTER_KEY);

    private String mName;

    private int mKeyVersion;

    private GPKeyDiversification mDiversification;

    private final ArrayList<GPKey> mKeys = new ArrayList<>();
    private final Hashtable<GPKeyType, GPKey> mKeysByType = new Hashtable<>();
    private final Hashtable<Integer, GPKey> mKeysById = new Hashtable<>();

    private static GPKeySet buildGeneric(String name, byte[] masterKey) {
        GPKeySet keySet = new GPKeySet(name);
        keySet.putKey(new GPKey(GPKeyType.MASTER, (byte)0, GPKeyCipher.GENERIC, masterKey));
        return keySet;
    }

    /**
     * Constructor for empty keysets
     */
    public GPKeySet(String name) {
        this(name, 0, GPKeyDiversification.NONE);
    }

    /**
     * Constructor for empty keysets
     *
     * @param name       of the keyset
     * @param keyVersion of the keyset
     */
    public GPKeySet(String name, int keyVersion) {
        this(name, keyVersion, GPKeyDiversification.NONE);
    }

    /**
     * Constructor for empty keysets
     *
     * @param name            of the keyset
     * @param keyVersion      of the keyset
     * @param diversification of the keyset
     */
    public GPKeySet(String name, int keyVersion, GPKeyDiversification diversification) {
        mName = name;
        mKeyVersion = keyVersion;
        mDiversification = diversification;
    }

    /** @return the name of this keyset */
    public String getName() {
        return mName;
    }

    /** @return the version of this keyset */
    public int getKeyVersion() {
        return mKeyVersion;
    }

    /** @return the diversification of this keyset */
    public GPKeyDiversification getDiversification() {
        return mDiversification;
    }

    /** @return the keys in this keyset */
    public List<GPKey> getKeys() {
        return new ArrayList<>(mKeys);
    }

    /**
     * Retrieves the key of the given type from the keyset
     *
     * @param type of the key
     * @return the key or null
     */
    public GPKey getKeyByType(GPKeyType type) {
        if(mKeysByType.containsKey(type)) {
            return mKeysByType.get(type);
        } else {
            return mKeysByType.get(GPKeyType.MASTER);
        }
    }

    /**
     * Retrieves a key with the given id from the keyset
     *
     * @param keyId of the key
     * @return the key or null
     */
    public GPKey getKeyById(int keyId) {
        if(mKeysById.containsKey(keyId)) {
            return mKeysById.get(keyId);
        } else {
            return mKeysByType.get(GPKeyType.MASTER);
        }
    }

    /**
     * Store a key into the keyset
     *
     * @param key to store
     */
    public void putKey(GPKey key) {
        int keyId = key.getId();
        GPKeyType keyType = key.getType();
        if (mKeysByType.containsKey(keyType)) {
            throw new IllegalArgumentException("Key set " + mName + " already has a " + keyType + " key");
        }
        if(keyId != 0) {
            if (mKeysById.containsKey(keyId)) {
                throw new IllegalArgumentException("Key set " + mName + " already has key with id " + keyId);
            }
        }
        mKeys.add(key);
        mKeysByType.put(keyType, key);
        if(keyId != 0) {
            mKeysById.put(keyId, key);
        }
    }

    private static final GPKeyType[] DIVERSIFICATION_KEYS = {
            GPKeyType.ENC, GPKeyType.MAC, GPKeyType.KEK, GPKeyType.RMAC
    };

    /**
     * Perform key diversification on the keyset
     * <p/>
     * Will generate and return a new set of diversified keys.
     * <p/>
     * @param diversification function to be used
     * @param diversificationData for diversification
     * @return keyset containing the diversified keys
     */
    public GPKeySet diversify(GPKeyDiversification diversification, byte[] diversificationData) {
        if (mDiversification != GPKeyDiversification.NONE) {
            throw new IllegalArgumentException("Cannot diversify a diversified keyset");
        }
        if(diversification == GPKeyDiversification.NONE) {
            return this;
        }
        String diversifiedName = mName + "-" + diversification.name() + ":" + HexUtil.bytesToHex(diversificationData);
        GPKeySet diversifiedKeys = new GPKeySet(diversifiedName, mKeyVersion, diversification);
        switch(diversification) {
            case EMV:
                for(GPKeyType type: DIVERSIFICATION_KEYS) {
                    GPKey key = getKeyByType(type);
                    if(key != null) {
                        diversifiedKeys.putKey(diversifyKeyEMV(type, getKeyByType(type), diversificationData));
                    }
                }
                break;
            case VISA2:
                for(GPKeyType type: DIVERSIFICATION_KEYS) {
                    GPKey key = getKeyByType(type);
                    if(key != null) {
                        diversifiedKeys.putKey(diversifyKeyVisa2(type, getKeyByType(type), diversificationData));
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException("Diversification " + diversification + " not supported");
        }
        return diversifiedKeys;
    }

    private GPKey diversifyKeyEMV(GPKeyType type, GPKey key, byte[] diversificationData) {
        byte[] data = new byte[16];
        System.arraycopy(diversificationData, 4, data, 0, 6);
        data[6] = (byte)0xF0;
        data[7] = type.diversifyId;
        System.arraycopy(diversificationData, 4, data, 8, 6);
        data[14] = (byte)0x0F;
        data[15] = type.diversifyId;
        byte[] dKey = GPCrypto.enc_3des_ecb(key, data);
        return new GPKey(type, key.getId(), key.getCipher(), dKey);
    }

    private GPKey diversifyKeyVisa2(GPKeyType type, GPKey key, byte[] diversificationData) {
        byte[] data = new byte[16];
        System.arraycopy(diversificationData, 0, data, 0, 2);
        System.arraycopy(diversificationData, 4, data, 2, 4);
        data[6] = (byte)0xF0;
        data[7] = type.diversifyId;
        System.arraycopy(diversificationData, 0, data, 8, 2);
        System.arraycopy(diversificationData, 4, data, 10, 4);
        data[14] = (byte)0x0F;
        data[15] = type.diversifyId;
        byte[] dKey = GPCrypto.enc_3des_ecb(key, data);
        return new GPKey(type, key.getId(), key.getCipher(), dKey);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("keyset \"" + mName + "\"");
        sb.append(" version " + mKeyVersion);
        if (mKeys.isEmpty()) {
            sb.append(":\n EMPTY");
        } else {
            sb.append(":");
            for (GPKey key : mKeys) {
                sb.append("\n ");
                sb.append(key.toString());
            }
        }
        return sb.toString();
    }

}
