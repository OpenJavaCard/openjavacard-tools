package better.smartcard.debug.record;

import better.smartcard.util.HexUtil;

public class LogEntry {

    public final byte  tag;
    public final byte  level;
    public final byte[] data;

    public LogEntry(byte tag, byte level, byte[] data) {
        this.tag = tag;
        this.level = level;
        this.data = data;
    }

    public String toString() {
        return "tag " + tag + " level " + level + " data " + HexUtil.bytesToHex(data);
    }

    public static LogEntry fromBytes(byte[] buf) {
        if(buf.length < 3) {
            throw new Error("Too short");
        }
        int off = 0;
        byte tag = buf[off++];
        byte level = buf[off++];
        byte len = buf[off++];
        byte[] data = new byte[len];
        System.arraycopy(buf, off, data, 0, len);
        return new LogEntry(tag, level, data);
    }
}