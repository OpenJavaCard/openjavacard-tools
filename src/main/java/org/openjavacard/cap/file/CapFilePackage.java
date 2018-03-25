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

package org.openjavacard.cap.file;

import org.openjavacard.gp.client.GPLoadFile;
import org.openjavacard.iso.AID;
import org.openjavacard.tlv.TLVLength;
import org.openjavacard.tlv.TLVTag;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.VerboseString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.jar.Attributes;

/**
 * Package in a CAP file
 * <p/>
 * This contains the components of the package, which can be used to generate a load file.
 */
public class CapFilePackage implements VerboseString {

    private static final Logger LOG = LoggerFactory.getLogger(CapFilePackage.class);

    private static final String JC = "Java-Card";

    private static final String ATTR_JC_CAP_FILE_VERSION = JC + "-CAP-File-Version";
    private static final String ATTR_JC_CAP_CREATION_TIME = JC + "-CAP-Creation-Time";
    private static final String ATTR_JC_CONVERTER_PROVIDER = JC + "-Converter-Provider";
    private static final String ATTR_JC_CONVERTER_VERSION = JC + "-Converter-Version";
    private static final String ATTR_JC_PACKAGE_NAME = JC + "-Package-Name";
    private static final String ATTR_JC_PACKAGE_VERSION = JC + "-Package-Version";
    private static final String ATTR_JC_PACKAGE_AID = JC + "-Package-AID";
    private static final String ATTR_JC_INT_SUPPORT_REQUIRED = JC + "-Integer-Support-Required";

    private static final String ATTR_JC_APPLET_PREFIX = JC + "-Applet-";
    private static final String ATTR_JC_IMPORT_PREFIX = JC + "-Imported-Package-";

    private String mName;

    private Attributes mAttributes;

    private String mCapFileVersion;
    private String mCapCreationTime;

    private String mConverterProvider;
    private String mConverterVersion;

    private String mPackageName;
    private String mPackageVersion;
    private AID mPackageAID;

    private boolean mIntSupportRequired;

    private Vector<CapFileApplet> mApplets = new Vector<>();
    private Vector<CapFileImport> mImports = new Vector<>();

    private Vector<CapFileComponent> mComponents = new Vector<>();
    private HashMap<CapComponentType, CapFileComponent> mComponentsByType = new HashMap<>();

    CapFilePackage() {
    }

    public String getName() {
        return mName;
    }

    public Attributes getAttributes() {
        return mAttributes;
    }

    public String getCapFileVersion() {
        return mCapFileVersion;
    }

    public String getCapCreationTime() {
        return mCapCreationTime;
    }

    public String getConverterProvider() {
        return mConverterProvider;
    }

