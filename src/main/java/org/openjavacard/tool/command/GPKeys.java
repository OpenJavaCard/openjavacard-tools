/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.tool.command;

import org.openjavacard.gp.GPCard;
import org.openjavacard.gp.GPContext;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyType;
import org.openjavacard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;

import javax.smartcardio.CardException;
import java.io.PrintStream;

@Parameters(
        commandNames = "gp-keys",
        commandDescription = "GlobalPlatform: set card security keys"
)
public class GPKeys extends GPCommand {

    @Parameter(
            names = "--new-version",
            validateWith = PositiveInteger.class
    )
    int newKeyVersion = 1;

    @Parameter(
            names = "--new-id",
            validateWith = PositiveInteger.class
    )
    int newKeyId = 1;

    @Parameter(
            names = "--new-cipher"
    )
    GPKeyCipher newKeyCipher = GPKeyCipher.DES3;

    @Parameter(
            names = "--new-types"
    )
    String newKeyTypes = "MAC:ENC:KEK";

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

        os.println("New " + newKeys);
        os.println();

        os.println("Checking key compatibility...");
        card.getIssuerDomain().checkKeys(newKeys);
        os.println("Check complete.");
        os.println();

    }

    private GPKeySet createNewKeys() {
        if(newKeyVersion > 255) {
            throw new Error("Bad key version");
        }
        // XXX this is not comprehensive because of the loop and protocol variations
        if(newKeyId > 255) {
            throw new Error("Bad key id");
        }
        GPKeySet keys = new GPKeySet("commandline", newKeyVersion);
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
