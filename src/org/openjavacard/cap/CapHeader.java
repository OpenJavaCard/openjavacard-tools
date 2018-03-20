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

package org.openjavacard.cap;

import org.openjavacard.iso.AID;
import org.openjavacard.util.HexUtil;

public class CapHeader {

    private static final byte[] MAGIC_HEADER = HexUtil.hexToBytes("DECAFFED");

    private static final int FLAG_ACC_INT = 0x01;
    private static final int FLAG_ACC_EXPORT = 0x02;
    private static final int FLAG_ACC_APPLET = 0x04;

    int mMajorVersion;
    int mMinorVersion;

    int mFlags;

    int mPackageMajorVersion;
    int mPackageMinorVersion;

    AID mPackageAID;

    String mPackageName;

    boolean isIntRequired() {
        return (mFlags & FLAG_ACC_INT) != 0;
    }

    boolean isExportPresent() {
        return (mFlags & FLAG_ACC_EXPORT) != 0;
    }

    boolean isAppletPresent() {
        return (mFlags & FLAG_ACC_APPLET) != 0;
    }

}
