/*
 * IsoApplet: A Java Card PKI applet aimiing for ISO 7816 compliance.
 * Copyright (C) 2014  Philip Wendland (wendlandphilip@gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.openjavacard.tlv;

import org.openjavacard.util.HexUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TLVUtil {

    //private static final Logger LOG = LoggerFactory.getLogger(TLVUtil.class);

    public static List<TLV> parseTags(byte[] tlv) {
        return parseTags(tlv, 0, tlv.length);
    }

    public static List<TLV> parseTags(byte[] tlv, int offset, int length) {
        List<TLV> result = new ArrayList<>();
        int end = offset + length;
        int off = offset;
        int len = length;

        while (off < end) {
            // read tag
            int tlvTag = readTag(tlv, off, len);
            int tagSize = sizeTag(tlvTag);
            off += tagSize;
            len -= tagSize;
            // read length
            int tlvLen = readLength(tlv, off, len);
            if (tlvLen < 0) {
                throw new Error("Inconsistent TLV: bad length");
            }
            int lenSize = sizeLength(tlvLen);
            off += lenSize;
            len -= lenSize;
            if (len < tlvLen) {
                throw new Error("Inconsistent TLV: input too short");
            }
            // read data
            byte[] data = Arrays.copyOfRange(tlv, off, off + tlvLen);
            off += tlvLen;
            len -= tlvLen;
            // create TLV object
            result.add(new TLV(tlvTag, data));
        }

        if (off != end) {
            throw new Error("Inconsistent TLV: end mismatch");
        }

        if (len != 0) {
            throw new Error("Inconsistent TLV: final length not zero");
        }

        return result;
    }

    public static TLV parseTag(int tag, byte[] tlv) {
        return parseTag(tag, tlv, 0, tlv.length);
    }

    public static TLV parseTag(int tag, byte[] tlv, int offset, int length) {
        TLV result = parseTag(tlv, offset, length);
        int found = result.getTag();
        if (found != tag) {
            throw new Error("Bad TLV: expected " + stringTag(tag) + " got " + stringTag(found));
        }
        return result;
    }

    public static TLV parseTag(byte[] tlv) {
        return parseTag(tlv, 0, tlv.length);
    }

    public static TLV parseTag(byte[] tlv, int offset, int length) {
        int off = offset;
        int len = length;

        // read tag
        int tlvTag = readTag(tlv, off, len);
        int tagSize = sizeTag(tlvTag);
        off += tagSize;
        len -= tagSize;

        // read length
        int tlvLen = readLength(tlv, off, len);
        if (tlvLen < 0) {
            throw new Error("Inconsistent TLV: bad length");
        }
        int lenSize = sizeLength(tlvLen);
        off += lenSize;
        len -= lenSize;
        if (len < tlvLen) {
            throw new Error("Inconsistent TLV: bad length");
        }

        if (len > tlvLen) {
            throw new Error("Inconsistent TLV: input too long");
        }
        if (len < tlvLen) {
            throw new Error("Inconsistent TLV: input too short");
        }

        // read data
        byte[] value = Arrays.copyOfRange(tlv, off, off + tlvLen);

        // construct result
        return new TLV(tlvTag, value);
    }

    public static int findTag(byte[] tlv, int offset, int length, int tag) {
        int end = offset + length;
        int off = offset;
        int len = length;

        while (off < end) {
            int tagOff = off;
            // read tag
            int tlvTag = readTag(tlv, off, len);
            int tagSize = sizeTag(tlvTag);
            off += tagSize;
            len -= tagSize;
            // read length
            int tlvLen = readLength(tlv, off, len);
            if (len < 0) {
                throw new Error("Inconsistent TLV");
            }
            int lenSize = sizeLength(tlvLen);
            off += lenSize;
            len -= lenSize;
            if (len < tlvLen) {
                throw new Error("Inconsistent TLV");
            }
            // check tag
            if (tlvTag == tag) {
                return tagOff;
            }
            // skip data
            off += tlvLen;
            len -= tlvLen;
        }

        if (off != end) {
            throw new Error("Inconsistent TLV");
        }

        return -1;
    }

    public static boolean checkTags(byte[] tlv, int offset, int length) {
        int count = countTags(tlv, offset, length);
        return count >= 0;
    }

    public static int countTags(byte[] tlv, int offset, int length) {
        int end = offset + length;
        int off = offset;
        int len = length;

        int tags = 0;

        int tlvTag;
        int tlvLen;

        while (off < (offset + length)) {
            // skip tag
            tlvTag = readTag(tlv, off, len);
            if (tlvTag < 0) {
                return -1;
            }
            int tagSize = sizeTag(tlvTag);
            off += tagSize;
            len -= tagSize;
            // skip length
            tlvLen = readLength(tlv, off, len);
            if (tlvLen < 0) {
                return -1;
            }
            int lenSize = sizeLength(tlvLen);
            off += lenSize;
            len -= lenSize;
            if (len < tlvLen) {
                return -1;
            }
            // skip data
            off += tlvLen;
            len -= tlvLen;
            // count one tag
            tags++;
        }

        if (off != end) {
            return -1;
        }

        if (len != 0) {
            return -1;
        }

        return tags;
    }

    public static int readTag(byte[] buf, int offset, int length) {
        if (length < 1) {
            throw new Error("TLV short read");
        }
        int tag = buf[offset++] & 0xFF;
        if ((tag & 0x1F) == 0x1F) {
            if (length < 2) {
                throw new Error("TLV short read");
            }
            tag = (tag << 8) | (buf[offset++] & 0xFF);
        }
        return tag;
    }

    public static int putTag(byte[] buf, int offset, int value) {
        if (value > 255) {
            buf[offset + 0] = (byte) ((value >> 8) & 0xFF);
            buf[offset + 1] = (byte) (value & 0xFF);
            return 2;
        } else {
            buf[offset] = (byte) (value & 0xFF);
            return 1;
        }
    }

    public static String stringTag(int tag) {
        if (tag > 255) {
            return HexUtil.hex16(tag);
        } else {
            return HexUtil.hex8(tag);
        }
    }

    public static int sizeTag(int tag) {
        if (tag > 255) {
            return 2;
        } else {
            return 1;
        }
    }

    public static byte[] convertTag(int tag) {
        int resSize = sizeTag(tag);
        byte[] res = new byte[resSize];
        putTag(res, 0, tag);
        return res;
    }

    public static int readLength(byte[] buf, int offset, int length) {
        if (length < 1) {
            throw new Error("TLV short read");
        }
        byte first = buf[offset];
        if (first == 0x82) {
            if (length < 3) {
                throw new Error("TLV short read");
            }
            return getShort(buf, offset + 1);
        } else if (first == 0x81) {
            if (length < 2) {
                throw new Error("TLV short read");
            }
            return 0xFF & buf[offset + 1];
        } else if (first >= 0) { // 00..7F
            return 0x7F & first;
        } else {
            return -1;
        }
    }

    public static int putLength(byte[] buf, int offset, int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Invalid TLV length " + value);
        } else if (value < 128) {
            buf[offset] = (byte) (value & 0x7F);
            return 1;
        } else if (value < 256) {
            buf[offset + 0] = (byte) 0x81;
            buf[offset + 1] = (byte) (value & 0xFF);
            return 2;
        } else if (value < Short.MAX_VALUE) {
            buf[offset + 0] = (byte) 0x82;
            putShort(buf, offset + 1, (short) (value & 0x7FFF));
            return 3;
        } else {
            throw new IllegalArgumentException("Invalid TLV length " + value);
        }
    }

    public static int sizeLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Invalid TLV length " + length);
        } else if (length < 128) {
            return 1;
        } else if (length < 256) {
            return 2;
        } else if (length < Short.MAX_VALUE) {
            return 3;
        } else {
            throw new IllegalArgumentException("Invalid TLV length " + length);
        }
    }

    public static byte[] convertLength(int length) {
        int resSize = sizeLength(length);
        byte[] res = new byte[resSize];
        putLength(res, 0, length);
        return res;
    }

    public static int getShort(byte[] buf, int offset) {
        byte h = buf[offset + 1];
        byte l = buf[offset + 2];
        return (h << 8) | l;
    }

    public static int putShort(byte[] buf, int offset, int value) {
        buf[offset + 0] = (byte) ((value >> 8) & 0xFF);
        buf[offset + 1] = (byte) ((value >> 0) & 0xFF);
        return 2;
    }

}
