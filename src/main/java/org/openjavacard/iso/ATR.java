package org.openjavacard.iso;

public class ATR {

    private static final byte MIN_LENGTH = 2;
    private static final byte MAX_LENGTH = 33;

    private static final byte OFFSET_TS = 0;
    private static final byte OFFSET_T0 = 1;

    private static final byte SUBOFFSET_TA = 0;
    private static final byte SUBOFFSET_TB = 1;
    private static final byte SUBOFFSET_TC = 2;
    private static final byte SUBOFFSET_TD = 3;

    private static final byte TS_DIRECT_CONVENTION = 0x3B;
    private static final byte TS_INVERSE_CONVENTION = 0x3F;

    private static final int[] TA1L_DI = new int[] {
            -1,
            1,
            2,
            4,
            8,
            16,
            32,
            64,
            12,
            20,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
    };

    private static final int[] TA1H_FI = new int[] {
            372,
            372,
            558,
            744,
            1116,
            1488,
            1860,
            -1,
            -1,
            512,
            768,
            1024,
            1536,
            2048,
            -1,
            -1,
    };

    private static final int[] TA1H_FMAX = new int[] {
            4000000,
            5000000,
            6000000,
            8000000,
            12000000,
            16000000,
            20000000,
            -1,
            -1,
            5000000,
            7500000,
            10000000,
            15000000,
            20000000,
            -1,
            -1,
    };


    private final byte[] mData;


    public ATR(byte[] data) {
        // check for valid length range
        if (data.length < MIN_LENGTH || data.length > MAX_LENGTH) {
            throw new IllegalArgumentException("ATR must be between 2 and 33 bytes long");
        }
        mData = data;
    }

}
