package better.smartcard.debug.record;

import better.smartcard.util.BinUtil;

public class LogStatus {

    final short capacity;
    final short level;
    final LogClient[] clients;

    private LogStatus(short capacity, short level) {
        this.capacity = capacity;
        this.level = level;
        this.clients = new LogClient[0];
    }

    public String toString() {
        return "capacity " + capacity + " level " + level;
    }

    public static LogStatus fromBytes(byte[] buf) {
        if(buf.length < 5) {
            throw new Error("Too short");
        }
        short capacity = BinUtil.getShort(buf, 0);
        short level = BinUtil.getShort(buf, 2);
        byte numClients = buf[4];
        return new LogStatus(capacity, level);
    }
}
