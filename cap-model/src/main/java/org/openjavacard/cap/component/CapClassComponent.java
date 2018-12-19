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

package org.openjavacard.cap.component;

import org.openjavacard.cap.base.CapComponent;
import org.openjavacard.cap.base.CapStructureReader;
import org.openjavacard.cap.file.CapComponentType;
import org.openjavacard.cap.structure.CapClassInfo;
import org.openjavacard.cap.structure.CapFlag;
import org.openjavacard.cap.structure.CapInterfaceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class CapClassComponent extends CapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CapClassComponent.class);

    private ArrayList<CapInterfaceInfo> mInterfaces;
    private ArrayList<CapClassInfo>     mClasses;

    public CapClassComponent() {
        super(CapComponentType.Class);
    }

    public ArrayList<CapInterfaceInfo> getInterfaces() {
        return mInterfaces;
    }

    public ArrayList<CapClassInfo> getClasses() {
        return mClasses;
    }

    @Override
    public void read(CapStructureReader reader) throws IOException {
        //int signaturePoolLength = reader.readU2();
        //LOG.trace("reading " + signaturePoolLength + " bytes of signature pool");
        ArrayList<CapClassInfo> classes = new ArrayList<>();
        ArrayList<CapInterfaceInfo> interfaces = new ArrayList<>();
        while(reader.hasMore()) {
            int bitfield = reader.peekU1();
            int flags = (bitfield >> 4) & 0xF;
            if ((flags & CapFlag.ACC_INTERFACE) != 0) {
                interfaces.add(reader.readStructure(CapInterfaceInfo.class));
            } else {
                classes.add(reader.readStructure(CapClassInfo.class));
            }
        }
        LOG.trace("read " + interfaces.size() + " interfaces" + classes.size() + " classes");
    }

}
