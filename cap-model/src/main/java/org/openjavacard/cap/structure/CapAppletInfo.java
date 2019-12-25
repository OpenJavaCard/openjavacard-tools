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

/**
 * Applet component: description of an installable applet
 */
public class CapAppletInfo extends CapStructure {

    private static final Logger LOG = LoggerFactory.getLogger(CapAppletInfo.class);

    /** AID of the applet */
    private AID mAID;

    private int mInstallMethodOffset;

    public AID getAID() {
        return mAID;
    }

    public int getInstallMethodOffset() {
        return mInstallMethodOffset;
    }

    public void read(CapStructureReader reader) throws IOException {
        mAID = reader.readAID();
        mInstallMethodOffset = reader.readU2();
        LOG.trace("applet aid " + mAID + " installMethodOffset " + mInstallMethodOffset);
    }

}
