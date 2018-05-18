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

import org.openjavacard.cap.base.CapComponent;
import org.openjavacard.cap.base.CapStructureReader;
import org.openjavacard.cap.file.CapComponentType;
import org.openjavacard.cap.structure.CapPackageInfo;
import org.openjavacard.cap.structure.CapVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CapHeaderComponent extends CapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CapHeaderComponent.class);

    private static final long MAGIC = 0xDECAFFED;

    private static final int FLAG_ACC_INT = 0x01;
    private static final int FLAG_ACC_EXPORT = 0x02;
    private static final int FLAG_ACC_APPLET = 0x04;

    private CapVersion mCapVersion;
    private int mFlags;
    private CapPackageInfo mInfo;
    private String mName;

    public CapHeaderComponent() {
        super(CapComponentType.Header);
    }

    public CapVersion getCapVersion() {
        return mCapVersion;
    }

    public int getFlags() {
        return mFlags;
    }

    public boolean isIntRequired() {
        return (mFlags & FLAG_ACC_INT) != 0;
    }

    public boolean isExportPresent() {
        return (mFlags & FLAG_ACC_EXPORT) != 0;
    }

    public boolean isAppletPresent() {
        return (mFlags & FLAG_ACC_APPLET) != 0;
    }

    public CapPackageInfo getInfo() {
        return mInfo;
    }

    public String getName() {
        return mName;
    }

    public void read(CapStructureReader reader) throws IOException {
        // check header magic
        long magic = reader.readU4();
        if(magic != MAGIC) {
            reader.error("Missing magic");
        }
        // check and remember CAP version
        int minorVersion = reader.readU1();
        int majorVersion = reader.readU1();
        mCapVersion = new CapVersion(majorVersion, minorVersion);
        // package flags
        mFlags = reader.readU1();
        // package info
        mInfo = reader.readStructure(CapPackageInfo.class);
        if(!reader.hasMore()) {
            return;
        }
        // package name
        mName = reader.readString();
    }

}
