/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.tlv;

import org.openjavacard.util.VerboseString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class TLV implements VerboseString {

    private int mTag;

    protected TLV(int tag) {
        mTag = tag;
    }

    public int getTag() {
        return mTag;
    }

    public int getChildCount() {
        throw new UnsupportedOperationException();
    }

    public TLV getChild(int index) {
        throw new UnsupportedOperationException();
    }

    public List<TLV> getChildren() {
        throw new UnsupportedOperationException();
    }

    public int getValueLength() {
        throw new UnsupportedOperationException();
    }

    public byte[] getValueBytes() {
        throw new UnsupportedOperationException();
    }

    public int getEncodedLength() {
        int dataLength = getValueLength();
        return TLVTag.tagSize(mTag)
                + TLVLength.lengthSize(dataLength)
                + dataLength;
    }

    public byte[] getEncoded() throws IOException {
        int valueLength = getValueLength();
        byte[] valueBytes = getValueBytes();
        if(valueBytes.length != valueLength) {
            throw new InternalError("Value length inconsistent");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(TLVTag.tagBytes(mTag));
        bos.write(TLVLength.lengthBytes(valueLength));
        bos.write(valueBytes);
        return bos.toByteArray();
    }

    public TLVPrimitive asPrimitive() {
        if(this instanceof TLVPrimitive) {
            return (TLVPrimitive)this;
        } else {
            return new TLVPrimitive(mTag, getValueBytes());
        }
    }

    public TLVConstructed asConstructed() {
        if(this instanceof TLVConstructed) {
            return (TLVConstructed)this;
        } else {
            try {
                List<TLV> children = readRecursives(getValueBytes());
                return new TLVConstructed(mTag, children);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error deconstructing TLV", e);
            }
        }
    }

    public static TLV readRecursive(byte[] data) throws IOException {
        return readRecursive(data, 0, data.length);
    }

    public static TLV readRecursive(byte[] data, int offset, int length) throws IOException {
        TLVReader reader = new TLVReader(data, offset, length);
        TLV result = reader.readRecursive();
        if(reader.hasMoreData()) {
            throw new IllegalArgumentException("More than one tag where only one was expected");
        }
        return result;
    }

    public static List<TLV> readRecursives(byte[] data) throws IOException {
        return readRecursives(data, 0, data.length);
    }

    public static List<TLV> readRecursives(byte[] data, int offset, int length) throws IOException {
        ArrayList<TLV> res = new ArrayList<>();
        TLVReader reader = new TLVReader(data, offset, length);
        while(reader.hasMoreData()) {
            res.add(reader.readRecursive());
        }
        return res;
    }

}
