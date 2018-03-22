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
        addKnown("a000000003000000", "GlobalPlatform ISD");
        addKnown("a0000000035350",   "GlobalPlatform SSD Package");
        addKnown("a000000151000000", "NXP ISD");
        addKnown("a0000001515350",   "NXP SSD Package");
        addKnown("D2760000850101",   "NDEF Type 4 Tag");
        addKnown("D27600012401",     "FSFE OpenPGP");
        addKnown("D27600012402",     "FSFE SmartChess");
    }

    public final AID aid;
    public final String label;

    public AIDInfo(AID aid, String label) {
        this.aid = aid;
        this.label = label;
    }

}
