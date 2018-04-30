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

package org.openjavacard.cap.component;

import org.openjavacard.cap.file.CapComponentType;
import org.openjavacard.cap.io.CapComponent;
import org.openjavacard.cap.io.CapStructureReader;
import org.openjavacard.cap.structure.CapClassDescriptorInfo;
import org.openjavacard.cap.structure.CapExceptionHandlerInfo;
import org.openjavacard.cap.structure.CapMethodDescriptorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class CapMethodComponent extends CapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CapMethodComponent.class);

    public CapMethodComponent() {
        super(CapComponentType.Method);
    }

    @Override
    public void read(CapStructureReader reader) throws IOException {
        int handlerCount = reader.readU1();
        LOG.trace("reading " + handlerCount + " handlers");
        ArrayList<CapExceptionHandlerInfo> ehis = new ArrayList<>();
        for(int i = 0; i < handlerCount; i++) {
            ehis.add(reader.readStructure(CapExceptionHandlerInfo.class));
        }
        int methodInfoLength = reader.available();
        LOG.trace("method info is " + reader.available() + " bytes");
        byte[] methodInfo = reader.readBytes(methodInfoLength);
    }

    public void decodeMethodInfo(CapDescriptorComponent descriptorComponent) {
        for(CapClassDescriptorInfo classInfo: descriptorComponent.getClassInfos()) {
            for(CapMethodDescriptorInfo methodInfo: classInfo.getMethods()) {
            }
        }
    }

}
