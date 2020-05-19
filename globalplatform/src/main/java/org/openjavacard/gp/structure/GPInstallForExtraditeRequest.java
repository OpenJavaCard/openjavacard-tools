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

import org.openjavacard.iso.AID;
import org.openjavacard.util.ToBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GPInstallForExtraditeRequest implements ToBytes {
    public AID domainAID;
    public AID objectAID;
    public byte[] extraditionParameters;
    public byte[] extraditionToken;

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if(domainAID == null) {
                throw new IOException("Domain AID is mandatory");
            } else {
                bos.write(domainAID.getLength());
                bos.write(domainAID.getBytes());
            }
            bos.write(0); // never any data
            if(objectAID == null) {
                throw new IOException("Object AID is mandatory");
            } else {
                bos.write(objectAID.getLength());
                bos.write(objectAID.getBytes());
            }
            bos.write(0); // never any data
            if(extraditionParameters == null) {
                bos.write(0);
            } else {
                bos.write(extraditionParameters.length);
                bos.write(extraditionParameters);
            }
            if(extraditionToken == null) {
                bos.write(0);
            } else {
                bos.write(extraditionToken.length);
                bos.write(extraditionToken);
            }
        } catch (IOException e) {
            throw new Error("Error serializing INSTALL [for EXTRADITE] request", e);
        }
        return bos.toByteArray();
    }
}
