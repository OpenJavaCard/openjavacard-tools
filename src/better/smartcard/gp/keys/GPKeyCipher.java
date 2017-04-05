package better.smartcard.gp.keys;

import better.smartcard.gp.protocol.GP;

public enum GPKeyCipher {
    DES, DES3, AES;

    public static GPKeyCipher getCipherForKeyType(byte keyType) {
        switch (keyType) {
            case GP.KEY_TYPE_DES:
            case GP.KEY_TYPE_DES_CBC:
            case GP.KEY_TYPE_DES_ECB:
                return GPKeyCipher.DES;
            case GP.KEY_TYPE_3DES_CBC:
                return GPKeyCipher.DES3;
            case GP.KEY_TYPE_AES:
                return GPKeyCipher.AES;
            default:
                throw new IllegalArgumentException("Unsupported key type " + keyType);
        }
    }

}
