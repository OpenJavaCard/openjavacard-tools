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

package org.openjavacard.util;

import org.openjavacard.iso.SWInfo;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Utilities related to APDU objects
 *
 * Namely verbose APDU printing and convenient APDU construction.
 *
 * This fills some convenience and debuggability gaps in the smartcard API.
 */
public class APDUUtil {

    /**
     * Stringify a command APDU verbosely
     *
     * @param apdu to stringify
     * @return string representing the APDU
     */
    public static String toString(CommandAPDU apdu) {
        byte[] data = apdu.getData();
        String dataString = " (no data)";
        if (data != null) {
            int dataLength = data.length;
            if (dataLength > 0) {
                dataString = " LC=" + HexUtil.hex8(dataLength)
                        + " DATA=" + HexUtil.bytesToHex(data);
            }
        }
        String p12 = HexUtil.bytesToHex(
                new byte[]{
                        (byte) apdu.getP1(),
                        (byte) apdu.getP2()}
        );
        return "CLA=" + HexUtil.hex8(apdu.getCLA())
                + " INS=" + HexUtil.hex8(apdu.getINS())
                + " P12=" + p12
                + dataString
                + " LE=" + apdu.getNe();
    }

    /**
     * Stringify a response APDU verbosely
     *
     * @param apdu to stringify
     * @return string representing the APDU
     */
    public static String toString(ResponseAPDU apdu) {
        StringBuffer sb = new StringBuffer();
        int sw = apdu.getSW();
        sb.append("SW=" + HexUtil.hex16(sw));
        SWInfo swData = SWInfo.get(sw);
        if (swData != null) {
            sb.append(" [");
            sb.append(swData.name);
            sb.append("]");
        } else {
            sb.append(" [unknown]");
        }
        byte[] data = apdu.getData();
        if (data == null || data.length == 0) {
            sb.append(" (no data)");
        } else {
            sb.append(" LE=" + data.length);
            sb.append(" DATA=" + HexUtil.bytesToHex(data));
        }
        return sb.toString();
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins) {
        return buildCommand(cla, ins, (byte) 0, (byte) 0, null);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p1
     * @param p2
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, byte p1, byte p2) {
        return buildCommand(cla, ins, p1, p2, null);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param data
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, byte[] data) {
        return buildCommand(cla, ins, (byte) 0, (byte) 0, data);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p12
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, short p12) {
        return buildCommand(cla, ins, p12, null);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p12
     * @param data
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, short p12, byte[] data) {
        return buildCommand(cla, ins,
                (byte) ((p12 >> 8) & 0xFF),
                (byte) ((p12 >> 0) & 0xFF),
                data);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p1
     * @param p2
     * @param data
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, byte p1, byte p2, byte[] data) {
        return new CommandAPDU(cla, ins, p1, p2, data, 256);
    }

}
