/*
 * openjavacard-tools: OpenJavaCard Development Tools
 * Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de
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
 *
 */

package org.openjavacard.cap.structure;

import org.openjavacard.cap.base.CapStructure;
import org.openjavacard.cap.base.CapStructureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapTypeDescriptorInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapTypeDescriptorInfo.class);

    private ArrayList<CapTypeDescriptor> mTypeDescriptors;

    public List<CapTypeDescriptor> getTypeDescriptors() {
        return mTypeDescriptors;
    }

    public void read(CapStructureReader reader) throws IOException {
        int constantPoolCount = reader.readU2();
        int[] constantPoolTypes = reader.readU2Array(constantPoolCount);
        LOG.trace("read " + constantPoolCount + " constant pool types");
        ArrayList<CapTypeDescriptor> typeDescriptors = new ArrayList<>();
        while(reader.hasMore()) {
            typeDescriptors.add(reader.readStructure(CapTypeDescriptor.class));
        }
        mTypeDescriptors = typeDescriptors;
        LOG.trace("read " + typeDescriptors.size() + " type descriptors");
    }

}
