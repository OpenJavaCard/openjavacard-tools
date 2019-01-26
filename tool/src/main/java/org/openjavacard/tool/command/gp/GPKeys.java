/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2019 Ingo Albrecht <copyright@promovicz.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.openjavacard.tool.command.gp;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeySet;

import javax.smartcardio.CardException;
import java.io.PrintStream;

@Parameters(
        commandNames = "gp-keys",
        commandDescription = "GlobalPlatform: show or change security keys"
)
public class GPKeys extends GPCommand {

    @Parameter(
            names = "--new-version",
            validateWith = PositiveInteger.class
    )
    private int newKeyVersion = 1;

    @Parameter(
            names = "--new-id",
            validateWith = PositiveInteger.class
    )
    private int newKeyId = 1;

    @Parameter(
            names = "--new-cipher"
    )
    private GPKeyCipher newKeyCipher = GPKeyCipher.GENERIC;

    @Parameter(
            names = "--new-types"
    )
    private String newKeyTypes = "MASTER";

    @Parameter(
            names = "--new-secrets"
    )
    private String newKeySecrets = null;

    public GPKeys(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;

        // print the KIT
        os.println(card.getCardKeyInfo());
        os.println();

        if(context.isKeyLoggingEnabled()) {
            os.println("Old keys: " + card.getKeys());
            os.println();
        }

        if(newKeySecrets != null) {
            GPKeySet newKeys = buildKeysFromParameters(newKeyId, newKeyVersion, newKeyCipher, newKeyTypes, newKeySecrets);

            if (context.isKeyLoggingEnabled()) {
                os.println("New keys: " + newKeys);
                os.println();
            }

            os.println("Checking key compatibility...");
            card.getCardKeyInfo().checkKeySetForReplacement(newKeys);
            os.println("Check complete.");
            os.println();

            os.println("Uploading keys...");
            card.getIssuerDomain().replaceKeys(newKeys);
            os.println("Upload complete.");
            os.println();
        }
    }

}
