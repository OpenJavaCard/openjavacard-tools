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

    private static final void addProtected(String aid, String label) {
        AIDInfo info = new AIDInfo(new AID(aid), label, true);
        KNOWN.put(info.aid, info);
    }

    private static final void addDescription(String aid, String label) {
        AIDInfo info = new AIDInfo(new AID(aid), label, false);
        KNOWN.put(info.aid, info);
    }

    static {
        addProtected("a000000003000000", "Visa ISD");
        addProtected("a0000000035350",   "Visa SSD Package");
        addProtected("a000000151000000", "GlobalPlatform ISD");
        addProtected("a0000001515350",   "GlobalPlatform SSD Package");
        addDescription("D2760000850101",   "NDEF Type 4 Tag");
        addDescription("D27600012401",     "fsfEurope OpenPGP");
        addDescription("D2760001240101",   "fsfEurope OpenPGP V1");
        addDescription("D2760001240102",   "fsfEurope OpenPGP V2");
        addDescription("D27600012402",     "fsfEurope SmartChess");
    }

    public final AID aid;
    public final String label;
    public final boolean protect;

    public AIDInfo(AID aid, String label, boolean protect) {
        this.aid = aid;
        this.label = label;
        this.protect = protect;
    }

}
