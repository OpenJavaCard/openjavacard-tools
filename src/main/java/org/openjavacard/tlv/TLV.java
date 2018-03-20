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

import org.openjavacard.util.HexUtil;

public class TLV {

    public static final byte[] encode(int tag, byte[] data) {
        return new TLV(tag, data).getEncoded();
    }

    int mTag;

    byte[] mData;

    public TLV(int tag, byte[] data) {
        mTag = tag;
        mData = data;
    }

    public int getTag() {
        return mTag;
    }

    public int getLength() {
        return mData.length;
    }

    public byte[] getData() {
        return mData.clone();
    }

    public void setData(byte[] data) {
        mData = data.clone();
    }

    public int getEncodedLength() {
        return TLVUtil.sizeTag(mTag)
                + TLVUtil.sizeLength(mData.length)
                + mData.length;
    }

    public byte[] getEncoded() {
        int length = getEncodedLength();
        byte[] encoded = new byte[length];
        int off = 0;
        int tagSize = TLVUtil.putTag(encoded, off, mTag);
        off += tagSize;
        int lenSize = TLVUtil.putLength(encoded, off, mData.length);
        off += lenSize;
        System.arraycopy(mData, 0, encoded, off, mData.length);
        return encoded;
    }

    public String toString() {
        return "TLV(T=" + Integer.toHexString(mTag) + ",L=" + mData.length + ",V=" + HexUtil.bytesToHex(mData) + ")";
    }

}
