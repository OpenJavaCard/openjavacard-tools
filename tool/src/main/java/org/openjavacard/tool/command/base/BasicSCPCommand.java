package org.openjavacard.tool.command.base;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeyDiversification;
import org.openjavacard.gp.keys.GPKeyId;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.gp.keys.GPKeyVersion;
import org.openjavacard.gp.scp.SCPSecurityPolicy;
import org.openjavacard.util.HexUtil;

public class BasicSCPCommand extends BasicCardCommand {

    @Parameter(
            names = "--scp-diversification", order = 300,
            description = "Use specified key diversification"
    )
    protected GPKeyDiversification scpDiversification = GPKeyDiversification.NONE;

    @Parameter(
            names = "--scp-protocol", order = 300,
            description = "Require specified SCP protocol"
    )
    protected String scpProtocol = "00";

    @Parameter(
            names = "--scp-parameters", order = 300,
            description = "Require specified SCP parameters"
    )
    protected String scpParameters = "00";

    @Parameter(
            names = "--scp-security", order = 300,
            description = "Require specified SCP security level"
    )
    protected SCPSecurityPolicy scpSecurity = SCPSecurityPolicy.CMAC;

    @Parameter(
            names = "--key-version",
            description = "User-specified keys: key version (0 means any)",
            validateWith = PositiveInteger.class
    )
    private int scpKeyVersion = 0;

    @Parameter(
            names = "--key-id",
            description = "User-specified keys: first key ID (0 means any)",
            validateWith = PositiveInteger.class
    )
    private int scpKeyId = 0;

    @Parameter(
            names = "--key-cipher",
            description = "User-specified keys: key cipher"
    )
    private GPKeyCipher scpKeyCipher = GPKeyCipher.GENERIC;

    @Parameter(
            names = "--key-types",
            description = "User-specified keys: key types (colon-separated)"
    )
    private String scpKeyTypes = "MASTER";

    @Parameter(
            names = "--key-secrets",
            description = "User-specified keys: secrets (colon-separated)"
    )
    private String scpKeySecrets = null;


    public GPKeySet getKeySet() {
        GPKeySet keys = GPKeySet.GLOBALPLATFORM;

        if(scpKeySecrets != null) {
            keys = buildKeysFromParameters(scpKeyId, scpKeyVersion, scpKeyCipher, scpKeyTypes, scpKeySecrets);
        }

        return keys;
    }

    public static GPKeySet buildKeysFromParameters(int keyId, int keyVersion, GPKeyCipher cipher, String types, String secrets) {
        // XXX this is not comprehensive because of the loop and protocol variations
        GPKeyId.checkKeyId(keyId);
        GPKeyVersion.checkKeyVersion(keyVersion);
        // build a key set
        GPKeySet keys = new GPKeySet("commandline", keyVersion);
        // split arguments
        String[] typeStrings = types.split(":");
        String[] secretStrings = secrets.split(":");
        // check lengths of provided arrays
        if(typeStrings.length != secretStrings.length) {
            throw new Error("Must provide an equal number of key types and secrets");
        }
        // assume number of keys from number of types
        int numKeys = typeStrings.length;
        for(int i = 0; i < numKeys; i++) {
            GPKeyUsage usage = GPKeyUsage.valueOf(typeStrings[i]);
            byte[] secret = HexUtil.hexToBytes(secretStrings[i]);
            byte id = (byte)(keyId + i);
            if(usage == GPKeyUsage.MASTER) {
                id = 0;
            }
            GPKey key = new GPKey(id, usage, cipher, secret);
            keys.putKey(key);
        }
        return keys;
    }

}
