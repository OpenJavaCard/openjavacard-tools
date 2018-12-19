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
import org.openjavacard.iso.SWException;
import org.openjavacard.tool.converter.HexByteConverter;
import org.openjavacard.tool.converter.HexShortConverter;
import org.openjavacard.util.APDUUtil;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "apdu",
        commandDescription = "Generic: Send an APDU to the card "
)
public class GenericAPDU extends GenericCommand {

    @Parameter(
            names = "--cla",
            description = "CLA of the command",
            converter = HexByteConverter.class,
            required = true
    )
    byte apduCLA;
    @Parameter(
            names = "--ins",
            description = "INS of the command",
            converter = HexByteConverter.class,
            required = true
    )
    byte apduINS;
    @Parameter(
            names = "--p12",
            description = "P12 of the command",
            converter = HexShortConverter.class
    )
    short apduP12;

    @Parameter(
            names = "--data",
            description = "Command data"
    )
    byte[] apduData;

    @Parameter(
            names = "--file",
            description = "File with command data"
    )
    File apduFile;

    @Parameter(
            description = "Raw APDUs"
    )
    List<byte[]> raw;

    public GenericAPDU(GenericContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GenericCard card) throws CardException {
        PrintStream os = System.out;
        CardChannel channel = card.getBasicChannel();

        byte[] data = buildData();

        if (raw == null || raw.isEmpty()) {
            sendArg(os, card, channel, data);
        } else {
            sendRaw(os, card, channel);
        }
    }

    private void sendArg(PrintStream os, GenericCard card, CardChannel channel, byte[] data) throws CardException {
        CommandAPDU capdu = APDUUtil.buildCommand(
                apduCLA, apduINS, apduP12, data
        );
        os.println("APDU > " + APDUUtil.toString(capdu));
        ResponseAPDU rapdu = card.transmit(channel, capdu);
        os.println("APDU < " + APDUUtil.toString(rapdu));
        int sw = rapdu.getSW();
        if (sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error executing command", sw);
        }
    }

    private void sendRaw(PrintStream os, GenericCard card, CardChannel channel) throws CardException {
        for(byte[] apdu: raw) {
            CommandAPDU capdu = new CommandAPDU(apdu);
            os.println("APDU > " + APDUUtil.toString(capdu));
            ResponseAPDU rapdu = card.transmit(channel, capdu);
            os.println("APDU < " + APDUUtil.toString(rapdu));
            int sw = rapdu.getSW();
            if(sw != ISO7816.SW_NO_ERROR) {
                throw new SWException("Error executing command", sw);
            }
        }
    }

    private byte[] buildData() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if(apduData != null) {
                bos.write(apduData);
            }
            if(apduFile != null) {
                try {
                    FileInputStream fis = new FileInputStream(apduFile);
                    int len = fis.available();
                    if(len > 255) {
                        throw new RuntimeException("Data file to long");
                    }
                    byte[] data = new byte[len];
                    fis.read(data);
                    bos.write(data);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

}
