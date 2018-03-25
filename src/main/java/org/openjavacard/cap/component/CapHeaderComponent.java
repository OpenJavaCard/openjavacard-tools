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
import org.openjavacard.iso.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CapHeaderComponent extends CapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CapHeaderComponent.class);

    private static final long MAGIC = 0xDECAFFED;

    private static final int FLAG_ACC_INT = 0x01;
    private static final int FLAG_ACC_EXPORT = 0x02;
    private static final int FLAG_ACC_APPLET = 0x04;

    private int mMinorVersion;
    private int mMajorVersion;
    private int mFlags;
    private int mPackageMinorVersion;
    private int mPackageMajorVersion;
    private AID mPackageAID;
    private String mPackageName;

    public CapHeaderComponent() {
        super(CapComponentType.Header);
    }

    public int getMinorVersion() {
        return mMinorVersion;
    }

    public int getMajorVersion() {
        return mMajorVersion;
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

    public int getPackageMinorVersion() {
        return mPackageMinorVersion;
    }

    public int getPackageMajorVersion() {
        return mPackageMajorVersion;
    }

    public AID getPackageAID() {
        return mPackageAID;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void read(CapStructureReader reader) throws IOException {
        long magic = reader.readU4();
        if(magic != MAGIC) {
            reader.error("Missing magic");
        }
        mMinorVersion = reader.readU1();
        mMajorVersion = reader.readU1();
        mFlags = reader.readU1();
        LOG.trace("cap maj " + mMajorVersion + " min " + mMinorVersion + " flag " + mFlags);
        // package info
        mPackageMinorVersion = reader.readU1();
        mPackageMajorVersion = reader.readU1();
        LOG.trace("pkg maj " + mPackageMajorVersion + " min " + mPackageMinorVersion);
        mPackageAID = reader.readAID();
        LOG.trace("pkg aid " + mPackageAID);
        if(!reader.hasMore()) {
            return;
        }
        // package name
        mPackageName = reader.readString();
    }

}
