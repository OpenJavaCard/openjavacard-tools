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
import org.openjavacard.cap.structure.CapPackageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class CapImportComponent extends CapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CapImportComponent.class);

    private ArrayList<CapPackageInfo> mImports;

    public CapImportComponent() {
        super(CapComponentType.Import);
    }

    public ArrayList<CapPackageInfo> getImports() {
        return mImports;
    }

    @Override
    public void read(CapStructureReader reader) throws IOException {
        int importCount = reader.readU1();
        LOG.trace("reading " + importCount + " imports");
        ArrayList<CapPackageInfo> imports = new ArrayList<>();
        for(int i = 0; i < importCount; i++) {
            imports.add(reader.readStructure(CapPackageInfo.class));
        }
        mImports = imports;
    }

}
