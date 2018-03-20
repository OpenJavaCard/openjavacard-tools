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
