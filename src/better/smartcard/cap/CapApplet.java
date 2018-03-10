package better.smartcard.cap;

import better.smartcard.iso.AID;

public class CapApplet {

    private static final String ATTR_NAME = "Name";
    private static final String ATTR_VERSION = "Version";
    private static final String ATTR_AID = "AID";

    String mName;
    String mVersion;

    AID mAID;

    CapApplet() {
    }

    public String getName() {
        return mName;
    }

    public String getVersion() {
        return mVersion;
    }

    public AID getAID() {
        return mAID;
    }

    void readAttribute(String name, String value) {
        if (name.equals(ATTR_NAME)) {
            mName = value;
        }
        if (name.equals(ATTR_VERSION)) {
            mVersion = value;
        }
        if (name.equals(ATTR_AID)) {
            mAID = AID.fromArrayString(value);
        }
    }

}
