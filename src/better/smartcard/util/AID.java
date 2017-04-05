package better.smartcard.util;

import java.util.Arrays;

/**
 * Representation for ISO7816 AIDs and RIDs
 */
public class AID {

    /** The length of the RID-prefix common to all AIDs */
    private static final int RID_LENGTH = 5;

    /** Binary value of the AID */
    byte[] mBytes;

    /**
     * Construct an AID from a byte array
     * @param bytes indicating the AID
     */
    public AID(byte[] bytes) {
        mBytes = bytes;
    }

    /**
     * Construct an AID from part of a byte array
     * @param bytes containing the AID
     * @param offset of the AID
     * @param length of the AID
     */
    public AID(byte[] bytes, int offset, int length) {
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

    /** @return the RID part of the AID as a byte array */
    public byte[] getRIDBytes() {
        byte[] res = new byte[RID_LENGTH];
        System.arraycopy(mBytes, 0, res, 0, RID_LENGTH);
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

}
