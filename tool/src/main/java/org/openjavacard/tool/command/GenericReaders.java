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
import javax.smartcardio.CardTerminal;
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
        commandNames = "readers",
        commandDescription = "Generic: List available readers"
)
public class GenericReaders implements Runnable {

    private GenericContext mContext;

    public GenericReaders(GenericContext context) {
        mContext = context;
    }

    @Override
    public void run() {
        PrintStream os = System.out;
        List<CardTerminal> terminals = mContext.findTerminals();
        for(CardTerminal terminal: terminals) {
            os.println("Reader: " + terminal.getName());
            os.println("Class: " + terminal.getClass().getName());
            os.println();
        }
    }

}
