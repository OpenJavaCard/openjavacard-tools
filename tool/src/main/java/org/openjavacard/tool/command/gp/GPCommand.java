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
import com.beust.jcommander.validators.PositiveInteger;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeyDiversification;
import org.openjavacard.gp.keys.GPKeyId;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.gp.keys.GPKeyVersion;
import org.openjavacard.gp.scp.SCPProtocolPolicy;
import org.openjavacard.gp.scp.SCPSecurityPolicy;
import org.openjavacard.iso.AID;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import java.io.PrintStream;

public abstract class GPCommand implements Runnable {

    @Parameter(
            names = "--reader", order = 100,
            description = "Reader to use"
    )
    protected String reader = null;

    @Parameter(
            names = "--isd", order = 200,
            description = "AID of the issuer security domain"
    )
    protected AID isd;

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

    @Parameter(
            names = "--force-protected", order = 800,
            description = "Force operation on protected object"
    )
    boolean forceProtected = false;

    @Parameter(
            names = "--log-keys", order = 900,
            description = "Allow writing keys into the debug log"
    )
    private boolean logKeys = false;

    private GPContext mContext;

    public GPCommand(GPContext context) {
        mContext = context;
    }

    public GPContext getContext() {
        return mContext;
    }

    @Override
    public void run() {
        GPCard card = prepareOperation();
        try {
            beforeOperation(card);
            performOperation(mContext, card);
            afterOperation(card);
        } catch (CardException e) {
            throw new Error("Error performing operation", e);
        }
    }

    /**
     * Prepare for GP operation and detect card
     * @return card or null if not found
     */
    private GPCard prepareOperation() {
        PrintStream os = System.out;

        GPKeySet keys = GPKeySet.GLOBALPLATFORM;

        if(logKeys) {
            mContext.enableKeyLogging();
        }

        if(scpKeySecrets != null) {
            keys = buildKeysFromParameters(scpKeyId, scpKeyVersion, scpKeyCipher, scpKeyTypes, scpKeySecrets);
        }

        return mContext.findSingleGPCard(reader, isd, keys);
    }

    /**
     * Executed before performing the operation
     * @param card
     * @throws CardException
     */
    private void beforeOperation(GPCard card) throws CardException {
        PrintStream os = System.out;

        AID isdConf = card.getISD();
        os.println("Host GP configuration:");
        os.println("  ISD " + ((isdConf==null)?"auto":isdConf));
        int protocol = HexUtil.unsigned8(scpProtocol);
        int parameters = HexUtil.unsigned8(scpParameters);
        SCPProtocolPolicy protocolPolicy = new SCPProtocolPolicy(protocol, parameters);
        os.println("  Key diversification " + scpDiversification);
        card.setDiversification(scpDiversification);
        os.println("  Protocol policy " + protocolPolicy);
        card.setProtocolPolicy(protocolPolicy);
        os.println("  Security policy " + scpSecurity);
        card.setSecurityPolicy(scpSecurity);
        os.println();
        os.println("CONNECTING");
        card.connect();
        os.println();
    }

    /**
     * Override this for the operation itself
     * @param context
     * @param card
     * @throws CardException
     */
    protected void performOperation(GPContext context, GPCard card) throws CardException {
    }

    /**
     * Executed after performing the operation
     * @param card
     * @throws CardException
     */
    private void afterOperation(GPCard card) throws CardException {
        PrintStream os = System.out;

        os.println("DISCONNECTING");
        card.disconnect();
        os.println();
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
