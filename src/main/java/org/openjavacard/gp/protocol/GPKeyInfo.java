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
import org.openjavacard.tlv.TLVReader;
import org.openjavacard.util.HexUtil;

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
public class GPKeyInfo {

    private static final int TAG_KEY_INFO_TEMPLATE = 0xE000;
    private static final int TAG_KEY_INFO = 0xC000;

    List<GPKeyInfoEntry> mKeyInfos = new ArrayList<>();

    public GPKeyInfo() {
    }

    public List<GPKeyInfoEntry> getKeyInfos() {
        return new ArrayList<>(mKeyInfos);
    }

    public boolean matchesKeysetForUsage(GPKeySet keys) {
        for (GPKeyInfoEntry keyInfo : mKeyInfos) {
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
        for (GPKeyInfoEntry keyInfo : mKeyInfos) {
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
        TLV kitTlv = TLV.readRecursive(buf).asConstructed();
        if(kitTlv.getTag() != TAG_KEY_INFO_TEMPLATE) {
            throw new Error("Invalid foo");
        }
        List<TLV> kiTlvs = kitTlv.getChildren();
        ArrayList<GPKeyInfoEntry> infos = new ArrayList<>();
        for (TLV kiTlv : kiTlvs) {
            int tag = kiTlv.getTag();
            if (tag != TAG_KEY_INFO) {
                throw new Error("Invalid key info template - unknown tag "
                        + HexUtil.hex16(tag) + ", expected " + HexUtil.hex16(TAG_KEY_INFO));
            } else {
                GPKeyInfoEntry info = new GPKeyInfoEntry();
                info.read(kiTlv.getValueBytes());
                infos.add(info);
            }
        }
        mKeyInfos = infos;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GP Key Information Template:");
        for (GPKeyInfoEntry info : mKeyInfos) {
            sb.append("\n  ");
            sb.append(info.toString());
        }
        return sb.toString();
    }

}
