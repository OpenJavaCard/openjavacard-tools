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

package org.openjavacard.gp.protocol;

import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.tlv.TLV;
import org.openjavacard.tlv.TLVConstructed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * GlobalPlatform Key Information Template
 * <p/>
 * This describes the set of keys that a card holds
 * and/or expects from potential secure hosts.
 * <p/>
 * It contains several Key Information blocks,
 * each of which describes one key with its own ID.
 * <p/>
 */
public class GPKeyInfoTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(GPKeyInfoTemplate.class);

    /** Tag of GP key information templates */
    private static final int TAG_KEY_INFO_TEMPLATE = 0xE000;

    /** Key infos in this template */
    private final ArrayList<GPKeyInfo> mKeyInfos;

    /**
     * Construct a Key Information Template
     * @param keyInfos to include
     */
    public GPKeyInfoTemplate(List<GPKeyInfo> keyInfos) {
        mKeyInfos = new ArrayList<>(keyInfos);
    }

    /** Return the Key Information objects in this template */
    public List<GPKeyInfo> getKeyInfos() {
        return new ArrayList<>(mKeyInfos);
    }

    /**
     * Check if the given GPKeySet matches this template for SCP usage
     *
     * Using a keyset requires that it contains all keys described by
     * the template, that the algorithms of the keys are compatible and
     * that the key version agrees with the template.
     *
     * Mapping of keys is performed by key ID.
     *
     * @param keys to check
     * @return true if usable
     */
    public void checkKeysetForUsage(GPKeySet keys) throws CardException {
        LOG.trace("checking keyset " + keys.getName());
        for (GPKeyInfo keyInfo : mKeyInfos) {
            int keyId = keyInfo.getKeyId();
            LOG.trace("checking key with id " + keyId);
            GPKey key = keys.getKeyById(keyId);
            if (key == null) {
                throw new CardException("No key found with id " + keyId);
            }
            int keyVersion = keys.getKeyVersion();
            if (keyVersion != 0 && keyInfo.getKeyVersion() != keyVersion) {
                throw new CardException("Wrong key version " + keyInfo.getKeyVersion()
                        + ", expected " + keyVersion);
            }
            if (!keyInfo.matchesKey(key)) {
                throw new CardException("Key is incompatible");
            }
        }
    }

    /**
     * Check if the given GPKeySet matches this template for key replacement
     *
     * Replacing keys requires that the set contains all keys described in
     * the template and that their algorithms match. Key versions are not
     * compared at all.
     *
     * Mapping of keys is performed by key ID.
     *
     * @param keys to check
     */
    public void checkKeySetForReplacement(GPKeySet keys) throws CardException {
        HashSet<GPKey> keysUsed = new HashSet<>();
        // try to satisfy each key info
        for (GPKeyInfo keyInfo : mKeyInfos) {
            int keyId = keyInfo.getKeyId();
            GPKey key = keys.getKeyById(keyId);
            if (key == null) {
                throw new CardException("Could not find key with id " + keyId);
            }
            if (!keyInfo.matchesKey(key)) {
                throw new CardException("Key with id " + keyId + " is incompatible");
            }
            keysUsed.add(key);
        }
        // check that all keys are used
        for(GPKey key: keys.getKeys()) {
            if(!keysUsed.contains(key)) {
                throw new CardException("Key not used by card template: " + key);
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GP Key Information Template:");
        for (GPKeyInfo info : mKeyInfos) {
            sb.append("\n  ");
            sb.append(info.toString());
        }
        return sb.toString();
    }

    /**
     * Parse a Key Information Template from bytes
     *
     * @param buf containing the KIT
     * @return a parsed instance
     * @exception IOException on parse error
     */
    public static GPKeyInfoTemplate fromBytes(byte[] buf) throws IOException {
        ArrayList<GPKeyInfo> infos = new ArrayList<>();
        // KIT is a constructed TLV
        TLVConstructed kitTlv = TLVConstructed.readConstructed(buf).asConstructed(TAG_KEY_INFO_TEMPLATE);
        // for each child
        for (TLV kiTlv : kitTlv.getChildren()) {
            // parse and add
            infos.add(GPKeyInfo.fromTLV(kiTlv));
        }
        // construct and return instance
        return new GPKeyInfoTemplate(infos);
    }

}
