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

public class GPInstallForInstallRequest implements ToBytes {
    public AID packageAID;
    public AID moduleAID;
    public AID appletAID;
    public byte[] privileges;
    public byte[] installParameters;
    public byte[] installToken;

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if(packageAID == null) {
                throw new IOException("Package AID is mandatory");
            } else {
                bos.write(packageAID.getLength());
                bos.write(packageAID.getBytes());
            }
            if(moduleAID == null) {
                throw new IOException("Module AID is mandatory");
            } else {
                bos.write(moduleAID.getLength());
                bos.write(moduleAID.getBytes());
            }
            if(appletAID == null) {
                throw new IOException("Applet AID is mandatory");
            } else {
                bos.write(appletAID.getLength());
                bos.write(appletAID.getBytes());
            }
            if(privileges == null) {
                throw new IOException("Privileges are mandatory");
            } else {
                bos.write(privileges.length);
                bos.write(privileges);
            }
            if(installParameters == null) {
                bos.write(new byte[] { (byte)0x02, (byte)0xC9, (byte)0x00 });
            } else {
                bos.write(installParameters.length + 2);
                bos.write((byte)0xC9);
                bos.write(installParameters.length);
                bos.write(installParameters);
            }
            if(installToken == null) {
                bos.write(0);
            } else {
                bos.write(installToken.length);
                bos.write(installToken);
            }
        } catch (IOException e) {
            throw new Error("Error serializing INSTALL [for INSTALL] request", e);
        }
        return bos.toByteArray();
    }
}