    public String getConverterVersion() {
        return mConverterVersion;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getPackageVersion() {
        return mPackageVersion;
    }

    public AID getPackageAID() {
        return mPackageAID;
    }

    public boolean isIntSupportRequired() {
        return mIntSupportRequired;
    }

    public List<CapFileImport> getImports() {
        return mImports;
    }

    public List<CapFileApplet> getApplets() {
        return mApplets;
    }

    public List<CapFileComponent> getComponents() {
        return mComponents;
    }

    public CapFileComponent getComponentByType(CapComponentType type) {
        return mComponentsByType.get(type);
    }

    public List<CapFileComponent> getLoadComponents() {
        ArrayList<CapFileComponent> res = new ArrayList<>();
        for(CapComponentType type: CapComponentType.LOAD_ORDER) {
            CapFileComponent component = getComponentByType(type);
            if(component != null) {
                res.add(component);
            }
        }
        return res;
    }

    public String toString() {
        return "CAPFilePackage " + mPackageAID + " " + mPackageVersion + "(" + mPackageName + ")";
    }

    @Override
    public String toVerboseString() {
        String sb = "CAP package " + mPackageName + ":" +
                "\n  File version: " + mCapFileVersion +
                "\n  File created: " + mCapCreationTime +
                "\n  Converter provider: " + mConverterProvider +
                "\n  Converter version: " + mConverterVersion +
                "\n  Package version: " + mPackageVersion +
                "\n  Package AID: " + mPackageAID +
                "\n";
        return sb;
    }

    /**
     * Generate a combined load file
     * @param blockSize for the file
     * @return a GPLoadFile
     */
    public GPLoadFile generateCombinedLoadFile(int blockSize) {
        GPLoadFile res = new GPLoadFile(getPackageAID());
        try {
            // need to know total length
            int totalSize = 0;
            // need to know components to emit
            List<CapFileComponent> components = new ArrayList<>();

            // find components in load order
            for (CapComponentType type : CapComponentType.LOAD_ORDER) {
                CapFileComponent component = getComponentByType(type);
                // if we have a component of the given type
                if (component != null) {
                    byte[] data = component.getData();
                    // add up the total size
                    totalSize += data.length;
                    // remember the components
                    components.add(component);
                }
            }

            // emit one tag with all the components
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(TLVTag.tagBytes(0xC400));
            bos.write(TLVLength.lengthBytes(totalSize));
            for(CapFileComponent component : components) {
                byte[] data = component.getData();
                bos.write(data);
            }

            // get ourselves an array with all the data
            byte[] raw = bos.toByteArray();

            // split the result into appropriate blocks
            byte[][] blocks = ArrayUtil.splitBlocks(raw, blockSize);

            // add the blocks to the load file
            for(byte[] block: blocks) {
                res.addBlock(block);
            }
        } catch (IOException e) {
            throw new Error("Error generating load file", e);
        }
        return res;
    }

    /**
     * Internal: parse a CAP package
     * @param componentName
     * @param attributes
     * @param files
     */
    void read(String componentName, Attributes attributes, Map<String, byte[]> files) {
        mName = componentName;
        mAttributes = attributes;
        // process attributes
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            String name = entry.getKey().toString();
            String value = (String) entry.getValue();
            LOG.trace("attribute " + name + " = " + value);
            // simple properties
            if (name.equals(ATTR_JC_CAP_FILE_VERSION)) {
                mCapFileVersion = value;
            }
            if (name.equals(ATTR_JC_CAP_CREATION_TIME)) {
                mCapCreationTime = value;
            }
            if (name.equals(ATTR_JC_CONVERTER_PROVIDER)) {
                mConverterProvider = value;
            }
            if (name.equals(ATTR_JC_CONVERTER_VERSION)) {
                mConverterVersion = value;
            }
            if (name.equals(ATTR_JC_PACKAGE_NAME)) {
                mPackageName = value;
            }
            if (name.equals(ATTR_JC_PACKAGE_VERSION)) {
                mPackageVersion = value;
            }
            if (name.equals(ATTR_JC_PACKAGE_AID)) {
                mPackageAID = AID.fromArrayString(value);
            }
            if (name.equals(ATTR_JC_INT_SUPPORT_REQUIRED)) {
                mIntSupportRequired = value.equals("TRUE");
            }
            // applets
            if (name.startsWith(ATTR_JC_APPLET_PREFIX)) {
                String rest = name.substring(ATTR_JC_APPLET_PREFIX.length());
                String[] split = rest.split("-");
                int index = extractAttributeIndex(split);
                String propName = extractAttributeName(split);
                LOG.trace("applet " + index + " attr " + propName + " value " + value);
                CapFileApplet app = getOrCreateApplet(index - 1);
                app.readAttribute(propName, value);
            }
            // imports
            if (name.startsWith(ATTR_JC_IMPORT_PREFIX)) {
                String rest = name.substring(ATTR_JC_IMPORT_PREFIX.length());
                String[] split = rest.split("-");
                int index = extractAttributeIndex(split);
                String propName = extractAttributeName(split);
                LOG.trace("import " + index + " attr " + propName + " value " + value);
                CapFileImport imp = getOrCreateImport(index - 1);
                imp.readAttribute(propName, value);
            }
        }
        // process components
        String jcPkgPrefix = mName + "/javacard/";
        for(Map.Entry<String, byte[]> entry: files.entrySet()) {
            String key = entry.getKey();
            byte[] data = entry.getValue();
            if(key.startsWith(jcPkgPrefix)) {
                String name = key.substring(jcPkgPrefix.length());
                CapComponentType type = CapComponentType.forFilename(name);
                LOG.trace("component " + name);
                if(type != null) {
                    CapFileComponent com = new CapFileComponent(type, data);
                    addComponent(com);
                }
            }
        }
    }

    private void addComponent(CapFileComponent component) {
        mComponents.add(component);
        mComponentsByType.put(component.getType(), component);
    }

    private int extractAttributeIndex(String[] split) {
        String indexStr = split[0];
        return Integer.parseInt(indexStr);
    }

    private String extractAttributeName(String[] split) {
        StringBuilder propBuf = new StringBuilder();
        for (int i = 1; i < split.length; i++) {
            propBuf.append(split[i]);
            if (i < (split.length - 1)) {
                propBuf.append('-');
            }
        }
        return propBuf.toString();
    }

    private CapFileApplet getOrCreateApplet(int index) {
        CapFileApplet res = null;
        if (index < mApplets.size()) {
            res = mApplets.get(index);
        } else {
            mApplets.setSize(index + 1);
        }
        if (res == null) {
            LOG.trace("new applet " + index);
            res = new CapFileApplet();
            mApplets.set(index, res);
        }
        return res;
    }

    private CapFileImport getOrCreateImport(int index) {
        CapFileImport res = null;
        if (index < mImports.size()) {
            res = mImports.get(index);
        } else {
            mImports.setSize(index + 1);
        }
        if (res == null) {
            LOG.trace("new import " + index);
            res = new CapFileImport();
            mImports.set(index, res);
        }
        return res;
    }

}
