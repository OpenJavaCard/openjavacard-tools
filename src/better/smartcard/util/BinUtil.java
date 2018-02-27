package better.smartcard.util;

/** Utilities related to treating integers as binary words */
public class BinUtil {

    public static final short makeShort(byte b1, byte b2) {
        return (short) (((short) b1 << 8) + ((short) b2 & 0xff));
    }

    public static final short getShort(byte buf[], int off)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        return (short) (((short) buf[off] << 8) + ((short) buf[off + 1] & 0xff));
    }

    public static final short setShort(byte buf[], int off, short sValue)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        buf[off] = (byte) (sValue >> 8);
        buf[off + 1] = (byte) sValue;
        return (short) (off + 2);
    }

}
