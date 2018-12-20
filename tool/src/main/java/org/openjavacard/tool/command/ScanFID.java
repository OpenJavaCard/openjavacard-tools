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
import org.openjavacard.generic.GenericCard;
import org.openjavacard.generic.GenericContext;
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SW;
import org.openjavacard.tool.converter.Hex16Converter;
import org.openjavacard.tool.converter.Hex8Converter;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.BinUtil;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.PrintStream;
import java.util.ArrayList;

@Parameters(
        commandNames = "scan-fid",
        commandDescription = "Scanning: Scan a card using SELECT BY FILE ID"
)
public class ScanFID extends GenericCommand {

    @Parameter(
            names = "--base",
            description = "FID to start scanning at",
            converter = Hex16Converter.class
    )
    int fidBase = 0;

    @Parameter(
            names = "--count",
            description = "Number of sequential FIDs to try"
    )
    int fidCount = 65536;

    @Parameter(
            names = "--p1",
            description = "Custom P1 to use",
            converter = Hex8Converter.class
    )
    int customP1 = -1;

    @Parameter(
            names = "--p2",
            description = "Custom P2 to use",
            converter = Hex8Converter.class
    )
    int customP2 = -1;

    @Parameter(
            names = "--verbose",
            description = "Display each FID while scanning"
    )
    boolean verbose = false;

    public ScanFID(GenericContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GenericCard card) throws CardException {
        scanNames(card, fidBase, fidCount);
    }

    private void scanNames(GenericCard card, int base, int count) throws CardException {
        PrintStream os = System.out;

        int last = base + count - 1;

        os.println("SCANNING FILE ID FROM " + HexUtil.hex16(base) + " THROUGH " + HexUtil.hex16(last));

        ArrayList<Integer> found = new ArrayList<>();
        for(int current = base; current <= last; current++) {
            if(verbose) {
                os.println(" PROGRESS " + HexUtil.hex16(current));
            }
            ResponseAPDU rapdu = performSelect(card, current, true);
            int sw = rapdu.getSW();
            if(sw == 0x9000) {
                String foundLog = "  FOUND " + HexUtil.hex16(current);
                found.add(current);
                byte[] data = rapdu.getData();
                if(data.length > 0) {
                    foundLog += " DATA " + HexUtil.bytesToHex(data);
                }
                os.println(foundLog);
            } else if(sw == ISO7816.SW_FILE_NOT_FOUND) {
                continue;
            } else {
                os.println("  ERROR FID=" + HexUtil.hex16(current) + " " + SW.toString(sw));
            }
        }

        if(found.isEmpty()) {
            os.println("  FOUND NOTHING");
        }
    }

    private ResponseAPDU performSelect(GenericCard card, int fid, boolean first) throws CardException {
        byte p1;
        if(customP1 < 0) {
            p1 = ISO7816.SELECT_P1_BY_FILEID;
        } else {
            p1 = (byte)customP1;
        }
        byte p2;
        if(customP2 < 0) {
            p2 = first ? ISO7816.SELECT_P2_FIRST_OR_ONLY : ISO7816.SELECT_P2_NEXT;
        } else {
            p2 = (byte)customP2;
        }
        byte[] fidBytes = new byte[2];
        BinUtil.setShort(fidBytes, 0, (short)fid);
        CommandAPDU scapdu = APDUUtil.buildCommand(
                ISO7816.CLA_ISO7816, ISO7816.INS_SELECT,
                p1, p2, fidBytes);
        return card.transmit(scapdu);
    }

}
