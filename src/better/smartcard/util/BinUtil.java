package better.smartcard.util;

import java.util.Arrays;

/** Utilities related to treating integers as binary words */
public class BinUtil {

    public static final byte makeByte(int v) {
        if(v > 255) {
            throw new IllegalArgumentException();
        }
        return (byte)(v & 0xFF);
    }

    public static final byte getByte(byte buf[], int off)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        return (byte)(buf[off] & 0xFF);
    }

    public static final short makeShort(int v) {
        if(v > 65535) {
            throw new IllegalArgumentException();
        }
        return (short)(v & 0xFFFF);
    }

    public static final short makeShort(byte b1, byte b2) {
        return (short) (((short) b1 << 8) + ((short) b2 & 0xff));
    }

    public static final byte getShortLowByte(short s) {
        return (byte)((s >> 0) & 0xFF);
    }

    public static final byte getShortHighByte(short s) {
        return (byte)((s >> 8) & 0xFF);
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

    public static byte[][] splitBlocks(byte[] data, int blockSize) {
        int numBytes = data.length;
        int numBlocks = numBytes / blockSize;
        if(numBytes % blockSize > 0) {
            numBlocks++;
        }
        byte[][] res = new byte [numBlocks][];
        int offset = 0;
        for(int i = 0; i < numBlocks; i++) {
            int size = blockSize;
            if(offset + size > numBytes) {
                size = numBytes - offset;
            }
            res[i] = Arrays.copyOfRange(data, offset, offset + size);
            offset += size;
        }
        return res;
    }

}
