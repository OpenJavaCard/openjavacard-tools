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

package org.openjavacard.iso;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    private static final void addProtected(String aid, String label, AIDUsage usage) {
        AIDUsage[] usageArray = new AIDUsage[] {usage};
        AIDInfo info = new AIDInfo(new AID(aid), label, true, usageArray, null);
        KNOWN.put(info.aid, info);
    }

    private static final void addDescription(String aid, String label, AIDUsage usage) {
        AIDUsage[] usageArray = new AIDUsage[] {usage};
        AIDInfo info = new AIDInfo(new AID(aid), label, false, usageArray, null);
        KNOWN.put(info.aid, info);
    }

    private static final void addDescription(String aid, String label, AIDUsage usage, AIDUsage prefixUsage) {
        AIDUsage[] usageArray = new AIDUsage[] {usage};
        AIDUsage[] prefixUsageArray = new AIDUsage[] {prefixUsage};
        AIDInfo info = new AIDInfo(new AID(aid), label, false, usageArray, prefixUsageArray);
        KNOWN.put(info.aid, info);
    }

    static {

        // Visa International
        addDescription("A000000003",       "Visa International", AIDUsage.PREFIX);
        // OpenPlatform (early GlobalPlatform, under RID provided by Visa)
        addProtected(  "A000000003000000", "OpenPlatform ISD", AIDUsage.DOMAIN);
        addProtected(  "A0000000035350",   "OpenPlatform SSD Package", AIDUsage.PACKAGE);

        // GlobalPlatform
        addDescription("A000000151",       "GlobalPlatform", AIDUsage.PREFIX);
        addProtected(  "A000000151000000", "GlobalPlatform ISD", AIDUsage.DOMAIN);
        addProtected(  "A0000001515350",   "GlobalPlatform SSD Package", AIDUsage.PACKAGE);

        // Yubico
        addDescription("A000000527",       "Yubico", AIDUsage.PREFIX);
        addDescription("A000000527210101", "Yubikey OATH", AIDUsage.APPLET);

        // NXP Germany
        addDescription("D276000085",       "NXP Germany", AIDUsage.PREFIX);
        // NFC Forum (under RID provided by NXP Germany)
        addDescription("D2760000850101",   "NDEF Type 4 Tag", AIDUsage.APPLET);

        // FSF Europe
        addDescription("D276000124",       "fsfEurope", AIDUsage.PREFIX);
        addDescription("D27600012401",     "fsfEurope OpenPGP", AIDUsage.PREFIX);
        addDescription("D2760001240101",   "fsfEurope OpenPGP V1", AIDUsage.APPLET);
        addDescription("D2760001240102",   "fsfEurope OpenPGP V2", AIDUsage.APPLET);
        addDescription("D2760001240103",   "fsfEurope OpenPGP V3", AIDUsage.APPLET);
        addDescription("D27600012402",     "fsfEurope SmartChess", AIDUsage.PREFIX);

        // OpenJavaCard project (under RID provided by signal interrupt)
        addDescription("D27600017710",   "OpenJavaCard", AIDUsage.PREFIX);

        addDescription("D2760001771001", "OpenJavaCard Applications", AIDUsage.PREFIX, AIDUsage.APPLET);
        addDescription("D276000177100110", "OpenJavaCard Libraries", AIDUsage.PREFIX, AIDUsage.APPLET);
        addDescription("D27600017710011001", "OpenJavaCard Libraries (Demo applet)", AIDUsage.APPLET);
        addDescription("D27600017710011020", "OpenJavaCard Libraries (Debug service)", AIDUsage.APPLET);
        addDescription("D27600017710011021", "OpenJavaCard Libraries (Random service)", AIDUsage.APPLET);

        addDescription("D2760001771002", "OpenJavaCard Install Packages", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D276000177100210", "OpenJavaCard Libraries", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D27600017710021001", "OpenJavaCard Libraries (Demo applet)", AIDUsage.PACKAGE);
        addDescription("D27600017710021020", "OpenJavaCard Libraries (Debug service)", AIDUsage.PACKAGE);
        addDescription("D27600017710021021", "OpenJavaCard Libraries (Random service)", AIDUsage.PACKAGE);
        addDescription("D27600017710021101", "OpenJavaCard NDEF (full plain)", AIDUsage.PACKAGE);
        addDescription("D27600017710021102", "OpenJavaCard NDEF (stub plain)", AIDUsage.PACKAGE);
        addDescription("D27600017710021103", "OpenJavaCard NDEF (tiny plain)", AIDUsage.PACKAGE);
        addDescription("D27600017710021111", "OpenJavaCard NDEF (full proguard)", AIDUsage.PACKAGE);
        addDescription("D27600017710021112", "OpenJavaCard NDEF (stub proguard)", AIDUsage.PACKAGE);
        addDescription("D27600017710021113", "OpenJavaCard NDEF (tiny proguard)", AIDUsage.PACKAGE);
        addDescription("D27600017710022001", "OpenJavaCard YKNeo OpenPGP (standard)", AIDUsage.PACKAGE);
        addDescription("D27600017710022101", "OpenJavaCard YKNeo OATH (standard)", AIDUsage.PACKAGE);
        addDescription("D27600017710022201", "OpenJavaCard IsoApplet (standard)", AIDUsage.PACKAGE);

        addDescription("D2760001771003", "OpenJavaCard Library Packages", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D276000177100310", "OpenJavaCard Libraries", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D27600017710031001", "OpenJavaCard Libraries (BER processing)", AIDUsage.PACKAGE);
        addDescription("D27600017710031002", "OpenJavaCard Libraries (String processing)", AIDUsage.PACKAGE);
        addDescription("D27600017710031003", "OpenJavaCard Libraries (Fortuna PRNG)", AIDUsage.PACKAGE);
        addDescription("D27600017710031004", "OpenJavaCard Libraries (RSA extensions)", AIDUsage.PACKAGE);
        addDescription("D27600017710031005", "OpenJavaCard Libraries (Debug utilities)", AIDUsage.PACKAGE);
        addDescription("D27600017710031006", "OpenJavaCard Libraries (Auth framework)", AIDUsage.PACKAGE);
        addDescription("D27600017710031007", "OpenJavaCard Libraries (Password functions)", AIDUsage.PACKAGE);

        addDescription("D2760001771004", "OpenJavaCard Security Domains", AIDUsage.PREFIX, AIDUsage.DOMAIN);

        // signal interrupt
        addDescription("D276000177",   "signal interrupt", AIDUsage.PREFIX);
        addDescription("D276000177E0", "Self-Assigned Experimental Applications", AIDUsage.PREFIX, AIDUsage.APPLET);
        addDescription("D276000177E1", "Self-Assigned Experimental Packages", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D276000177E2", "Self-Assigned Experimental Libraries", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D276000177E3", "Self-Assigned Experimental Domains", AIDUsage.PREFIX, AIDUsage.DOMAIN);
        addDescription("D276000177F0", "Self-Assigned Production Applications", AIDUsage.PREFIX, AIDUsage.APPLET);
        addDescription("D276000177F1", "Self-Assigned Production Packages", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D276000177F2", "Self-Assigned Production Libraries", AIDUsage.PREFIX, AIDUsage.PACKAGE);
        addDescription("D276000177F3", "Self-Assigned Production Domains", AIDUsage.PREFIX, AIDUsage.DOMAIN);
    }

    /** AID for this information object */
    public final AID aid;

    /** Short label for this object */
    public final String label;

    /** True if instances should be protected from inadvertent deletion */
    public final boolean protect;

    /** Intended usage for the AID or its children */
    public final Set<AIDUsage> usage;
    /** Intended usage for children in case of an explicit PREFIX */
    public final Set<AIDUsage> prefixUsage;

    public AIDInfo(AID aid, String label, boolean protect, AIDUsage[] usage, AIDUsage[] prefixUsage) {
        this.aid = aid;
        this.label = label;
        this.protect = protect;
        if(usage != null) {
            this.usage = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(usage)));
        } else {
            this.usage = Collections.emptySet();
        }
        if(prefixUsage != null) {
            this.prefixUsage = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(prefixUsage)));
        } else {
            this.prefixUsage = Collections.emptySet();
        }
    }

}
