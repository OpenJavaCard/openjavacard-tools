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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CapDirectoryComponent extends CapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CapDirectoryComponent.class);

    private int[] mComponentSizes;

    private int mImageSize;
    private int mArrayInitCount;
    private int mArrayInitSize;
    private int mImportCount;
    private int mAppletCount;
    private int mCustomCount;

    public CapDirectoryComponent() {
        super(CapComponentType.Directory);
    }

    public int[] getComponentSizes() {
        return mComponentSizes;
    }

    public int getComponentSize(int tag) {
        return mComponentSizes[tag - 1];
    }

    public int getComponentSize(CapComponentType type) {
        return mComponentSizes[type.tag() - 1];
    }

    public int getImageSize() {
        return mImageSize;
    }

    public int getArrayInitCount() {
        return mArrayInitCount;
    }

    public int getArrayInitSize() {
        return mArrayInitSize;
    }

    public int getImportCount() {
        return mImportCount;
    }

    public int getAppletCount() {
        return mAppletCount;
    }

    public int getCustomCount() {
        return mCustomCount;
    }

    public void read(CapStructureReader reader) throws IOException {
        mComponentSizes = reader.readU2Array(12);
        for(int i = 0; i < 12; i++) {
            LOG.trace("component " + i + " size " + mComponentSizes[i]);
        }
        mImageSize = reader.readU2();
        LOG.trace("image size " + mImageSize);
        mArrayInitCount = reader.readU2();
        mArrayInitSize = reader.readU2();
        LOG.trace("array count " + mArrayInitCount + " size " + mArrayInitSize);
        mImportCount = reader.readU1();
        LOG.trace("imports " + mImportCount);
        if(!reader.hasMore()) {
            return;
        }
        mAppletCount = reader.readU1();
        LOG.trace("applets " + mAppletCount);
        if(!reader.hasMore()) {
            return;
        }
        mCustomCount = reader.readU1();
        LOG.trace("custom " + mCustomCount);
        if(mCustomCount != 0) {
            throw new IOException("Custom CAP components are not supported");
        }
    }

}
