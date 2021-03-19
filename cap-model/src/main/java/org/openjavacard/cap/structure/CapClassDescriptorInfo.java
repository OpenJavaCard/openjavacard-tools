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

package org.openjavacard.cap.structure;

import org.openjavacard.cap.base.CapStructure;
import org.openjavacard.cap.base.CapStructureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Descriptor component: descriptor for a class
 */
public class CapClassDescriptorInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapClassDescriptorInfo.class);

    /** References to implemented interfaces */
    private ArrayList<CapClassRef> mInterfaces;
    /** Descriptors for fields */
    private ArrayList<CapFieldDescriptorInfo> mFields;
    /** Descriptors for methods */
    private ArrayList<CapMethodDescriptorInfo> mMethods;

    public ArrayList<CapClassRef> getInterfaces() {
        return mInterfaces;
    }

    public ArrayList<CapFieldDescriptorInfo> getFields() {
        return mFields;
    }

    public ArrayList<CapMethodDescriptorInfo> getMethods() {
        return mMethods;
    }

    public void read(CapStructureReader reader) throws IOException {
        int token = reader.readU1();
        int accessFlags = reader.readU1();
        CapClassRef classRef = reader.readClassRef();
        LOG.trace("class token " + token + " accessFlags " + accessFlags + " ref " + classRef);
        int interfaceCount = reader.readU1();
        int fieldCount = reader.readU2();
        int methodCount = reader.readU2();
        LOG.trace("class interfaceCount " + interfaceCount + " fieldCount " + fieldCount + " methodCount " + methodCount);

        ArrayList<CapClassRef> ifcs = new ArrayList<>();
        for(int j = 0; j < interfaceCount; j++) {
            ifcs.add(reader.readClassRef());
        }
        mInterfaces = ifcs;
        LOG.trace("read " + ifcs.size() + " interface references");

        ArrayList<CapFieldDescriptorInfo> fdis = new ArrayList<>();
        for(int j = 0; j < fieldCount; j++) {
            fdis.add(reader.readStructure(CapFieldDescriptorInfo.class));
        }
        mFields = fdis;
        LOG.trace("read " + fdis.size() + " field descriptors");

        ArrayList<CapMethodDescriptorInfo> mdis = new ArrayList<>();
        for(int j = 0; j < methodCount; j++) {
            mdis.add(reader.readStructure(CapMethodDescriptorInfo.class));
        }
        mMethods = mdis;
        LOG.trace("read " + mdis.size() + " method descriptors");
    }

}
