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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Stream-based parser for TLV structures
 */
public class TLVReader {

    /** Input stream that we are parsing from */
    private InputStream mStream;

    /** Construct reader for a stream */
    TLVReader(InputStream stream) {
        mStream = stream;
    }

    /** Construct reader for a byte array */
    TLVReader(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    /** Construct reader for a byte array */
    TLVReader(byte[] bytes, int offset, int length) {
        this(new ByteArrayInputStream(bytes, offset, length));
    }

    /** Return true if there is more data */
    boolean hasMoreData() throws TLVException {
        try {
            return mStream.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Read one TLV structure recursively
     * <p/>
     * The structure must have consistent CONSTRUCTED flags.
     * <p/>
     * @return a TLV object
     * @throws TLVException
     */
    TLV readRecursive() throws TLVException {
        return readTLV(true);
    }

    /**
     * Read one TLV primitive structure
     * <p/>
     * This avoid parsing the contents recursively.
     * <p/>
     * @return a TLVPrimitive
     * @throws TLVException
     */
    TLVPrimitive readPrimitive() throws TLVException {
        return readTLV(false).asPrimitive();
    }

    /**
     * Read one TLV constructed structure
     * <p/>
     * Will fail when the structures contents are not valid TLV.
     * <p/>
     * @return a TLVConstructed
     * @throws TLVException
     */
    TLVConstructed readConstructed() throws TLVException {
        return readTLV(false).asConstructed();
    }

    private TLV readTLV(boolean recurse) throws TLVException {
        int tag = readTag();
        int length = readLength();
        byte[] value = readBytes(length);
        if(recurse && TLVTag.isConstructed(tag)) {
            List<TLV> children = TLV.readRecursives(value);
            return new TLVConstructed(tag, children);
        } else {
            return new TLVPrimitive(tag, value);
        }
    }

    private int readByte() throws TLVException {
        try {
            int data = mStream.read();
            if (data == -1) {
                throw new TLVException("TLV parse error: short read");
            }
            return data;
        } catch (IOException e) {
            throw new TLVException(e);
        }
    }

    private byte[] readBytes(int length) throws TLVException {
        byte[] data = new byte[length];
        if(length > 0) {
            readBytesInto(data);
        }
        return data;
    }

    private void readBytesInto(byte[] buffer) throws TLVException {
        readBytesInto(buffer, 0, buffer.length);
    }

    private void readBytesInto(byte[] buffer, int offset, int length) throws TLVException {
        try {
            int read = mStream.read(buffer, offset, length);
            if (read == -1 || read != length) {
                throw new TLVException("TLV parse error: short read");
            }
        } catch (IOException e) {
            throw new TLVException(e);
        }
    }

    private int readTag() throws TLVException {
        int first = readByte();
        int second = 0;
        if (TLVTag.byteIsLongForm(first)) {
            second = readByte();
            if(!TLVTag.byteIsLast(second)) {
                throw new TLVException("TLV tag to long");
            }
        }
        return ((first & 0xFF) << 8) | (second & 0xFF);
    }

    private int readLength() throws TLVException {
        int first = readByte();
        int length = first;
        if (TLVLength.isLongForm(first)) {
            int size = TLVLength.longLength(first);
            if (size > 3) {
                throw new TLVException("TLV parse error: length too large");
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
