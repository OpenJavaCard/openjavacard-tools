package org.openjavacard.cap;

import org.openjavacard.iso.AID;

public class CapImport {

    private static final String ATTR_VERSION = "Version";
    private static final String ATTR_AID = "AID";

    AID mAID;
    String mVersion;

    CapImport() {
    }

    public AID getAID() {
        return mAID;
    }

    public String getVersion() {
        return mVersion;
    }

    void readAttribute(String name, String value) {
        if (name.equals(ATTR_VERSION)) {
            mVersion = value;
        }
        if (name.equals(ATTR_AID)) {
            mAID = AID.fromArrayString(value);
        }
    }

    @Override
    public String toString() {
        return "CapImport " + mAID;
    }
}
