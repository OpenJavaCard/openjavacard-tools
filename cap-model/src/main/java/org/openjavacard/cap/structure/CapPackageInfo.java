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
import org.openjavacard.iso.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CapPackageInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapPackageInfo.class);

    private int mMinorVersion;
    private int mMajorVersion;

    private AID mAID;

    public int getMinorVersion() {
        return mMinorVersion;
    }

    public int getMajorVersion() {
        return mMajorVersion;
    }

    public AID getAID() {
        return mAID;
    }

    public void read(CapStructureReader reader) throws IOException {
        mMinorVersion = reader.readU1();
        mMajorVersion = reader.readU1();
        mAID = reader.readAID();
        LOG.trace("package " + mAID + " version " + mMajorVersion + "." + mMinorVersion);
    }

}
