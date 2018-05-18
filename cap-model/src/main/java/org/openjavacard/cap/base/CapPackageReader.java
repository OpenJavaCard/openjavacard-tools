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

import org.openjavacard.cap.component.*;
import org.openjavacard.cap.file.CapComponentType;
import org.openjavacard.cap.file.CapFileComponent;
import org.openjavacard.cap.file.CapFilePackage;
import org.openjavacard.cap.structure.CapClassRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapPackageReader {

    private static final Logger LOG = LoggerFactory.getLogger(CapPackageReader.class);

    private ArrayList<CapComponent> mComponents;

    private CapHeaderComponent mHeader;
    private CapDirectoryComponent mDirectory;
    private CapImportComponent mImports;
    private CapAppletComponent mApplets;
    private CapClassComponent mClasses;
    private CapMethodComponent mMethods;
    private CapStaticFieldComponent mStaticFields;
    private CapExportComponent mExports;
    private CapConstantPoolComponent mConstantPool;
    private CapReferenceLocationComponent mReferenceLocation;

    private CapDescriptorComponent mDescriptor;

    private final ArrayList<CapClassRef> mClassRefs;

    public CapPackageReader() {
        mComponents = new ArrayList<>();
        mClassRefs = new ArrayList<>();
    }

    public List<CapComponent> getComponents() {
        return new ArrayList<>(mComponents);
    }

    public void read(CapFilePackage filePackage) throws IOException {
        List<CapFileComponent> fileComponents = filePackage.getLoadComponents();
        for(CapFileComponent fileComponent: fileComponents) {
            LOG.debug("reading component " + fileComponent.getType());
            CapComponentReader reader = new CapComponentReader(this, fileComponent);
            CapComponentType type = fileComponent.getType();
            int size = reader.readComponentHeader();
            if(mDirectory != null) {
                if(size != mDirectory.getComponentSize(type)) {
                    throw new IOException("Component size disagrees with directory");
                }
            }
            switch (type) {
                case Header:
                    mHeader = reader.readStructure(CapHeaderComponent.class);
                    mComponents.add(mHeader);
                    break;
                case Directory:
                    mDirectory = reader.readStructure(CapDirectoryComponent.class);
                    mComponents.add(mDirectory);
                    break;
                case Import:
                    mImports = reader.readStructure(CapImportComponent.class);
                    mComponents.add(mImports);
                    break;
                case Applet:
                    mApplets = reader.readStructure(CapAppletComponent.class);
                    mComponents.add(mApplets);
                    break;
                case Class:
                    mClasses = reader.readStructure(CapClassComponent.class);
                    mComponents.add(mClasses);
                    break;
                case Method:
                    mMethods = reader.readStructure(CapMethodComponent.class);
                    mComponents.add(mMethods);
                    break;
                case StaticField:
                    mStaticFields = reader.readStructure(CapStaticFieldComponent.class);
                    mComponents.add(mStaticFields);
                    break;
                case Export:
                    mExports = reader.readStructure(CapExportComponent.class);
                    mComponents.add(mExports);
                    break;
                case ConstantPool:
                    mConstantPool = reader.readStructure(CapConstantPoolComponent.class);
                    mComponents.add(mConstantPool);
                    break;
                case ReferenceLocation:
                    mReferenceLocation = reader.readStructure(CapReferenceLocationComponent.class);
                    mComponents.add(mReferenceLocation);
                    break;
                case Descriptor:
                    mDescriptor = reader.readStructure(CapDescriptorComponent.class);
                    mComponents.add(mDescriptor);
                    // parse methods in the method component now
                    // that we have the required descriptor data
                    mMethods.decodeMethodInfo(mDescriptor);
                    break;
                default:
                    LOG.warn("ignoring component " + type);
                    break;
            }
            if(reader.hasMore()) {
                throw new IOException("Trailing data in component " + type);
            }
        }
    }

}
