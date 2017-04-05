package better.smartcard.commands;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;
import better.smartcard.gp.keys.GPKey;
import better.smartcard.gp.keys.GPKeyCipher;
import better.smartcard.gp.keys.GPKeySet;
import better.smartcard.gp.keys.GPKeyType;
import better.smartcard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;
import java.io.PrintStream;

@Parameters(
        commandNames = "gp-keys",
        commandDescription = "Adds or replaces keys in an SD"
)
public class GPKeys extends GPCommand {

    @Parameter(
            names = "--new-version"
    )
    byte newKeyVersion = 1;

    @Parameter(
            names = "--new-id"
    )
    byte newKeyId = 1;

    @Parameter(
            names = "--new-cipher"
    )
    GPKeyCipher newKeyCipher = GPKeyCipher.DES3;

    @Parameter(
            names = "--new-types"
    )
    String newKeyTypes = "MASTER";

    @Parameter(
            names = "--new-secrets",
            required = true
    )
    String newKeySecrets;

    public GPKeys(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        GPKeySet newKeys = createNewKeys();

        os.println("New keys:\n" + newKeys);
    }

    private GPKeySet createNewKeys() {
        GPKeySet keys = new GPKeySet("command", newKeyVersion);
        String[] keyTypes = newKeyTypes.split(":");
        String[] keySecrets = newKeySecrets.split(":");
        if(keyTypes.length != keySecrets.length) {
            throw new Error("Must provide an equal number of key types and secrets");
        }
        int numKeys = keyTypes.length;
        for(int i = 0; i < numKeys; i++) {
            GPKeyType keyType = GPKeyType.valueOf(keyTypes[i]);
            byte[] keySecret = HexUtil.hexToBytes(keySecrets[i]);
            byte keyId = (byte)(newKeyId + i);
            if(keyType == GPKeyType.MASTER) {
                keyId = 0;
            }
            GPKey key = new GPKey(keyType, keyId, newKeyCipher, keySecret);
            keys.putKey(key);
        }
        return keys;
    }

}
