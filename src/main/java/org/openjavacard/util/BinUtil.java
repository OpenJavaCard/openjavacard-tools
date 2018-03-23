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

package org.openjavacard.util;

/** Utilities related to treating integers as binary words */
public class BinUtil {

    public static final byte makeByte(int v) {
        if(v > 255) {
            throw new IllegalArgumentException();
        }
        return (byte)(v & 0xFF);
    }

    public static final byte getByte(byte buf[], int off)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        return (byte)(buf[off] & 0xFF);
    }

    public static final short makeShort(int v) {
        if(v > 65535) {
            throw new IllegalArgumentException();
        }
        return (short)(v & 0xFFFF);
    }

    public static final short makeShort(byte b1, byte b2) {
        return (short) (((short) b1 << 8) + ((short) b2 & 0xff));
    }

    public static final byte getShortLowByte(short s) {
        return (byte)((s >> 0) & 0xFF);
    }

    public static final byte getShortHighByte(short s) {
        return (byte)((s >> 8) & 0xFF);
    }

    public static final short getShort(byte buf[], int off)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        return (short) (((short) buf[off] << 8) + ((short) buf[off + 1] & 0xff));
    }

    public static final short setShort(byte buf[], int off, short sValue)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        buf[off] = (byte) (sValue >> 8);
        buf[off + 1] = (byte) sValue;
        return (short) (off + 2);
    }

}
