package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.EnumConverter;
import org.openjavacard.iso.AID;
import org.openjavacard.iso.AIDUsage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

@Parameters(
        commandNames = "aid-now",
        commandDescription = "Generate a self-assigned AID"
)
public class AIDNow implements Runnable {

    @Parameter(
            names = "--usage",
            description = "Usage types for generated AIDs",
            required = true
    )
    List<AIDUsage> types;

    @Parameter(
            names = "--production",
            description = "Mark the AIDs for production use"
    )
    boolean production;

    @Override
    public void run() {
        PrintStream os = System.out;

        SecureRandom random = new SecureRandom();

        for(AIDUsage type: types) {
            try {
                AID base = getBase(type);
                int randomLength = 16 - 2 - base.getLength();

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] seed = random.generateSeed(digest.getDigestLength());
                digest.update(seed);
                byte[] hash = digest.digest();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.write(base.getBytes());
                bos.write(hash, 0, randomLength);
                byte[] aidBytes = bos.toByteArray();
                AID aid = new AID(aidBytes);

                os.println("AID: " + aid);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private AID getBase(AIDUsage usage) {
        switch (usage) {
            case APPLET:
                if(production) {
                    return BASE_PRODUCTION_APPLICATION;
                } else {
                    return BASE_EXPERIMENTAL_APPLICATION;
                }
            case PACKAGE:
            case MODULE:
                if(production) {
                    return BASE_PRODUCTION_PACKAGE;
                } else {
                    return BASE_EXPERIMENTAL_PACKAGE;
                }
            case DOMAIN:
                if(production) {
                    return BASE_PRODUCTION_DOMAIN;
                } else {
                    return BASE_EXPERIMENTAL_DOMAIN;
                }
            default:
                throw new UnsupportedOperationException("Cannot generate AID for usage " + usage);
        }
    }

    static AID BASE_EXPERIMENTAL_APPLICATION = new AID("D276000177E0");
    static AID BASE_EXPERIMENTAL_PACKAGE = new AID("D276000177E1");
    static AID BASE_EXPERIMENTAL_DOMAIN = new AID("D276000177E2");

    static AID BASE_PRODUCTION_APPLICATION = new AID("D276000177F0");
    static AID BASE_PRODUCTION_PACKAGE = new AID("D276000177F1");
    static AID BASE_PRODUCTION_DOMAIN = new AID("D276000177F2");

}
