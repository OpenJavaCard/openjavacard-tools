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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openjavacard.cap.base.CapComponent;
import org.openjavacard.cap.base.CapStructureReader;
import org.openjavacard.cap.file.CapComponentType;
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

    private ArrayList<CapExceptionHandlerInfo> mExceptionHandlerInfos;

    private byte[] mRawMethodInfo;

    public ArrayList<CapExceptionHandlerInfo> getExceptionHandlerInfos() {
        return mExceptionHandlerInfos;
    }

    @JsonIgnore
    public byte[] getRawMethodInfo() {
        return mRawMethodInfo;
    }

    @Override
    public void read(CapStructureReader reader) throws IOException {
        int handlerCount = reader.readU1();
        LOG.trace("reading " + handlerCount + " handlers");
        mExceptionHandlerInfos = reader.readStructureArray(handlerCount, CapExceptionHandlerInfo.class);
        int methodInfoLength = reader.available();
        LOG.trace("method info is " + reader.available() + " bytes");
        mRawMethodInfo = reader.readBytes(methodInfoLength);
    }

    public void decodeMethodInfo(CapDescriptorComponent descriptorComponent) {
        for(CapClassDescriptorInfo classInfo: descriptorComponent.getClassInfos()) {
            for(CapMethodDescriptorInfo methodInfo: classInfo.getMethods()) {
            }
        }
    }

}
