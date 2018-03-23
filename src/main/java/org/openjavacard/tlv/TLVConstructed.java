package org.openjavacard.tlv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TLVConstructed extends TLV {

    private final List<TLV> mChildren;

    public TLVConstructed(int tag, List<TLV> children) {
        super(tag);
        mChildren = children;
    }

    @Override
    public int getChildCount() {
        return mChildren.size();
    }

    @Override
    public TLV getChild(int index) {
        return mChildren.get(index);
    }

    @Override
    public List<TLV> getChildren() {
        return mChildren;
    }

    @Override
    public String toVerboseString() {
        return null;
    }

    public static TLVConstructed readConstructed(byte[] data) throws IOException {
        return readConstructed(data, 0, data.length);
    }

    public static TLVConstructed readConstructed(byte[] data, int offset, int length) throws IOException {
        TLVReader reader = new TLVReader(data, offset, length);
        TLVConstructed result = reader.readConstructed();
        if(reader.hasMoreData()) {
            throw new IllegalArgumentException("More than one tag where only one was expected");
        }
        return result;
    }

    public static List<TLVConstructed> readConstructeds(byte[] data) throws IOException {
        return readConstructeds(data, 0, data.length);
    }

    public static List<TLVConstructed> readConstructeds(byte[] data, int offset, int length) throws IOException {
        ArrayList<TLVConstructed> res = new ArrayList<>();
        TLVReader reader = new TLVReader(data, offset, length);
        while(reader.hasMoreData()) {
            res.add(reader.readConstructed());
        }
        return res;
    }

}
