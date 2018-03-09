package better.smartcard.util;

import java.util.Arrays;

/**
 * Representation for ISO7816 AIDs and RIDs
 */
public class AID implements VerboseString {

    private static final int RID_OFFSET =  0;
    private static final int RID_LENGTH =  5;
    private static final int PIX_OFFSET =  5;
    private static final int PIX_LENGTH = 11;

    /** Binary value of the AID */
    private final byte[] mBytes;

    /**
     * Construct an AID from a byte array
     * @param bytes indicating the AID
     */
    public AID(byte[] bytes) {
        if(bytes.length < RID_LENGTH) {
            throw new IllegalArgumentException("AID must be 5 bytes long");
        }
        mBytes = bytes.clone();
    }

    /**
     * Construct an AID from part of a byte array
     * @param bytes containing the AID
     * @param offset of the AID
     * @param length of the AID
     */
    public AID(byte[] bytes, int offset, int length) {
        if(length < RID_LENGTH) {
            throw new IllegalArgumentException("AID must be 5 bytes long");
        }
        mBytes = Arrays.copyOfRange(bytes, offset, offset + length);
    }

    /**
     * Construct an AID from a hex string
     * @param hex string indicating the AID
     */
    public AID(String hex) {
        mBytes = HexUtil.hexToBytes(hex);
    }

    /** @return length of the AID */
    public int getLength() {
        return mBytes.length;
    }

    /** @return binary representation of the AID */
    public byte[] getBytes() {
        return mBytes.clone();
    }

    /** @return the RID part of the AID as an object */
    public AID getRID() {
        return new AID(getRIDBytes());
    }

    public int getRIDLength() {
        return RID_LENGTH;
    }

    /** @return the RID part of the AID as a byte array */
    public byte[] getRIDBytes() {
        byte[] res = new byte[RID_LENGTH];
        System.arraycopy(mBytes, RID_OFFSET, res, 0, RID_LENGTH);
        return res;
    }

    public int getPIXLength() {
        return mBytes.length - RID_LENGTH;
    }

    public byte[] getPIXBytes() {
        int len = getPIXLength();
        byte[] res = new byte[PIX_LENGTH];
        System.arraycopy(mBytes, PIX_OFFSET, res, 0, len);
        return res;
    }

    /** @return the RID part of the AID as a hex string */
    public String getRIDString() {
        return HexUtil.bytesToHex(getRIDBytes());
    }

    /** Stringify the AID */
    public String toString() {
        return HexUtil.bytesToHex(mBytes);
    }

    /** Equality by value */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AID aid = (AID) o;
        return Arrays.equals(mBytes, aid.mBytes);
    }

    /** Hash of value */
    @Override
    public int hashCode() {
        return Arrays.hashCode(mBytes);
    }

    /**
     * Construct an AID from a string-serialized byte array
     *
     * This format is used in CAP manifests.
     *
     * @param string indicating the AID
     * @return an appropriate instance
     */
    public static AID fromArrayString(String string) {
        String hex = string.replace("0x", "").replace(":", "");
        return new AID(hex);
    }

    @Override
    public String toVerboseString() {
        byte[] rid = getRIDBytes();
        byte[] pix = getPIXBytes();
        String res = "";
        res += HexUtil.bytesToHex(rid);
        if(pix.length > 0) {
            res += HexUtil.bytesToHex(pix);
        }
        return res;
    }

}
