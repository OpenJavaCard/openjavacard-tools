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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TLVReader {

    public static TLV readSinglePrimitive(byte[] data) throws IOException {
        return readSingle(data, 0, data.length);
    }

    public static TLV readSinglePrimitive(byte[] data, int offset, int length) throws IOException {
        InputStream stream = new ByteArrayInputStream(data, offset, length);
        TLVReader reader = new TLVReader(stream);
        TLVPrimitive result = reader.readTLVPrimitive();
        if(reader.hasMoreData()) {
            throw new IllegalArgumentException("More than one tag where only one was expected");
        }
        return result;
    }

    public static TLV readSingle(byte[] data) throws IOException {
        return readSingle(data, 0, data.length);
    }


    public static TLV readSingle(byte[] data, int offset, int length) throws IOException {
        InputStream stream = new ByteArrayInputStream(data, offset, length);
        TLVReader reader = new TLVReader(stream);
        TLV result = reader.readTLV();
        if(reader.hasMoreData()) {
            throw new IllegalArgumentException("More than one tag where only one was expected");
        }
        return result;
    }

    public static List<TLV> readMultiplePrimitive(byte[] data) throws IOException {
        return readMultiplePrimitive(data, 0, data.length);
    }

    public static List<TLV> readMultiplePrimitive(byte[] data, int offset, int length) throws IOException {
        InputStream stream = new ByteArrayInputStream(data);
        TLVReader reader = new TLVReader(stream);
        return reader.readTLVs(false);
    }

    public static List<TLV> readMultiple(byte[] data) throws IOException {
        return readMultiple(data, 0, data.length);
    }

    public static List<TLV> readMultiple(byte[] data, int offset, int length) throws IOException {
        InputStream stream = new ByteArrayInputStream(data);
        TLVReader reader = new TLVReader(stream);
        return reader.readTLVs(true);
    }

    private InputStream mStream;

    TLVReader(InputStream stream) {
        mStream = stream;
    }

    TLVReader(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    TLVReader(byte[] bytes, int offset, int length) {
        this(new ByteArrayInputStream(bytes, offset, length));
    }

    boolean hasMoreData() throws IOException {
        return mStream.available() > 0;
    }

    TLV readTLV() throws IOException {
        return readTLV(true);
    }

    TLVPrimitive readTLVPrimitive() throws IOException {
        TLV res = readTLV(false);
        if(res instanceof TLVPrimitive) {
            return (TLVPrimitive)res;
        } else {
            throw new InternalError();
        }
    }

    private TLV readTLV(boolean allowConstructed) throws IOException {
        int tag = readTag();
        int length = readLength();
        byte[] value = readBytes(length);
        if(allowConstructed && TLVTag.isConstructed(tag)) {
            List<TLV> children = readMultiple(value);
            return new TLVConstructed(tag, children);
        } else {
            return new TLVPrimitive(tag, value);
        }
    }

    private List<TLV> readTLVs(boolean allowConstructed) throws IOException {
        ArrayList<TLV> result = new ArrayList<>();
        while(hasMoreData()) {
            TLV tlv = readTLV(allowConstructed);
            result.add(tlv);
        }
        return result;
    }


    void skipTLV() throws Exception {
        int tag = readTag();
        int length = readLength();
        skipBytes(length);
    }

    void skipTLV(int tagExpected) throws Exception {
        int tag = readTag();
        int length = readLength();
        skipBytes(length);
        checkTag(tagExpected, tag);
    }

    private void checkTag(int tagExpected, int tagRead) throws IOException {
        if (tagExpected != tagRead) {
            throw new IOException(
                    "TLV parse error: expected tag " + tagExpected
                            + " but got " + tagRead);
        }
    }

    private int readByte() throws IOException {
        int data = mStream.read();
        if (data == -1) {
            throw new IOException("TLV parse error: short read");
        }
        return data;
    }

    private byte[] readBytes(int length) throws IOException {
        byte[] data = new byte[length];
        readBytesInto(data);
        return data;
    }

    private void readBytesInto(byte[] buffer) throws IOException {
        readBytesInto(buffer, 0, buffer.length);
    }

    private void readBytesInto(byte[] buffer, int offset, int length) throws IOException {
        int read = mStream.read(buffer, offset, length);
        if (read == -1 || read != length) {
            throw new IOException("TLV parse error: short read");
        }
    }

    private void skipBytes(int length) throws IOException {
        mStream.skip(length);
    }

    private int readTag() throws IOException {
        int first = readByte();
        int second = 0;
        if (TLVTag.isLongForm(first)) {
            second = readByte();
            if(!TLVTag.isLastByte(second)) {
                throw new IOException("TLV tag to long");
            }
        }
        return ((first & 0xFF) << 8) | (second & 0xFF);
    }

    private int readLength() throws IOException {
        int first = readByte();
        int length = first;
        if (TLVLength.isLongForm(first)) {
            int size = TLVLength.longLength(first);
            if (size > 3) {
                throw new IOException("TLV parse error: length too large");
            }
            length = 0;
            for (int i = 0; i < size; i++) {
                int current = readByte();
                length = (length << 8) | (current & 0xFF);
            }
        }
        return length;
    }

}
