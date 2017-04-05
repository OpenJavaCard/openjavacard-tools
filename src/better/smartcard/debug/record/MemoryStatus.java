package better.smartcard.debug.record;


import better.smartcard.util.BinUtil;

public class MemoryStatus {
    public final short persistent;
    public final short transientReset;
    public final short transientDeselect;
    public MemoryStatus(short p, short tR, short tD) {
        persistent = p;
        transientReset = tR;
        transientDeselect = tD;
    }
    public String toString() {
        return "persistent " + persistent
                + " transient-reset " + transientReset
                + " transient-deselect " + transientDeselect;
    }
    public static MemoryStatus fromBytes(byte[] data) {
        if(data.length != 6) {
            throw new Error("Bad length");
        }
        short p = BinUtil.getShort(data, 0);
        short tR = BinUtil.getShort(data, 2);
        short tD = BinUtil.getShort(data, 4);
        return new MemoryStatus(p, tR, tD);
    }
}