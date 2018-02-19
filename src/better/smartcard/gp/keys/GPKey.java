package better.smartcard.gp.keys;

import better.smartcard.util.HexUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class GPKey {

    byte mId;

    GPKeyType mType;

    GPKeyCipher mCipher;

    byte[] mSecret;

    /**
     * Constructs a key object for the provided data
     *
     * @param type   of the key object
     * @param cipher of the key object
     * @param secret the key itself
     */
    public GPKey(GPKeyType type, byte id, GPKeyCipher cipher, byte[] secret) {
        mType = type;
        mId = id;
        mCipher = cipher;
        mSecret = secret.clone();
        checkKeyLength();
    }

    /**
     * @return key id of this key
     */
    public byte getId() {
        return mId;
    }

    /**
     * @return type of this key
     */
    public GPKeyType getType() {
        return mType;
    }

    /**
     * @return cipher of this key
     */
    public GPKeyCipher getCipher() {
        return mCipher;
    }

    /**
     * @return key bytes of this key
     */
    public byte[] getSecret() {
        return mSecret.clone();
    }

    /**
     * @return length of secret in bytes
     */
    public int getLength() {
        return mSecret.length;
    }

    /**
     * Return a SecretKey object for this key
     *
     * @return new SecretKey corresponding to this key
     */
    public SecretKey getSecretKey() {
        return getSecretKey(mCipher);
    }

    /**
     * Return a SecretKey for a specific cipher
     * <p/>
     * Will coerce the key if required.
     *
     * @param cipher for the new key
     * @return new SecretKey corresponding to this key
     */
    public SecretKey getSecretKey(GPKeyCipher cipher) {
        switch (cipher) {
            case DES:
                return new SecretKeySpec(enlarge(mSecret, 8), "DES");
            case DES3:
                return new SecretKeySpec(enlarge(mSecret, 24), "DESede");
            case AES:
                return new SecretKeySpec(mSecret, "AES");
            default:
                throw new IllegalArgumentException("Do not know how to handle cipher " + cipher);
        }
    }

    private byte[] enlarge(byte[] key, int length) {
        int secretLen = key.length;
        if(length == secretLen) {
            return key;
        }
        if(length == 8) {
            byte[] key8 = new byte[8];
            switch(secretLen) {
                case 8:
                case 16:
                case 24:
                    System.arraycopy(key, 0, key8, 0, 8);
                    return key8;
            }
        }
        if(length == 24) {
            byte[] key24 = new byte[24];
            switch(secretLen) {
                case 8:
                    System.arraycopy(key, 0, key24,  0, 8);
                    System.arraycopy(key, 0, key24,  8, 8);
                    System.arraycopy(key, 0, key24, 16, 8);
                    return key24;
                case 16:
                    System.arraycopy(key, 0, key24,  0, 16);
                    System.arraycopy(key, 0, key24, 16,  8);
                    return key24;
                case 24:
                    System.arraycopy(key, 0, key24, 0, 24);
                    return key24;
            }
        }
        throw new Error("Do not know how to coerce DES key from length " + secretLen + " to length " + length);
        /*
        if (length == 24) {
            byte[] key24 = new byte[24];
            System.arraycopy(key, 0, key24, 0, 16);
            System.arraycopy(key, 0, key24, 16, 8);
            return key24;
        } else {
            byte[] key8 = new byte[8];
            System.arraycopy(key, 0, key8, 0, 8);
            return key8;
        }
        */
    }

    /**
     * Internal: check that key length is appropriate
     */
    private void checkKeyLength() {
        int keyLength = -1;
        switch (mCipher) {
            case DES:
                keyLength = 8;
                break;
            case DES3:
            case AES:
                keyLength = 16;
                break;
        }
        if (mSecret.length != keyLength) {
            throw new IllegalArgumentException(
                    "Wrong key length " + mSecret.length
                            + " for " + mCipher + ", need " + keyLength);
        }
    }

    public String toString() {
        String idString = "";
        if(mId != 0) {
            idString = " id " + mId;
        }
        return "key " + mType + idString + " cipher " + mCipher
                + " secret " + HexUtil.bytesToHex(mSecret);
    }

}
