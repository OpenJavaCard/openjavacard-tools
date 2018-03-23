package org.openjavacard.tlv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static TLVPrimitive readPrimitive(byte[] data) throws IOException {
        return readPrimitive(data, 0, data.length);
    }

    public static TLVPrimitive readPrimitive(byte[] data, int offset, int length) throws IOException {
        TLVReader reader = new TLVReader(data, offset, length);
        TLVPrimitive result = reader.readPrimitive();
        if(reader.hasMoreData()) {
            throw new IllegalArgumentException("More than one tag where only one was expected");
        }
        return result;
    }

    public static List<TLVPrimitive> readPrimitives(byte[] data) throws IOException {
        return readPrimitives(data, 0, data.length);
    }

    public static List<TLVPrimitive> readPrimitives(byte[] data, int offset, int length) throws IOException {
        ArrayList<TLVPrimitive> res = new ArrayList<>();
        TLVReader reader = new TLVReader(data, offset, length);
        while(reader.hasMoreData()) {
            res.add(reader.readPrimitive());
        }
        return res;
    }

}
