package org.openjavacard.gp.scp;

import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyType;
import org.openjavacard.util.HexUtil;

import java.util.Hashtable;

/**
 * SCP02 session key derivation
 */
public class SCP02Derivation {

    /** All key types in SCP02 */
    private static final GPKeyType[] KEY_TYPES = {
            GPKeyType.ENC, GPKeyType.MAC, GPKeyType.KEK, GPKeyType.RMAC
    };

    /** Derivation constants */
    private static final Hashtable<GPKeyType, byte[]> CONSTANTS = new Hashtable<>();
    private static final byte[] SCP02_DERIVE_MAC  = {0x01, 0x01};
    private static final byte[] SCP02_DERIVE_RMAC = {0x01, 0x02};
    private static final byte[] SCP02_DERIVE_KEK = {0x01, (byte) 0x81};
    private static final byte[] SCP02_DERIVE_ENC = {0x01, (byte) 0x82};
    static {
        CONSTANTS.put(GPKeyType.MAC,  SCP02_DERIVE_MAC);
        CONSTANTS.put(GPKeyType.RMAC, SCP02_DERIVE_RMAC);
        CONSTANTS.put(GPKeyType.KEK,  SCP02_DERIVE_KEK);
        CONSTANTS.put(GPKeyType.ENC,  SCP02_DERIVE_ENC);
    }

    /**
     * Derive SCP02 session keys
     *
     * @param staticKeys to derive from
     * @param sequence for derivation
     * @return keyset containing session keys
     */
    public static GPKeySet deriveSessionKeys(GPKeySet staticKeys, byte[] sequence) {
        // synthesize a name for the new keyset
        String name = staticKeys.getName() + "-SCP02:" + HexUtil.bytesToHex(sequence);
        // create the new set
        GPKeySet derivedSet = new GPKeySet(name, staticKeys.getKeyVersion(), staticKeys.getDiversification());
        // initialize buffer for derivation
        byte[] buffer = new byte[16];
        System.arraycopy(sequence, 0, buffer, 2, 2);
        // go through all keys
        for (GPKeyType type : KEY_TYPES) {
            // get the static base key
            GPKey staticKey = staticKeys.getKeyByType(type);
            if(staticKey != null) {
                // insert derivation data
                byte[] derivation = CONSTANTS.get(type);
                System.arraycopy(derivation, 0, buffer, 0, 2);
                // derive using DES
                byte[] derived = GPCrypto.enc_3des_cbc_nulliv(staticKey, buffer);
                // construct the new key
                GPKey sessionKey = new GPKey(type, staticKey.getId(),
                        GPKeyCipher.DES3, derived);
                // insert key into new set
                derivedSet.putKey(sessionKey);
            }
        }
        // return the new set
        return derivedSet;
    }

}
