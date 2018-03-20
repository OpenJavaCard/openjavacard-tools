package org.openjavacard.tlv;

import org.openjavacard.util.HexUtil;

public class TLV {

    public static final byte[] encode(int tag, byte[] data) {
        return new TLV(tag, data).getEncoded();
    }

    int mTag;

    byte[] mData;

    public TLV(int tag, byte[] data) {
        mTag = tag;
        mData = data;
    }

    public int getTag() {
        return mTag;
    }

    public int getLength() {
        return mData.length;
    }

    public byte[] getData() {
        return mData.clone();
    }

    public void setData(byte[] data) {
        mData = data.clone();
    }

    public int getEncodedLength() {
        return TLVUtil.sizeTag(mTag)
                + TLVUtil.sizeLength(mData.length)
                + mData.length;
    }

    public byte[] getEncoded() {
        int length = getEncodedLength();
        byte[] encoded = new byte[length];
        int off = 0;
        int tagSize = TLVUtil.putTag(encoded, off, mTag);
        off += tagSize;
        int lenSize = TLVUtil.putLength(encoded, off, mData.length);
        off += lenSize;
        System.arraycopy(mData, 0, encoded, off, mData.length);
        return encoded;
    }

    public String toString() {
        return "TLV(T=" + Integer.toHexString(mTag) + ",L=" + mData.length + ",V=" + HexUtil.bytesToHex(mData) + ")";
    }

}
