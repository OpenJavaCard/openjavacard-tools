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

package org.openjavacard.gp.structure;

import org.openjavacard.util.HexUtil;

import java.util.Arrays;

/**
 * Parsed response to an INIT UPDATE command
 * <p/>
 * This is somewhat magic because the structure differs
 * slightly between SCP versions, so total size can differ.
 */
public class GPInitUpdateResponse {

    /**
     * Length of data for SCP01/02
     */
    static final int SCP0102_LENGTH = 28;
    /**
     * Length of data for SCP03
     */
    static final int SCP03_LENGTH = 32;

    /**
     * Key diversification data
     */
    public final byte[] diversificationData;
    /**
     * Key version selected by card
     */
    public final int keyVersion;
    /**
     * SCP version selected by card
     */
    public final int scpProtocol;
    /**
     * SCP parameters selected by card (SCP03 only)
     */
    public final int scp03Parameters;
    /**
     * Card challenge for authentication
     */
    public final byte[] cardChallenge;
    /**
     * Card cryptogram for authentication
     */
    public final byte[] cardCryptogram;
    /**
     * Session sequence number for authentication (SCP03 only, others use part of challenge)
     */
    public final byte[] scp03Sequence;

    /**
     * Parse and construct an INIT UPDATE response
     *
     * @param data to be parsed
     */
    public GPInitUpdateResponse(byte[] data) {
        int offset = 0;
        int length = data.length;

        // check for possible lengths
        if (length != SCP0102_LENGTH && length != SCP03_LENGTH) {
            throw new IllegalArgumentException("Invalid INIT UPDATE response length " + length);
        }

        // key diversification data
        diversificationData = Arrays.copyOfRange(data, offset, offset + 10);
        offset += diversificationData.length;

        // key version that the card uses
        keyVersion = data[offset++] & 0xFF;

        // SCP protocol version
        scpProtocol = data[offset++] & 0xFF;

        // SCP03 has protocol parameters here
        if (scpProtocol == 3) {
            scp03Parameters = data[offset++] & 0xFF;
        } else {
            scp03Parameters = 0;
        }

        // card challenge for authentication
        cardChallenge = Arrays.copyOfRange(data, offset, offset + 8);
        offset += cardChallenge.length;

        // card cryptogram for authentication
        cardCryptogram = Arrays.copyOfRange(data, offset, offset + 8);
        offset += cardCryptogram.length;

        // SCP03 has its sequence number here
        if (scpProtocol == 3) {
            scp03Sequence = Arrays.copyOfRange(data, offset, offset + 3);
            offset += scp03Sequence.length;
        } else {
            scp03Sequence = null;
        }

        // check that we have consumed everything
        if (offset != length) {
            throw new IllegalArgumentException("BUG: INIT UPDATE response length mismatch");
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("INIT UPDATE response:");
        sb.append("\n scpVersion ");
        sb.append(HexUtil.hex8(scpProtocol));
        if (scpProtocol == 3) {
            sb.append("\n scp03Parameters ");
            sb.append(HexUtil.hex8(scp03Parameters));
        }
        sb.append("\n keyVersion ");
        sb.append(HexUtil.hex8(keyVersion));
        sb.append("\n cardChallenge ");
        sb.append(HexUtil.bytesToHex(cardChallenge));
        sb.append("\n cardCryptogram ");
        sb.append(HexUtil.bytesToHex(cardCryptogram));
        sb.append("\n diversificationData ");
        sb.append(HexUtil.bytesToHex(diversificationData));
        if (scpProtocol == 3) {
            sb.append("\n scp03Sequence ");
            sb.append(HexUtil.bytesToHex(scp03Sequence));
        }
        return sb.toString();
    }
}
