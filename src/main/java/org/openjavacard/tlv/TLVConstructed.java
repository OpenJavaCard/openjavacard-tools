/*
 * openjavacard-tools: OpenJavaCard Development Tools
 * Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

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
