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

import org.openjavacard.gp.crypto.GPBouncy;
import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.BinUtil;
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

    private static final GPKeySet buildGeneric(String name, byte[] masterKey) {
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

    /** @return the version of this keyset */
    public int getKeyVersion() {
        return mKeyVersion;
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

    /**
     * Key type sequence for SCP02
     */
    private static final GPKeyType[] SCP02_KEYS = {
        GPKeyType.ENC, GPKeyType.MAC, GPKeyType.KEK
    };

    /**
     * Table containing SCP02 session key derivation constants
     */
    private static final Hashtable<GPKeyType, byte[]> SCP02_DERIVE = new Hashtable<>();

    private static final byte[] SCP02_DERIVE_MAC  = {0x01, 0x01};
    private static final byte[] SCP02_DERIVE_RMAC = {0x01, 0x02};
    private static final byte[] SCP02_DERIVE_KEK = {0x01, (byte) 0x81};
    private static final byte[] SCP02_DERIVE_ENC = {0x01, (byte) 0x82};

    static {
        SCP02_DERIVE.put(GPKeyType.MAC,  SCP02_DERIVE_MAC);
        SCP02_DERIVE.put(GPKeyType.RMAC, SCP02_DERIVE_RMAC);
        SCP02_DERIVE.put(GPKeyType.KEK,  SCP02_DERIVE_KEK);
        SCP02_DERIVE.put(GPKeyType.ENC,  SCP02_DERIVE_ENC);
    }

    /**
     * Derives a set of SCP02 session keys from this keyset
     *
     * @param sequence to be used for the computation
     * @return a new keyset containing the derived keys
     */
    public GPKeySet deriveSCP02(byte[] sequence) {
        // synthesize a name for the new keyset
        String name = mName + "-SCP02:" + HexUtil.bytesToHex(sequence);

        // create the new set
        GPKeySet derivedSet = new GPKeySet(name, mKeyVersion, mDiversification);

        // initialize buffer for derivation
        byte[] buffer = new byte[16];
        System.arraycopy(sequence, 0, buffer, 2, 2);

        // go through all keys
        for (GPKeyType type : SCP02_KEYS) {
            // get the static base key
            GPKey staticKey = getKeyByType(type);
            if(staticKey != null) {
                // insert derivation data
                byte[] derivation = SCP02_DERIVE.get(type);
                System.arraycopy(derivation, 0, buffer, 0, 2);
                // derive using DES
                byte[] derived = GPCrypto.enc_3des_cbc_nulliv(staticKey, buffer);
                // construct the new key
                GPKey sessionKey = new GPKey(type, staticKey.getId(),
                        GPKeyCipher.DES3, derived);
                // insert key into new set
                derivedSet.putKey(sessionKey);
            }
        }

        // return the new set
        return derivedSet;
    }

    private static final GPKeyType[] SCP03_KEYS = {
            GPKeyType.ENC, GPKeyType.MAC, GPKeyType.RMAC
    };

    private static final Hashtable<GPKeyType, Byte> SCP03_KDF_CONSTANTS = new Hashtable<>();
    private static final Hashtable<GPKeyType, byte[]> SCP03_KDF_DERIVE = new Hashtable<>();

    private static final byte[] SCP03_DERIVE_ENC = HexUtil.hexToBytes("0000000100");
    private static final byte[] SCP03_DERIVE_MAC = HexUtil.hexToBytes("0000000200");
    private static final byte[] SCP03_DERIVE_KEK = HexUtil.hexToBytes("0000000300");

    static {
        SCP03_KDF_CONSTANTS.put(GPKeyType.ENC,  (byte)0x04);
        SCP03_KDF_CONSTANTS.put(GPKeyType.MAC,  (byte)0x06);
        SCP03_KDF_CONSTANTS.put(GPKeyType.RMAC, (byte)0x07);
        SCP03_KDF_DERIVE.put(GPKeyType.ENC, SCP03_DERIVE_ENC);
        SCP03_KDF_DERIVE.put(GPKeyType.MAC, SCP03_DERIVE_MAC);
        SCP03_KDF_DERIVE.put(GPKeyType.KEK, SCP03_DERIVE_KEK);
    }

    public GPKeySet deriveSCP03(byte[] sequence, byte[] hostChallenge, byte[] cardChallenge) {
        // synthesize a name for the new keyset
        String name = mName + "-SCP03:" + HexUtil.bytesToHex(sequence);

        byte[] context = ArrayUtil.concatenate(hostChallenge, cardChallenge);

        // create the new set
        GPKeySet derivedSet = new GPKeySet(name, mKeyVersion, mDiversification);

        // go through all keys
        for (GPKeyType type : SCP03_KEYS) {
            // get the static base key
            GPKey staticKey = getKeyByType(type);
            if(staticKey != null) {
                GPKey sessionKey;
                if(SCP03_KDF_CONSTANTS.containsKey(type)) {
                    // perform derivation
                    byte constant = SCP03_KDF_CONSTANTS.get(type);
                    byte[] sessionSecret = GPBouncy.scp03_kdf(staticKey, constant, context, staticKey.getLength() * 8);
                    sessionKey = new GPKey(type, staticKey.getId(), GPKeyCipher.AES, sessionSecret);
                } else {
                    // copy keys that need no derivation
                    sessionKey = new GPKey(type, staticKey.getId(), GPKeyCipher.AES, staticKey.getSecret());
                }
                derivedSet.putKey(sessionKey);
            }
        }

        // return the new set
        return derivedSet;
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
