/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
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
        commandDescription = "AID: Generate a self-assigned AID"
)
public class AIDNow implements Runnable {

    @Parameter(
            names = "--production",
            description = "Mark the AIDs for production use"
    )
    boolean production;

    @Override
    public void run() {
        PrintStream os = System.out;

        os.println();
        os.println("Welcome to AID Now!");
        os.println();
        os.println("This program will generate a self-assigned AID for your project.");
        os.println();

        if(production) {
            os.println("The following assignment is for PRODUCTION applications only.");
            os.println();
            os.println("Experimental applications should use an EXPERIMENTAL identifier instead.");
        } else {
            os.println("The following assignment is for EXPERIMENTAL applications only.");
            os.println();
            os.println("Specify the --production option to generate production identifiers.");
        }
        os.println();

        SecureRandom random = new SecureRandom();
        int randomLength = 8;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] seed = random.generateSeed(digest.getDigestLength());
            digest.reset();
            digest.update(seed);
            byte[] hash = digest.digest();

            os.println("================================================================================");
            os.println();
            os.println("For an APPLICATION please use the following selectable AID");
            os.println();

            AID aidApp = buildAID(
                    ASSIGN_APPLICATION.getAID(production),
                    hash, 0, randomLength);

            os.println("    " + aidApp);
            os.println();

            os.println("You can construct AIDs for multiple instances like this");
            os.println();

            for(int i = 1; i <= 3; i++) {
                os.println("    " + aidApp + " 0" + i);
            }
            os.println("    " + "...");
            os.println();

            os.println("The corresponding PACKAGE should be named");
            os.println();

            AID aidPkg = buildAID(
                    ASSIGN_PACKAGE.getAID(production),
                    hash, 0, randomLength);

            os.println("    " + aidPkg);
            os.println();

            os.println("Any MODULES in the package should be named");
            os.println();

            for(int i = 1; i <= 3; i++) {
                os.println("    " + aidPkg + " 0" + i);
            }
            os.println("    " + "...");
            os.println();

            os.println("================================================================================");
            os.println();
            os.println("For a LIBRARY project please use the following AID");
            os.println();

            AID aidLib = buildAID(
                    ASSIGN_LIBRARY.getAID(production),
                    hash, 0, randomLength);

            os.println("    " + aidLib);
            os.println();

            os.println("================================================================================");
            os.println();
            os.println("For a SECURITY DOMAIN please use the following AID");
            os.println();

            AID aidDom = buildAID(
                    ASSIGN_DOMAIN.getAID(production),
                    hash, 0, randomLength);

            os.println("    " + aidDom);
            os.println();

            os.println("================================================================================");
            os.println();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        os.println("This service is provided by OpenJavaCard in cooperation with signal interrupt.");
        os.println();
        os.println("See https://openjavacard.org/resources/aid-now.html for more information.");
        os.println();
    }

    private AID buildAID(AID base, byte[] tailBuf, int tailOff, int tailLen) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(base.getBytes());
        bos.write(tailBuf, tailOff, tailLen);
        byte[] aidBytes = bos.toByteArray();
        return new AID(aidBytes);
    }

    static class Assignment {
        final String name;
        final String description;
        final AID aidExperimental;
        final AID aidProduction;
        Assignment(String name, String description, AID aidExperimental, AID aidProduction) {
            this.name = name;
            this.description = description;
            this.aidExperimental = aidExperimental;
            this.aidProduction = aidProduction;
        }
        AID getAID(boolean production) {
            if(production) {
                return aidProduction;
            } else {
                return aidExperimental;
            }
        }
    }

    static AID BASE_EXPERIMENTAL_APPLICATION = new AID("D276000177E0");
    static AID BASE_EXPERIMENTAL_PACKAGE = new AID("D276000177E1");
    static AID BASE_EXPERIMENTAL_LIBRARY = new AID("D276000177E2");
    static AID BASE_EXPERIMENTAL_DOMAIN = new AID("D276000177E3");

    static AID BASE_PRODUCTION_APPLICATION = new AID("D276000177F0");
    static AID BASE_PRODUCTION_PACKAGE = new AID("D276000177F1");
    static AID BASE_PRODUCTION_LIBRARY = new AID("D276000177F2");
    static AID BASE_PRODUCTION_DOMAIN = new AID("D276000177F3");

    static Assignment ASSIGN_APPLICATION = new Assignment(
            "Application", "Selectable applications (applets)",
            BASE_EXPERIMENTAL_APPLICATION, BASE_PRODUCTION_APPLICATION);
    static Assignment ASSIGN_PACKAGE = new Assignment(
            "Package", "Installable packages (containing modules)",
            BASE_EXPERIMENTAL_PACKAGE, BASE_PRODUCTION_PACKAGE);
    static Assignment ASSIGN_LIBRARY = new Assignment(
            "Library", "Installable libraries (usually without modules)",
            BASE_EXPERIMENTAL_LIBRARY, BASE_PRODUCTION_LIBRARY);
    static Assignment ASSIGN_DOMAIN = new Assignment(
            "Domain", "Security domains",
            BASE_EXPERIMENTAL_DOMAIN, BASE_PRODUCTION_DOMAIN);
}
