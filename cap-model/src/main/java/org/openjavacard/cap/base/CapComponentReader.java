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

package org.openjavacard.cap.base;

import org.openjavacard.cap.file.CapFileComponent;
import org.openjavacard.cap.structure.CapClassRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CapComponentReader extends CapStructureReader {

    private static final Logger LOG = LoggerFactory.getLogger(CapComponentReader.class);

    private final CapPackageReader mPackageReader;
    private final CapFileComponent mFileComponent;

    CapComponentReader(CapPackageReader packageReader, CapFileComponent fileComponent) {
        super(fileComponent.getData());
        mPackageReader = packageReader;
        mFileComponent = fileComponent;
    }

    public int readComponentHeader() throws IOException {
        int tag = readU1();
        int size = readU2();
        if(tag != mFileComponent.getType().tag()) {
            throw new IOException("Bad component type: expected " + mFileComponent.getType().tag() + " got " + tag);
        }
        if(size != mStream.available()) {
            throw new IOException("Invalid component length");
        }
        return size;
    }

    @Override
    public CapClassRef readClassRef() throws IOException {
        CapClassRef ref = super.readClassRef();
        // XXX register with packageReader
        return ref;
    }

}
