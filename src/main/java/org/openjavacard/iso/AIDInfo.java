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

package org.openjavacard.iso;

import java.util.Map;
import java.util.TreeMap;

public class AIDInfo {

    public static final AIDInfo get(AID aid) {
        Map.Entry<AID, AIDInfo> floor = KNOWN.floorEntry(aid);
        if(floor != null && aid.toString().startsWith(floor.getKey().toString())) {
            return floor.getValue();
        } else {
            return null;
        }
    }

    private static final TreeMap<AID, AIDInfo> KNOWN = new TreeMap<>();

    private static final void addKnown(String aid, String label) {
        AIDInfo info = new AIDInfo(new AID(aid), label);
        KNOWN.put(info.aid, info);
    }

    static {
        addKnown("a000000003000000", "Visa ISD");
        addKnown("a0000000035350",   "Visa SSD Package");
        addKnown("a000000151000000", "GlobalPlatform ISD");
        addKnown("a0000001515350",   "GlobalPlatform SSD Package");
        addKnown("D2760000850101",   "NDEF Type 4 Tag");
        addKnown("D27600012401",     "fsfEurope OpenPGP");
        addKnown("D2760001240101",   "fsfEurope OpenPGP V1");
        addKnown("D2760001240102",   "fsfEurope OpenPGP V2");
        addKnown("D27600012402",     "fsfEurope SmartChess");
    }

    public final AID aid;
    public final String label;

    public AIDInfo(AID aid, String label) {
        this.aid = aid;
        this.label = label;
    }

}
