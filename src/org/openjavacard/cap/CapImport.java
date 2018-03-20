/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.cap;

import org.openjavacard.iso.AID;

public class CapImport {

    private static final String ATTR_VERSION = "Version";
    private static final String ATTR_AID = "AID";

    AID mAID;
    String mVersion;

    CapImport() {
    }

    public AID getAID() {
        return mAID;
    }

    public String getVersion() {
        return mVersion;
    }

    void readAttribute(String name, String value) {
        if (name.equals(ATTR_VERSION)) {
            mVersion = value;
        }
        if (name.equals(ATTR_AID)) {
            mAID = AID.fromArrayString(value);
        }
    }

    @Override
    public String toString() {
        return "CapImport " + mAID;
    }
}
