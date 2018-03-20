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

package org.openjavacard.util;

import java.util.Arrays;

/**
 * Utilities related to java arrays
 */
public class ArrayUtil {

    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] s = new byte[a.length + b.length];
        System.arraycopy(a, 0, s, 0, a.length);
        System.arraycopy(b, 0, s, a.length, b.length);
        return s;
    }

    public static boolean startsWith(byte[] that, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (that[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[][] splitBlocks(byte[] data, int blockSize) {
        int numBytes = data.length;
        int numBlocks = numBytes / blockSize;
        if(numBytes % blockSize > 0) {
            numBlocks++;
        }
        byte[][] res = new byte [numBlocks][];
        int offset = 0;
        for(int i = 0; i < numBlocks; i++) {
            int size = blockSize;
            if(offset + size > numBytes) {
                size = numBytes - offset;
            }
            res[i] = Arrays.copyOfRange(data, offset, offset + size);
            offset += size;
        }
        return res;
    }
}
