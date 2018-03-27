/*
 * openjavacard-tools: OpenJavaCard Development Tools
 * Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de
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
 *
 */

package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.gp.keys.GPKeyDiversification;
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
            names = "--reader",
            description = "Reader to use for the operation"
    )
    protected String reader = null;

    @Parameter(
            names = "--keystore-file",
            description = "Keystore: file containing keystore"
    )
    protected String keystoreFile = null;

    @Parameter(
            names = "--keystore-type",
            description = "Keystore: type of keystore"
    )
    protected String keystoreType = null;

    @Parameter(
            names = "--keystore-password",
            description = "Keystore: password for keystore"
    )
    protected String keystorePassword = null;

    @Parameter(
            names = "--isd",
            description = "Use specified ISD"
    )
    protected AID isd;

    @Parameter(
            names = "--scp-diversify",
            description = "Use specified key diversification"
    )
    protected GPKeyDiversification scpDiversify = GPKeyDiversification.NONE;

    @Parameter(
            names = "--scp-protocol",
            description = "Require specified SCP protocol"
    )
    protected String scpProtocol = "00";

    @Parameter(
            names = "--scp-parameters",
            description = "Require specified SCP parameters"
    )
    protected String scpParameters = "00";

    @Parameter(
            names = "--scp-security",
            description = "Require specified SCP security level"
    )
    protected SCPSecurityPolicy scpSecurity = SCPSecurityPolicy.CMAC;

    protected GPContext mContext;

    public GPCommand(GPContext context) {
        mContext = context;
    }

    public GPContext getContext() {
        return mContext;
    }

    @Override
    public void run() {
        if(scpDiversify != GPKeyDiversification.NONE) {
            throw new Error("Key diversification is not supported yet");
        }
        PrintStream os = System.out;
        if(keystoreFile != null) {
            os.println("Opening keystore " + keystoreFile);
            KeyStore ks = openKeyStore();
            try {
                Enumeration<String> aliases = ks.aliases();
                while(aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    os.println("Keystore contains " + alias);
                }
            } catch (KeyStoreException e) {
                throw new Error("Could not load keystore aliases", e);
            }
        }
        GPCard card = mContext.findSingleGPCard(reader, isd);
        try {
            AID isdConf = card.getISD();
            os.println("Host GP configuration:");
            os.println("  ISD " + ((isdConf==null)?"auto":isdConf));
            int protocol = HexUtil.unsigned8(scpProtocol);
            int parameters = HexUtil.unsigned8(scpParameters);
            SCPProtocolPolicy protocolPolicy = new SCPProtocolPolicy(protocol, parameters);
            os.println("  Protocol policy " + protocolPolicy);
            card.setProtocolPolicy(protocolPolicy);
            os.println("  Security policy " + scpSecurity);
            card.setSecurityPolicy(scpSecurity);
            os.println();
            os.println("CONNECTING");
            card.connect();
            performOperation(mContext, card);
            os.println("DISCONNECTING");
            card.disconnect();
        } catch (CardException e) {
            throw new Error("Error performing operation", e);
        }
    }

    protected void performOperation(GPContext context, GPCard card) throws CardException {
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

}
