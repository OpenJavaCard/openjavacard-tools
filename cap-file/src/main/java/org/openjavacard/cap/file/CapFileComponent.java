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

/**
 * Component in a CAP file
 */
public class CapFileComponent {

    private final CapComponentType mType;

    private final byte[] mData;

    CapFileComponent(CapComponentType type, byte[] data) {
        mType = type;
        mData = data;
    }

    public String getName() {
        return mType.name();
    }

    public String getFilename() {
        return mType.filename();
    }

    public CapComponentType getType() {
        return mType;
    }

    public byte[] getData() {
        return mData;
    }

    public int getSize() {
        return mData.length;
    }

    public String toString() {
        return "CapFileComponent " + mType;
    }

}
