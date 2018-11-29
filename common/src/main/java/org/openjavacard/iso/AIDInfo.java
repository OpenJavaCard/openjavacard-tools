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
        // GlobalPlatform / OpenPlatform
        addProtected("a000000003000000", "Visa ISD");
        addProtected("a0000000035350",   "Visa SSD Package");
        addProtected("a000000151000000", "GlobalPlatform ISD");
        addProtected("a0000001515350",   "GlobalPlatform SSD Package");

        // NFC Forum (under RID provided by NXP Germany)
        addDescription("D2760000850101",   "NDEF Type 4 Tag");

        // Free Software Foundation in Europe
        addDescription("D27600012401",     "fsfEurope OpenPGP");
        addDescription("D2760001240101",   "fsfEurope OpenPGP V1");
        addDescription("D2760001240102",   "fsfEurope OpenPGP V2");
        addDescription("D27600012402",     "fsfEurope SmartChess");

        // OpenJavaCard project (under RID provided by signal interrupt)
        addDescription("D2760001771001", "OpenJavaCard Applications");
        addDescription("D2760001771002", "OpenJavaCard Packages");
        addDescription("D2760001771003", "OpenJavaCard Libraries");
        addDescription("D2760001771004", "OpenJavaCard Domains");
        addDescription("D27600017710020101", "OpenJavaCard NDEF (full plain)");
        addDescription("D27600017710020102", "OpenJavaCard NDEF (stub plain)");
        addDescription("D27600017710020103", "OpenJavaCard NDEF (tiny plain)");
        addDescription("D2760001771002010101", "OpenJavaCard NDEF (full plain)");
        addDescription("D2760001771002010201", "OpenJavaCard NDEF (stub plain)");
        addDescription("D2760001771002010301", "OpenJavaCard NDEF (tiny plain)");
        addDescription("D27600017710020111", "OpenJavaCard NDEF (full proguard)");
        addDescription("D27600017710020112", "OpenJavaCard NDEF (stub proguard)");
        addDescription("D27600017710020113", "OpenJavaCard NDEF (tiny proguard)");
        addDescription("D2760001771002011101", "OpenJavaCard NDEF (full proguard)");
        addDescription("D2760001771002011201", "OpenJavaCard NDEF (stub proguard)");
        addDescription("D2760001771002011301", "OpenJavaCard NDEF (tiny proguard)");

        // self-assigned space provided by signal interrupt
        addDescription("D276000177E0", "Self-Assigned Experimental Applications (signal interrupt)");
        addDescription("D276000177E1", "Self-Assigned Experimental Packages (signal interrupt)");
        addDescription("D276000177E2", "Self-Assigned Experimental Libraries (signal interrupt");
        addDescription("D276000177E3", "Self-Assigned Experimental Domains (signal interrupt");
        addDescription("D276000177F0", "Self-Assigned Production Applications (signal interrupt)");
        addDescription("D276000177F1", "Self-Assigned Production Packages (signal interrupt)");
        addDescription("D276000177F2", "Self-Assigned Production Libraries (signal interrupt)");
        addDescription("D276000177F3", "Self-Assigned Production Domains (signal interrupt)");
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
