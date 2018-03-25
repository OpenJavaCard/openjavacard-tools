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

import org.openjavacard.cap.io.CapStructure;
import org.openjavacard.cap.io.CapStructureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class CapClassDescriptorInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapClassDescriptorInfo.class);

    private ArrayList<CapClassRef> mInterfaces;
    private ArrayList<CapFieldDescriptorInfo> mFields;
    private ArrayList<CapMethodDescriptorInfo> mMethods;

    public void read(CapStructureReader reader) throws IOException {
        int token = reader.readU1();
        int accessFlags = reader.readU1();
        long classRef = reader.readU2();
        LOG.trace("class token " + token + " accessFlags " + accessFlags + " ref " + classRef);
        int interfaceCount = reader.readU1();
        int fieldCount = reader.readU2();
        int methodCount = reader.readU2();
        LOG.trace("class interfaceCount " + interfaceCount + " fieldCount " + fieldCount + " methodCount " + methodCount);
        ArrayList<CapClassRef> ifcs = new ArrayList<>();
        for(int j = 0; j < interfaceCount; j++) {
            ifcs.add(reader.readClassRef());
            LOG.trace("interface " + j);
        }
        mInterfaces = ifcs;
        ArrayList<CapFieldDescriptorInfo> fdis = new ArrayList<>();
        for(int j = 0; j < fieldCount; j++) {
            CapFieldDescriptorInfo fdi = reader.readStructure(CapFieldDescriptorInfo.class);
            fdis.add(fdi);
            LOG.trace("field " + j);
        }
        mFields = fdis;
        ArrayList<CapMethodDescriptorInfo> mdis = new ArrayList<>();
        for(int j = 0; j < methodCount; j++) {
            CapMethodDescriptorInfo mdi = reader.readStructure(CapMethodDescriptorInfo.class);
            mdis.add(mdi);
            LOG.trace("method " + j);
        }
        mMethods = mdis;
    }

}
