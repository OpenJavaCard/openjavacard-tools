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

package org.openjavacard.cap.file;

import org.openjavacard.iso.AID;

/**
 * Applet declaration in a CAP file
 */
public class CapFileApplet {

    private static final String ATTR_NAME = "Name";
    private static final String ATTR_VERSION = "Version";
    private static final String ATTR_AID = "AID";

    private String mName;
    private String mVersion;

    private AID mAID;

    CapFileApplet() {
    }

    public String getName() {
        return mName;
    }

    public String getVersion() {
        return mVersion;
    }

    public AID getAID() {
        return mAID;
    }

    public String toString() {
        return "CapFileApplet " + mAID + " " + mVersion + "(" + mName + ")";
    }

    /**
     * Internal: parse a manifest attribute of the applet
     * @param name
     * @param value
     */
    void readAttribute(String name, String value) {
        if (name.equals(ATTR_NAME)) {
            mName = value;
        }
        if (name.equals(ATTR_VERSION)) {
            mVersion = value;
        }
        if (name.equals(ATTR_AID)) {
            mAID = AID.fromArrayString(value);
        }
    }

}
