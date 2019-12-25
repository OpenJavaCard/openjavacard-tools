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

/**
 * Export component: representation of an exported class
 */
public class CapClassExportInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapClassExportInfo.class);

    /** Reference to the exported class */
    private CapClassRef mClassRef;

    private int[] mStaticFieldOffsets;

    private int[] mStaticMethodOffsets;

    public CapClassRef getClassRef() {
        return mClassRef;
    }

    public int[] getStaticFieldOffsets() {
        return mStaticFieldOffsets;
    }

    public int[] getStaticMethodOffsets() {
        return mStaticMethodOffsets;
    }

    public void read(CapStructureReader reader) throws IOException {
        mClassRef = reader.readStructure(CapClassRef.class);
        int staticFieldCount = reader.readU1();
        int staticMethodCount = reader.readU1();
        LOG.trace("staticFields " + staticFieldCount + " staticMethods " + staticMethodCount);
        mStaticFieldOffsets = reader.readU2Array(staticFieldCount);
        mStaticMethodOffsets = reader.readU2Array(staticMethodCount);
    }

}
