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

import java.io.IOException;
import java.util.ArrayList;
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

    public boolean matchesKeysetForUsage(GPKeySet keys) {
        for (GPKeyInfo keyInfo : mKeyInfos) {
            GPKey key = keys.getKeyById(keyInfo.getKeyId());
            if (key == null) {
                return false;
            }
            int keyVersion = keys.getKeyVersion();
            if (keyVersion != 0 && keyInfo.getKeyVersion() != keyVersion) {
                return false;
            }
            if (!keyInfo.matchesKey(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesKeysetForReplacement(GPKeySet keys) {
        for (GPKeyInfo keyInfo : mKeyInfos) {
            GPKey key = keys.getKeyById(keyInfo.getKeyId());
            if (key == null) {
                return false;
            }
            if (!keyInfo.matchesKey(key)) {
                return false;
            }
        }
        return true;
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

    /** Parse a Key Information Template from bytes */
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
