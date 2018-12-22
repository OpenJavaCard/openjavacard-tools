/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
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
 */

package org.openjavacard.tlv;

import org.openjavacard.util.HexUtil;

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

    @Override
    public String toString() {
        return "[" + TLVTag.toString(mTag) + "]" + HexUtil.bytesToHex(mData);
    }

}
