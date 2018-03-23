package org.openjavacard.tlv;

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

}
