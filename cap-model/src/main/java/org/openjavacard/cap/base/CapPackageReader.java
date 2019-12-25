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

package org.openjavacard.cap.base;

import org.openjavacard.cap.component.CapAppletComponent;
import org.openjavacard.cap.component.CapClassComponent;
import org.openjavacard.cap.component.CapConstantPoolComponent;
import org.openjavacard.cap.component.CapDescriptorComponent;
import org.openjavacard.cap.component.CapDirectoryComponent;
import org.openjavacard.cap.component.CapExportComponent;
import org.openjavacard.cap.component.CapHeaderComponent;
import org.openjavacard.cap.component.CapImportComponent;
import org.openjavacard.cap.component.CapMethodComponent;
import org.openjavacard.cap.component.CapReferenceLocationComponent;
import org.openjavacard.cap.component.CapStaticFieldComponent;
import org.openjavacard.cap.file.CapComponentType;
import org.openjavacard.cap.file.CapFileComponent;
import org.openjavacard.cap.file.CapFilePackage;
import org.openjavacard.cap.structure.CapClassRef;
import org.openjavacard.cap.structure.CapMethodRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapPackageReader {

    private static final Logger LOG = LoggerFactory.getLogger(CapPackageReader.class);

    private final ArrayList<CapClassRef> mClassRefs;
    private final ArrayList<CapMethodRef> mMethodRefs;

    public CapPackageReader() {
        mClassRefs = new ArrayList<>();
        mMethodRefs = new ArrayList<>();
    }

    public void registerClassRef(CapClassRef ref) {
        mClassRefs.add(ref);
    }

    public void registerMethodRef(CapMethodRef ref) {
        mMethodRefs.add(ref);
    }

    public void resolveClassRefs() {
        LOG.trace("resolving " + mClassRefs.size() + " class references");
    }

    public void resolveMethodRefs() {
        LOG.trace("resolving " + mMethodRefs.size() + " method references");
    }

    public CapPackage read(CapFilePackage filePackage) throws IOException {
        CapPackage pkg = new CapPackage();
        List<CapFileComponent> fileComponents = filePackage.getLoadComponents();
        for(CapFileComponent fileComponent: fileComponents) {
            LOG.debug("reading component " + fileComponent.getType());
            CapComponentReader reader = new CapComponentReader(this, fileComponent);
            CapComponentType type = fileComponent.getType();
            int size = reader.readComponentHeader();
            if(pkg.mDirectory != null) {
                if(size != pkg.mDirectory.getComponentSize(type)) {
                    throw new IOException("Component size disagrees with directory");
                }
            }
            switch (type) {
                case Header:
                    pkg.mHeader = reader.readStructure(CapHeaderComponent.class);
                    pkg.mComponents.add(pkg.mHeader);
                    break;
                case Directory:
                    pkg.mDirectory = reader.readStructure(CapDirectoryComponent.class);
                    pkg.mComponents.add(pkg.mDirectory);
                    break;
                case Import:
                    pkg.mImports = reader.readStructure(CapImportComponent.class);
                    pkg.mComponents.add(pkg.mImports);
                    break;
                case Applet:
                    pkg.mApplets = reader.readStructure(CapAppletComponent.class);
                    pkg.mComponents.add(pkg.mApplets);
                    break;
                case Class:
                    pkg.mClasses = reader.readStructure(CapClassComponent.class);
                    pkg.mComponents.add(pkg.mClasses);
                    break;
                case Method:
                    pkg.mMethods = reader.readStructure(CapMethodComponent.class);
                    pkg.mComponents.add(pkg.mMethods);
                    break;
                case StaticField:
                    pkg.mStaticFields = reader.readStructure(CapStaticFieldComponent.class);
                    pkg.mComponents.add(pkg.mStaticFields);
                    break;
                case Export:
                    pkg.mExports = reader.readStructure(CapExportComponent.class);
                    pkg.mComponents.add(pkg.mExports);
                    break;
                case ConstantPool:
                    pkg.mConstantPool = reader.readStructure(CapConstantPoolComponent.class);
                    pkg.mComponents.add(pkg.mConstantPool);
                    break;
                case ReferenceLocation:
                    pkg.mReferenceLocation = reader.readStructure(CapReferenceLocationComponent.class);
                    pkg.mComponents.add(pkg.mReferenceLocation);
                    break;
                case Descriptor:
                    pkg.mDescriptor = reader.readStructure(CapDescriptorComponent.class);
                    pkg.mComponents.add(pkg.mDescriptor);
                    // parse methods in the method component now
                    // that we have the required descriptor data
                    pkg.mMethods.decodeMethodInfo(pkg.mDescriptor);
                    break;
                default:
                    LOG.warn("ignoring component " + type);
                    break;
            }
            if(reader.hasMore()) {
                throw new IOException("Trailing data in component " + type);
            }
        }
        return pkg;
    }

}
