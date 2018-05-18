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

public class CapClassInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapClassInfo.class);

    private boolean mIsShareable;
    private boolean mIsRemote;

    private CapClassRef mSuper;
    private int mDeclaredInstanceSize;
    private int mFirstReferenceToken;
    private int mReferenceCount;
    private int mPublicMethodTableBase;
    private int mPublicMethodTableCount;
    private int mPackageMethodTableBase;
    private int mPackageMethodTableCount;

    public void read(CapStructureReader reader) throws IOException {
        int bitfield = reader.readU1();
        int flags = (bitfield >> 4) & 0xF;
        int interfaceCount = bitfield & 0xF;
        mIsShareable = (flags & CapFlag.ACC_SHAREABLE) != 0;
        mIsRemote = (flags & CapFlag.ACC_REMOTE) != 0;
        mSuper = reader.readClassRef();
        mDeclaredInstanceSize = reader.readU1();
        mFirstReferenceToken = reader.readU1();
        mReferenceCount = reader.readU1();
        mPublicMethodTableBase = reader.readU1();
        mPublicMethodTableCount = reader.readU1();
        mPackageMethodTableBase = reader.readU1();
        mPackageMethodTableCount = reader.readU1();
        LOG.trace("class methods public " + mPublicMethodTableCount + " package " + mPackageMethodTableCount);
        int[] mPublicMethods = reader.readU2Array(mPublicMethodTableCount);
        int[] mPackageMethods = reader.readU2Array(mPackageMethodTableCount);
        for(int i = 0; i < interfaceCount; i++) {
            CapImplementedInterfaceInfo impl = new CapImplementedInterfaceInfo();
            impl.read(reader);
        }
        if(mIsRemote) {
            throw new IllegalArgumentException("Remote not supported");
        }
    }

}
