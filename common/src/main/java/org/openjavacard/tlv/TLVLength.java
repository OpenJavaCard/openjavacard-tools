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

import org.openjavacard.util.BinUtil;
import org.openjavacard.util.HexUtil;

public class TLVLength {

    private static final byte LENGTH_LONG_MASK = (byte)0x80;
    private static final byte LENGTH_LONG_FLAG = (byte)0x80;
    private static final byte LENGTH_SIZE_MASK = (byte)0x7F;

    public static final boolean isLongForm(int firstByte) {
        return (firstByte & LENGTH_LONG_MASK) == LENGTH_LONG_FLAG;
    }

    public static final int longLength(int firstByte) {
        return (firstByte & ~LENGTH_SIZE_MASK);
    }

    public static final int lengthSize(int length) {
        if(length > 32767) {
            throw new IllegalArgumentException("Length " + length + " is to large");
        } if(length > 127) {
            return 3;
        } else {
            return 1;
        }
    }

    public static final byte[] lengthBytes(int length) {
        if(length > 32767) {
            throw new IllegalArgumentException("Length " + length + " is to large");
        } if(length > 127) {
            byte[] res = new byte[3];
            res[0] = 2 | TLVLength.LENGTH_LONG_FLAG;
            BinUtil.setShort(res, 1, (short)length);
            return res;
        } else {
            return new byte[] { (byte)length };
        }
    }

    public static final String toString(int length) {
        if(length > 32767) {
            throw new IllegalArgumentException("Length " + length + " is to large");
        } if(length > 255) {
            return HexUtil.hex16(length);
        } else {
            return HexUtil.hex8(length);
        }
    }

}
