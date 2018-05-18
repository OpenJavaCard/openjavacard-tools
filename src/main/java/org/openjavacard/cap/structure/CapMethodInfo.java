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

public class CapMethodInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapMethodInfo.class);

    private static final int BF1_EXTENDED_FLAG = 0x80;
    private static final int BF1_ABSTRACT_FLAG = 0x40;

    private static final int BF1_SIMPLE_MAX_STACK_MASK  = 0x0F;
    private static final int BF1_SIMPLE_MAX_STACK_SHIFT = 0;

    private static final int BF2_SIMPLE_NUM_ARGS_MASK  = 0xF0;
    private static final int BF2_SIMPLE_NUM_ARGS_SHIFT = 4;

    private static final int BF2_SIMPLE_MAX_LOCALS_MASK = 0x0F;
    private static final int BF2_SIMPLE_MAX_LOCALS_SHIFT = 0;

    private boolean mExtended;
    private boolean mAbstract;

    private int mMaxStack;
    private int mNumArgs;
    private int mMaxLocals;

    public void read(CapStructureReader reader) throws IOException {
        int bf1 = reader.readU1();
        mExtended = (bf1 & BF1_EXTENDED_FLAG) == BF1_EXTENDED_FLAG;
        mAbstract = (bf1 & BF1_ABSTRACT_FLAG) == BF1_ABSTRACT_FLAG;
        if(mExtended) {
            mMaxStack = reader.readU1();
            mNumArgs = reader.readU1();
            mMaxLocals = reader.readU1();
        } else {
            int bf2 = reader.readU1();
            mMaxStack = (bf1 & BF1_SIMPLE_MAX_STACK_MASK) >> BF1_SIMPLE_MAX_STACK_SHIFT;
            mNumArgs = (bf2 & BF2_SIMPLE_NUM_ARGS_MASK) >> BF2_SIMPLE_NUM_ARGS_SHIFT;
            mMaxLocals = (bf2 & BF2_SIMPLE_MAX_LOCALS_MASK) >> BF2_SIMPLE_MAX_LOCALS_SHIFT;
        }
        LOG.trace("method " + (mExtended?"extended ":"") + (mAbstract?"abstract ":"")
                    + " maxStack " + mMaxStack + " numArgs " + mNumArgs + " maxLocals " + mMaxLocals);
    }

}
