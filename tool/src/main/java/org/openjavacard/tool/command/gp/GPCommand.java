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
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyType;
import org.openjavacard.gp.scp.SCPProtocolPolicy;
import org.openjavacard.gp.scp.SCPSecurityPolicy;
import org.openjavacard.iso.AID;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

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
            description = "User-specified keys: key version",
            validateWith = PositiveInteger.class
    )
    private int scpKeyVersion = 1;

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
    private GPKeyCipher scpKeyCipher = GPKeyCipher.DES3;

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
            names = "--keystore-file", order = 500,
            description = "Keystore: file containing keystore"
    )
    private String keystoreFile = null;

    @Parameter(
            names = "--keystore-type", order = 500,
            description = "Keystore: type of keystore"
    )
    private String keystoreType = null;

    @Parameter(
            names = "--keystore-password", order = 500,
            description = "Keystore: password for keystore"
    )
    private String keystorePassword = null;

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
        } else {
            if (keystoreFile != null) {
                os.println("Opening keystore " + keystoreFile);
                KeyStore ks = openKeyStore();
                try {
                    Enumeration<String> aliases = ks.aliases();
                    while (aliases.hasMoreElements()) {
                        String alias = aliases.nextElement();
                        os.println("Keystore contains " + alias);
                    }
                } catch (KeyStoreException e) {
                    throw new Error("Could not load keystore aliases", e);
                }
            }
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

    private KeyStore openKeyStore() {
        KeyStore ks;

        String ksType = keystoreType;
        if(ksType == null) {
            ksType = KeyStore.getDefaultType();
        }
        String ksPath = keystoreFile;
        if(ksPath == null) {
            throw new Error("No keystore file specified");
        }
        File ksFile = new File(ksPath);
        if(!(ksFile.exists() && ksFile.isFile())) {
            throw new Error("Not a file: " + ksFile);
        }
        String ksPass = keystorePassword;
        char[] ksPassRaw = null;
        if(ksPass != null) {
            ksPassRaw = ksPass.toCharArray();
        }
        try {
            InputStream kis = new FileInputStream(ksFile);
            ks = KeyStore.getInstance(ksType);
            ks.load(kis, ksPassRaw);
        } catch (FileNotFoundException e) {
            throw new Error("Could not open keystore", e);
        } catch (KeyStoreException e) {
            throw new Error("Could not open keystore", e);
        } catch (CertificateException e) {
            throw new Error("Could not open keystore", e);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("Could not open keystore", e);
        } catch (IOException e) {
            throw new Error("Could not open keystore", e);
        }

        return ks;
    }

    protected GPKeySet buildKeysFromParameters(int pKeyVersion, int pKeyId, GPKeyCipher pKeyCipher, String pKeyTypes, String pKeySecrets) {
        if(pKeyVersion > 255) {
            throw new Error("Bad key version");
        }
        // XXX this is not comprehensive because of the loop and protocol variations
        if(pKeyId > 255) {
            throw new Error("Bad key id");
        }
        GPKeySet keys = new GPKeySet("commandline", pKeyVersion);
        String[] keyTypes = pKeyTypes.split(":");
        String[] keySecrets = pKeySecrets.split(":");
        if(keyTypes.length != keySecrets.length) {
            throw new Error("Must provide an equal number of key types and secrets");
        }
        int numKeys = keyTypes.length;
        for(int i = 0; i < numKeys; i++) {
            GPKeyType keyType = GPKeyType.valueOf(keyTypes[i]);
            byte[] keySecret = HexUtil.hexToBytes(keySecrets[i]);
            byte keyId = (byte)(pKeyId + i);
            if(keyType == GPKeyType.MASTER) {
                keyId = 0;
            }
            GPKey key = new GPKey(keyType, keyId, pKeyCipher, keySecret);
            keys.putKey(key);
        }
        return keys;
    }

}
