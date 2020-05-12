package org.openjavacard.gp.keys;

public class GPKeyId {

    public static final int ANY = -1;

    public static final void checkKeyId(int keyId) {
        if(keyId >= 0 && keyId <= 255) {
            return;
        }
        throw new IllegalArgumentException("Invalid key ID " + keyId);
    }

    public static final void checkKeyIdSpecifier(int keyIdSpecifier) {
        if(keyIdSpecifier == ANY) {
            return;
        }
        if(keyIdSpecifier >= 0 && keyIdSpecifier <= 255) {
            return;
        }
        throw new IllegalArgumentException("Invalid key ID specifier " + keyIdSpecifier);
    }

}
