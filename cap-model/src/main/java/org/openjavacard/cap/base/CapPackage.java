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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openjavacard.cap.component.*;

import java.util.ArrayList;

public class CapPackage {

    ArrayList<CapComponent> mComponents;

    CapHeaderComponent mHeader;
    CapDirectoryComponent mDirectory;
    CapImportComponent mImports;
    CapAppletComponent mApplets;
    CapClassComponent mClasses;
    CapMethodComponent mMethods;
    CapStaticFieldComponent mStaticFields;
    CapExportComponent mExports;
    CapConstantPoolComponent mConstantPool;
    CapReferenceLocationComponent mReferenceLocation;
    CapDescriptorComponent mDescriptor;

    CapPackage() {
        mComponents = new ArrayList<>();
    }

    @JsonIgnore
    public ArrayList<CapComponent> getComponents() {
        return mComponents;
    }

    public CapHeaderComponent getHeader() {
        return mHeader;
    }

    public CapDirectoryComponent getDirectory() {
        return mDirectory;
    }

    public CapImportComponent getImports() {
        return mImports;
    }

    public CapAppletComponent getApplets() {
        return mApplets;
    }

    public CapClassComponent getClasses() {
        return mClasses;
    }

    public CapMethodComponent getMethods() {
        return mMethods;
    }

    public CapStaticFieldComponent getStaticFields() {
        return mStaticFields;
    }

    public CapExportComponent getExports() {
        return mExports;
    }

    public CapConstantPoolComponent getConstantPool() {
        return mConstantPool;
    }

    public CapReferenceLocationComponent getReferenceLocation() {
        return mReferenceLocation;
    }

    public CapDescriptorComponent getDescriptor() {
        return mDescriptor;
    }

}
