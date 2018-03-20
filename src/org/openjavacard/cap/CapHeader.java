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
