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

public class CapInterfaceInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapInterfaceInfo.class);

    private boolean mShareable;
    private boolean mRemote;

    private ArrayList<CapClassRef> mSupers;
    private String mRemoteName;

    public boolean isShareable() {
        return mShareable;
    }

    public boolean isRemote() {
        return mRemote;
    }

    public ArrayList<CapClassRef> getSupers() {
        return mSupers;
    }

    public String getRemoteName() {
        return mRemoteName;
    }

    public void read(CapStructureReader reader) throws IOException {
        int bitfield = reader.readU1();
        int flags = (bitfield >> 4) & 0xF;
        int interfaceCount = bitfield & 0xF;
        mShareable = (flags & CapFlag.ACC_SHAREABLE) != 0;
        mRemote = (flags & CapFlag.ACC_REMOTE) != 0;
        ArrayList<CapClassRef> supers = new ArrayList<>();
        for(int i = 0; i < interfaceCount; i++) {
            supers.add(reader.readClassRef());
        }
        if(mRemote) {
            mRemoteName = reader.readString();
        }
        LOG.trace("interface " + (mShareable?"shareable ":"") + (mRemote?"remoteName " + mRemoteName:""));
    }

}
