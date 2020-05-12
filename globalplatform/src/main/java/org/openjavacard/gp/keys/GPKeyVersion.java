package org.openjavacard.gp.keys;

public class GPKeyVersion {

    public static final int ANY = -1;

    public static final void checkKeyVersion(int keyVersion) {
        if(keyVersion >= 0 && keyVersion <= 255) {
            return;
        }
        throw new IllegalArgumentException("Invalid key version " + keyVersion);
    }

    public static final void checkKeyVersionSpecifier(int keyVersionSpecifier) {
        if(keyVersionSpecifier == ANY) {
            return;
        }
        if(keyVersionSpecifier >= 0 && keyVersionSpecifier <= 255) {
            return;
        }
        throw new IllegalArgumentException("Invalid key version specifier " + keyVersionSpecifier);
    }

}
