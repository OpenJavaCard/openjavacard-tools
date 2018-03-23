package org.openjavacard.tlv;

public class TLVPrimitive extends TLV {

    private byte[] mData;

    public TLVPrimitive(int tag, byte[] data) {
        super(tag);
        mData = data;
    }

    @Override
    public int getValueLength() {
        return mData.length;
    }

    @Override
    public byte[] getValueBytes() {
        return mData;
    }

    @Override
    public String toVerboseString() {
        return null;
    }

}
