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
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CapStaticFieldComponent extends CapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CapStaticFieldComponent.class);

    public CapStaticFieldComponent() {
        super(CapComponentType.StaticField);
    }

    @Override
    public void read(CapStructureReader reader) throws IOException {
        int imageSize = reader.readU2();
        LOG.trace("image size " + imageSize);
        int referenceCount = reader.readU2();
        LOG.trace("reference count " + referenceCount);
        int arrayInitCount = reader.readU2();
        LOG.trace("array init count " + arrayInitCount);
        for(int i = 0; i < arrayInitCount; i++) {
            int arrayType = reader.readU1();
            LOG.trace("array " + i + " type " + arrayType);
            int arrayCount = reader.readU2();
            LOG.trace("array " + i + " count " + arrayCount);
            byte[] arrayData = reader.readBytes(arrayCount);
            LOG.trace("array " + i + " data " + HexUtil.bytesToHex(arrayData));
        }
        int defaultValueCount = reader.readU2();
        LOG.trace("default value count " + defaultValueCount);
        int nonDefaultValueCount = reader.readU2();
        LOG.trace("nondefault value count " + imageSize);
        byte[] nonDefaultValues = reader.readBytes(nonDefaultValueCount);
        LOG.trace("nondefault values " + HexUtil.bytesToHex(nonDefaultValues));
    }

}
