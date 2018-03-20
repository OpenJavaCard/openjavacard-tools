package org.openjavacard.tlv;

public class TLVLength {

    private static final int LENGTH_LONG_MASK = 0x80;
    private static final int LENGTH_LONG_FLAG = 0x80;
    private static final int LENGTH_SIZE_MASK = 0x7F;

    public static final boolean isLongForm(int firstByte) {
        return (firstByte & LENGTH_LONG_MASK) == LENGTH_LONG_FLAG;
    }

    public static final int longLength(int firstByte) {
        return (firstByte & ~LENGTH_SIZE_MASK);
    }

}
