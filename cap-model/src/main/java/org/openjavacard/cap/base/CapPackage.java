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
