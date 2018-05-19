package org.openjavacard.cap.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CapVersion {

    public static final CapVersion CAP21 = new CapVersion(2, 1);
    public static final CapVersion CAP22 = new CapVersion(2, 1);

    public final int major;
    public final int minor;

    public CapVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    @JsonIgnore
    public boolean isSupported() {
        if(this.major == 2) {
            if(this.minor == 1) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return major + "." + minor;
    }

}
