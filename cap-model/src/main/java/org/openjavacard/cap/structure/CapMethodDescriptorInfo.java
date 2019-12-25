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
 * Descriptor component: descriptor for a method
 */
public class CapMethodDescriptorInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapMethodDescriptorInfo.class);

    private int mToken;

    private int mAccessFlags;

    private int mOffset;

    private int mTypeOffset;

    private int mBytecodeCount;

    private int mExceptionCount;

    private int mExceptionIndex;

    public int getToken() {
        return mToken;
    }

    public int getAccessFlags() {
        return mAccessFlags;
    }

    public int getOffset() {
        return mOffset;
    }

    public int getTypeOffset() {
        return mTypeOffset;
    }

    public int getBytecodeCount() {
        return mBytecodeCount;
    }

    public int getExceptionCount() {
        return mExceptionCount;
    }

    public int getExceptionIndex() {
        return mExceptionIndex;
    }

    public void read(CapStructureReader reader) throws IOException {
        mToken = reader.readU1();
        mAccessFlags = reader.readU1();
        mOffset = reader.readU2();
        mTypeOffset = reader.readU2();
        mBytecodeCount = reader.readU2();
        mExceptionCount = reader.readU2();
        mExceptionIndex = reader.readU2();
        LOG.trace("method token " + mToken + " accessFlags " + mAccessFlags
                + " offset " + mOffset + " typeOffset " + mTypeOffset
                + " bytecodeCount " + mBytecodeCount
                + " exceptionCount " + mExceptionCount
                + " exceptionIndex " + mExceptionIndex);
    }

}
