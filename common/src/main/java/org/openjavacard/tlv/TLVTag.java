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

import org.openjavacard.util.HexUtil;

public class TLVTag {

    public static final int TYPE_MASK = 0x1F7F;
    public static final int TYPE_MASK_FIRST = 0x1F00;

    public static final int TYPE_EOC = 0x0000;
    public static final int TYPE_BOOLEAN = 0x0100;
    public static final int TYPE_INTEGER = 0x0200;
    public static final int TYPE_BITSTRING = 0x0300;
    public static final int TYPE_OCTETSTRING = 0x0400;
    public static final int TYPE_NULL = 0x0500;
    public static final int TYPE_OID = 0x0600;
    public static final int TYPE_OBJECTDESCRIPTOR = 0x0700;
    public static final int TYPE_EXTERNAL = 0x0800;
    public static final int TYPE_REAL = 0x0900;
    public static final int TYPE_ENUMERATED = 0x0A00;
    public static final int TYPE_EMBEDDED_PDV = 0x0B00;
    public static final int TYPE_UTF8STRING = 0x0C00;
    public static final int TYPE_RELATIVE_OID = 0x0D00;
    public static final int TYPE_SEQUENCE = 0x1000;
    public static final int TYPE_SET = 0x1100;
    public static final int TYPE_NUMERICSTRING = 0x1200;
    public static final int TYPE_PRINTABLESTRING = 0x1300;
    public static final int TYPE_T61STRING = 0x1400;
    public static final int TYPE_VIDEOTEXSTRING = 0x1500;
    public static final int TYPE_IA5STRING = 0x1600;
    public static final int TYPE_UTCTIME = 0x1700;
    public static final int TYPE_GENERALIZEDTIME = 0x1800;
    public static final int TYPE_GRAPHICSTRING = 0x1900;
    public static final int TYPE_VISIBLESTRING = 0x1A00;
    public static final int TYPE_GENERALSTRING = 0x1B00;
    public static final int TYPE_UNIVERSALSTRING = 0x1C00;
    public static final int TYPE_CHARACTERSTRING = 0x1D00;
    public static final int TYPE_BMPSTRING = 0x1E00;
    public static final int TYPE_LONG = 0x1F00;

    public static final int CLASS_MASK        = 0xC000;
    public static final int CLASS_UNIVERSAL   = 0x0000;
    public static final int CLASS_APPLICATION = 0x4000;
    public static final int CLASS_CONTEXT     = 0x8000;
    public static final int CLASS_PRIVATE     = 0xC000;

    public static final int CONSTRUCTED_FLAG = 0x2000;

    private static final byte TAGBYTE_FIRST_TYPE_MASK = (byte)0x1F;
    private static final byte TAGBYTE_FIRST_TYPE_LONG = (byte)0x1F;
    private static final byte TAGBYTE_FLAG_CONTINUES = (byte)0x80;

    public static final boolean byteIsLongForm(int firstByte) {
        return (firstByte & TAGBYTE_FIRST_TYPE_MASK) == TAGBYTE_FIRST_TYPE_LONG;
    }

    public static final boolean byteIsLast(int tagByte) {
        return (tagByte & TAGBYTE_FLAG_CONTINUES) == 0;
    }


    public static final boolean isUniversal(int tag) {
        return tagClass(tag) == CLASS_UNIVERSAL;
    }

    public static final boolean isApplication(int tag) {
        return tagClass(tag) == CLASS_APPLICATION;
    }

    public static final boolean isContext(int tag) {
        return tagClass(tag) == CLASS_CONTEXT;
    }

    public static final boolean isPrivate(int tag) {
        return tagClass(tag) == CLASS_PRIVATE;
    }


    public static final boolean isPrimitive(int tag) {
        return (tag & CONSTRUCTED_FLAG) == 0;
    }

    public static final boolean isConstructed(int tag) {
        return (tag & CONSTRUCTED_FLAG) != 0;
    }


    public static final int tagClass(int tag) {
        return (tag & CLASS_MASK);
    }

    public static final int tagType(int tag) {
        return (tag & TYPE_MASK);
    }

    public static final boolean tagIsLong(int tag) {
        return (tag & TYPE_MASK_FIRST) == TYPE_LONG;
    }


    public static int tagFirstByte(int tag) {
        return (tag >> 8) & 0xFF;
    }

    public static int tagSecondByte(int tag) {
        return tag & 0xFF;
    }

    public static final int tagSize(int tag) {
        if(byteIsLongForm(tagFirstByte(tag))) {
            if(!byteIsLast(tagSecondByte(tag))) {
                throw new IllegalArgumentException("Tag is longer than two bytes");
            }
            return 2;
        } else {
            return 1;
        }
    }

    public static final byte[] tagBytes(int tag) {
        if(byteIsLongForm(tagFirstByte(tag))) {
            if(!byteIsLast(tagSecondByte(tag))) {
                throw new IllegalArgumentException("Tag is longer than two bytes");
            }
            byte[] res = new byte[2];
            res[0] = (byte)tagFirstByte(tag);
            res[1] = (byte)tagSecondByte(tag);
            return res;
        } else {
            return new byte[] { (byte)(tag & 0xFF) };
        }
    }

    public static final String toString(int tag) {
        if(tagIsLong(tag)) {
            return HexUtil.hex16(tag);
        } else {
            return HexUtil.hex8(tagFirstByte(tag));
        }
    }

}
