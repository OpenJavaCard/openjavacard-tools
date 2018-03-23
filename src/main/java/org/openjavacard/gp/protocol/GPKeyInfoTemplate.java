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
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.tlv.TLV;
import org.openjavacard.tlv.TLVConstructed;
import org.openjavacard.tlv.TLVPrimitive;

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

    private static final int TAG_KEY_INFO_TEMPLATE = 0xE000;
    private static final int TAG_KEY_INFO = 0xC000;

    List<GPKeyInfo> mKeyInfos = new ArrayList<>();

    public GPKeyInfoTemplate() {
    }

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

    public void read(byte[] buf) throws IOException {
        // kit is a constructed TLV
        TLVConstructed kitTlv = TLVConstructed.readConstructed(buf).asConstructed(TAG_KEY_INFO_TEMPLATE);
        // collect key infos
        ArrayList<GPKeyInfo> infos = new ArrayList<>();
        // for each child
        for (TLV kiTlv : kitTlv.getChildren()) {
            // check its tag
            TLVPrimitive ki = kiTlv.asPrimitive(TAG_KEY_INFO);
            // parse
            GPKeyInfo info = new GPKeyInfo();
            info.read(ki.getValueBytes());
            // add
            infos.add(info);
        }
        // update fields
        mKeyInfos = infos;
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

}
