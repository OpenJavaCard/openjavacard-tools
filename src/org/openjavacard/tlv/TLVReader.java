package org.openjavacard.tlv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TLVReader {

    public static TLV decodeSingle(byte[] bytes) {
        TLVReader reader = new TLVReader(bytes);
        TLV tlv;
        try {
            tlv = reader.readTLV();
        } catch (IOException e) {
            throw new Error("Could not decode TLV", e);
        }
        return tlv;
    }

    public static TLV decodeSingle(byte[] bytes, int tagExpected) {
        TLVReader reader = new TLVReader(bytes);
        TLV tlv;
        try {
            tlv = reader.readTLV(tagExpected);
        } catch (IOException e) {
            throw new Error("Could not decode TLV", e);
        }
        return tlv;
    }

    InputStream mStream;

    TLVReader(InputStream stream) {
        mStream = stream;
    }

    TLVReader(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    TLVReader(byte[] bytes, int offset, int length) {
        this(new ByteArrayInputStream(bytes, offset, length));
    }

    protected int readByte() throws IOException {
        int data = mStream.read();
        if (data == -1) {
            throw new IOException("TLV parse error: short read");
        }
        return data;
    }

    protected byte[] readBytes(int length) throws IOException {
        byte[] data = new byte[length];
        readBytesInto(data);
        return data;
    }

    protected void readBytesInto(byte[] buffer) throws IOException {
        readBytesInto(buffer, 0, buffer.length);
    }

    protected void readBytesInto(byte[] buffer, int offset, int length) throws IOException {
        int read = mStream.read(buffer, offset, length);
        if (read == -1 || read != length) {
            throw new IOException("TLV parse error: short read");
        }
    }

    protected void skipByte() throws IOException {
        mStream.skip(1);
    }

    protected void skipBytes(int length) throws IOException {
        mStream.skip(length);
    }

    protected int readTag() throws IOException {
        int first = readByte();
        int tag = first;
        if (TLVTag.isLongForm(first)) {
            int current;
            do {
                if (tag > Short.MAX_VALUE) {
                    throw new IOException("TLV parse error: tag too long");
                }
                current = readByte();
                tag = (tag << 8) | (current & 0xFF);
            } while (!TLVTag.isLastByte(current));
        }
        return tag;
    }

    protected int readLength() throws IOException {
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

    TLV readTLV() throws IOException {
        int tag = readTag();
        int length = readLength();
        byte[] value = readBytes(length);
        return new TLV(tag, value);
    }

    TLV readTLV(int tagExpected) throws IOException {
        TLV tlv = readTLV();
        int tag = tlv.getTag();
        checkTag(tagExpected, tag);
        return tlv;
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
                    "TLV parse error: expected tag " + TLVTag.toString(tagExpected)
                            + " but got " + TLVTag.toString(tagRead));
        }
    }

}
