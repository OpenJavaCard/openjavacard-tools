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

import org.openjavacard.util.BinUtil;
import org.openjavacard.util.HexUtil;

public class TLVTag {

    public static final int TAG_EOC = 0x00;
    public static final int TAG_BOOLEAN = 0x01;
    public static final int TAG_INTEGER = 0x02;
    public static final int TAG_BITSTRING = 0x03;
    public static final int TAG_OCTETSTRING = 0x04;
    public static final int TAG_NULL = 0x05;
    public static final int TAG_OID = 0x06;
    public static final int TAG_OBJECTDESCRIPTOR = 0x07;
    public static final int TAG_EXTERNAL = 0x08;
    public static final int TAG_REAL = 0x09;
    public static final int TAG_ENUMERATED = 0x0A;
    public static final int TAG_EMBEDDED_PDV = 0x0B;
    public static final int TAG_UTF8STRING = 0x0C;
    public static final int TAG_RELATIVE_OID = 0x0D;
    public static final int TAG_SEQUENCE = 0x10;
    public static final int TAG_SET = 0x11;
    public static final int TAG_NUMERICSTRING = 0x12;
    public static final int TAG_PRINTABLESTRING = 0x13;
    public static final int TAG_T61STRING = 0x14;
    public static final int TAG_VIDEOTEXSTRING = 0x15;
    public static final int TAG_IA5STRING = 0x16;
    public static final int TAG_UTCTIME = 0x17;
    public static final int TAG_GENERALIZEDTIME = 0x18;
    public static final int TAG_GRAPHICSTRING = 0x19;
    public static final int TAG_VISIBLESTRING = 0x1A;
    public static final int TAG_GENERALSTRING = 0x1B;
    public static final int TAG_UNIVERSALSTRING = 0x1C;
    public static final int TAG_CHARACTERSTRING = 0x1D;
    public static final int TAG_BMPSTRING = 0x1E;
    public static final int TAG_LONG = 0x1F;

    public static final int TAG_TYPE_MASK = 0x1F;

    public static final int TAG_CONTINUES_MASK = 0x80;
    public static final int TAG_CONTINUES = 0x80;

    public static final int TAG_CLASS_MASK = 0xC0;
    public static final int TAG_CLASS_UNIVERSAL = 0x00;
    public static final int TAG_CLASS_APPLICATION = 0x40;
    public static final int TAG_CLASS_CONTEXT = 0x80;
    public static final int TAG_CLASS_PRIVATE = 0xC0;

    public static final int TAG_PC = 0x20;

    public static final boolean isLongForm(int firstByte) {
        return (firstByte & TAG_TYPE_MASK) == TAG_LONG;
    }

    public static final boolean isLastByte(int tagByte) {
        return (tagByte & TAG_CONTINUES_MASK) != TAG_CONTINUES;
    }

    public static final int getClassValue(int tag) {
        return (tag & TAG_CLASS_MASK);
    }

    public static final boolean isUniversal(int tag) {
        return getClassValue(tag) == TAG_CLASS_UNIVERSAL;
    }

    public static final boolean isApplication(int tag) {
        return getClassValue(tag) == TAG_CLASS_APPLICATION;
    }

    public static final boolean isContext(int tag) {
        return getClassValue(tag) == TAG_CLASS_CONTEXT;
    }

    public static final boolean isPrivate(int tag) {
        return getClassValue(tag) == TAG_CLASS_PRIVATE;
    }

    public static final boolean isPrimitive(int tag) {
        return (tag & TAG_PC) == 0;
    }

    public static final boolean isConstructed(int tag) {
        return (tag & TAG_PC) != 0;
    }

    public static final int tagSize(int tag) {
        if(isLongForm(tag & 0xFF)) {
            if(!isLastByte((tag >> 8) & 0xFF)) {
                throw new IllegalArgumentException("Tag is longer than two bytes");
            }
            return 2;
        } else {
            return 1;
        }
    }

    public static final byte[] tagBytes(int tag) {
        if(isLongForm(tag & 0xFF)) {
            if(!isLastByte((tag >> 8) & 0xFF)) {
                throw new IllegalArgumentException("Tag is longer than two bytes");
            }
            byte[] res = new byte[2];
            res[0] = (byte)(tag & 0xFF);
            res[1] = (byte)((tag >> 8) & 0xFF);
            return res;
        } else {
            return new byte[] { (byte)(tag & 0xFF) };
        }
    }

}
