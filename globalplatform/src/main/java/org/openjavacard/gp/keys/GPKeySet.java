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

import org.openjavacard.gp.scp.SCPDiversification;
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

    /** An empty keyset */
    public static final GPKeySet EMPTY = new GPKeySet("Empty");

    /** GlobalPlatform default keyset */
    public static final GPKeySet GLOBALPLATFORM = buildGeneric("GlobalPlatform", GPKey.GLOBALPLATFORM_MASTER);

    private static GPKeySet buildGeneric(String name, GPKey masterKey) {
        GPKeySet keySet = new GPKeySet(name);
        keySet.putKey(masterKey);
        return keySet;
    }

    /** Human-readable name of the keyset */
    private String mName;

    /** Key version common to all keys in the set */
    private int mKeyVersion;

    /** Diversification common to all keys in the set */
    private GPKeyDiversification mDiversification;

    /** Primary list of keys */
    private final ArrayList<GPKey> mKeys = new ArrayList<>();
    /** Secondary table by key usage */
    private final Hashtable<GPKeyUsage, GPKey> mKeysByUsage = new Hashtable<>();
    /** Secondary table by key id */
    private final Hashtable<Integer, GPKey> mKeysById = new Hashtable<>();

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
        GPKeyVersion.checkKeyVersion(keyVersion);
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
     * Retrieves the key of the given usage type from the keyset
     *
     * @param usage of the key
     * @return the key or null
     */
    public GPKey getKeyByUsage(GPKeyUsage usage) {
        if(mKeysByUsage.containsKey(usage)) {
            return mKeysByUsage.get(usage);
        } else {
            return mKeysByUsage.get(GPKeyUsage.MASTER);
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
            return mKeysByUsage.get(GPKeyUsage.MASTER);
        }
    }

    /**
     * Store a key into the keyset
     *
     * @param key to store
     */
    public void putKey(GPKey key) {
        int keyId = key.getId();
        GPKeyUsage keyType = key.getUsage();
        // check for duplicate key usage
        if (mKeysByUsage.containsKey(keyType)) {
            throw new IllegalArgumentException("Key set " + mName + " already has a " + keyType + " key");
        }
        // check for duplicate key id
        if(keyId != 0) {
            if (mKeysById.containsKey(keyId)) {
                throw new IllegalArgumentException("Key set " + mName + " already has key with id " + keyId);
            }
        }
        // add key to main list
        mKeys.add(key);
        // save the key by usage
        mKeysByUsage.put(keyType, key);
        // if a key id was provided
        if(keyId != 0) {
            // save the key by id
            mKeysById.put(keyId, key);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("keyset \"" + mName + "\"");
        sb.append(" version " + (mKeyVersion==0?"any":mKeyVersion));
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
