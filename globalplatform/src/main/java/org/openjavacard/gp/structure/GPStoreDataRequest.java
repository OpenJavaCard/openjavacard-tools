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

import org.openjavacard.tlv.TLVPrimitive;
import org.openjavacard.util.ToBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GPStoreDataRequest implements ToBytes {
    private static final int TAG_ISSUER_IDENTIFICATION_NUMBER = 0x4200;
    private static final int TAG_CARD_IMAGE_NUMBER = 0x4500;
    private static final int TAG_ISD_AID = 0x4F00;

    public byte[] cardIIN;
    public byte[] cardCIN;
    public byte[] cardISD;

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if(cardIIN != null) {
                bos.write(new TLVPrimitive(TAG_ISSUER_IDENTIFICATION_NUMBER, cardIIN).getEncoded());
            }
            if(cardCIN != null) {
                bos.write(new TLVPrimitive(TAG_CARD_IMAGE_NUMBER, cardCIN).getEncoded());
            }
            if(cardISD != null) {
                bos.write(new TLVPrimitive(TAG_ISD_AID, cardISD).getEncoded());
            }
        } catch (IOException e) {
            throw new Error("Error serializing STORE DATA request", e);
        }
        return bos.toByteArray();
    }
}
