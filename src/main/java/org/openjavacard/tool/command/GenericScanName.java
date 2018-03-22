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

import org.openjavacard.generic.GenericCard;
import org.openjavacard.generic.GenericContext;
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SW;
import org.openjavacard.tool.converter.BytesConverter;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

@Parameters(
        commandNames = "scan-name",
        commandDescription = "Generic: Scan a card using SELECT BY NAME"
)
public class GenericScanName extends GenericCommand {

    @Parameter(
            names = "--base",
            converter = BytesConverter.class,
            description = "AID to start scanning at"
    )
    byte[] aidBase;

    @Parameter(
            names = "--count",
            description = "Number of sequential AIDs to try"
    )
    int aidCount = 256;

    @Parameter(
            names = "--depth",
            description = "Hierarchical depth of AIDs to try"
    )
    int aidDepth = 0;

    @Parameter(
            names = "--recurse",
            description = "Hierarchical depth to recurse into found AIDs"
    )
    int aidRecurse = 0;

    @Parameter(
            names = "--verbose",
            description = "Display each AID while scanning"
    )
    boolean verbose = false;

    public GenericScanName(GenericContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GenericCard card) throws CardException {
        scanNames(card, aidBase, aidDepth, aidRecurse);
    }

    private void scanNames(GenericCard card, byte[] base, int depth, int recurse) throws CardException {
        PrintStream os = System.out;

        BigInteger bStart = new BigInteger(Arrays.copyOf(base, base.length + depth));
        BigInteger bLimit;
        if(aidDepth != 0) {
            BigInteger bDepth = BigInteger.valueOf(1 << (depth * 8));
            bLimit = bStart.add(bDepth);
        } else {
            BigInteger bCount = BigInteger.valueOf(aidCount);
            bLimit = bStart.add(bCount);
        }

        os.println("SCANNING NAMES FROM " + HexUtil.bytesToHex(bStart.toByteArray())
                + " BELOW " + HexUtil.bytesToHex(bLimit.toByteArray()));

        ArrayList<byte[]> found = new ArrayList<>();
        for(BigInteger index = bStart;
            index.compareTo(bLimit) < 0;
            index = index.add(BigInteger.ONE)) {
            byte[] name = index.toByteArray();
            if(verbose || (name[name.length - 1] == (byte)0xFF)) {
                os.println(" PROGRESS " + HexUtil.bytesToHex(name));
            }
            ResponseAPDU rapdu = performSelect(card, name, true);
            int sw = rapdu.getSW();
            if(sw == 0x9000) {
                String foundLog = "  FOUND " + HexUtil.bytesToHex(name);
                found.add(name);
                byte[] data = rapdu.getData();
                if(data.length > 0) {
                    foundLog += " DATA " + HexUtil.bytesToHex(data);
                }
                os.println(foundLog);
                card.reconnect(true);
            } else if(sw == ISO7816.SW_FILE_NOT_FOUND) {
                continue;
            } else {
                os.println("  ERROR " + HexUtil.bytesToHex(name) + " " + SW.toString(sw));
                card.reconnect(true);
            }
        }

        if(found.isEmpty()) {
            os.println("  FOUND NOTHING");
        }

        if(recurse > 0) {
            for(byte[] aid: found) {
                scanNames(card, aid, 1, recurse - 1);
            }
        }
    }

    private ResponseAPDU performSelect(GenericCard card, byte[] aid, boolean first) throws CardException {
        byte p1 = ISO7816.SELECT_P1_BY_NAME;
        byte p2 = first ? ISO7816.SELECT_P2_FIRST_OR_ONLY : ISO7816.SELECT_P2_NEXT;
        CommandAPDU scapdu = APDUUtil.buildCommand(
                ISO7816.CLA_ISO7816, ISO7816.INS_SELECT,
                p1, p2, aid);
        return card.transmit(scapdu);
    }

}
