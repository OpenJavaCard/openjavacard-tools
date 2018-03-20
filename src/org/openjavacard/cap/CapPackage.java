package org.openjavacard.cap;

import org.openjavacard.gp.GPLoadFile;
import org.openjavacard.tlv.TLVUtil;
import org.openjavacard.iso.AID;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.VerboseString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;

public class CapPackage implements VerboseString {

    private static final Logger LOG = LoggerFactory.getLogger(CapPackage.class);

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

    String mName;

    Attributes mAttributes;

    String mCapFileVersion;
    String mCapCreationTime;

    String mConverterProvider;
    String mConverterVersion;

    String mPackageName;
    String mPackageVersion;
    AID mPackageAID;

    boolean mIntSupportRequired;

    Vector<CapApplet> mApplets = new Vector<>();
    Vector<CapImport> mImports = new Vector<>();

    Vector<CapComponent> mComponents = new Vector<>();
    HashMap<CapComponentType, CapComponent> mComponentsByType = new HashMap<>();

    CapPackage() {
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

    public List<CapImport> getImports() {
        return mImports;
    }

    public List<CapApplet> getApplets() {
        return mApplets;
    }

    public List<CapComponent> getComponents() {
        return mComponents;
    }

    public CapComponent getComponentByType(CapComponentType type) {
        return mComponentsByType.get(type);
    }

    public List<CapComponent> getLoadComponents() {
        ArrayList<CapComponent> res = new ArrayList<>();
        for(CapComponentType type: CapComponentType.LOAD_ORDER) {
            CapComponent component = getComponentByType(type);
            if(component != null) {
                res.add(component);
            }
        }
        return res;
    }

    public void read(String componentName, Attributes attributes, Map<String, byte[]> files) {
        mName = componentName;
        mAttributes = attributes;
        // process attributes
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            String name = entry.getKey().toString();
            String value = (String) entry.getValue();
            LOG.trace("attribute " + name + " = " + value);
            // simple properties
            if (name == ATTR_JC_CAP_FILE_VERSION) {
                mCapFileVersion = value;
            }
            if (name == ATTR_JC_CAP_CREATION_TIME) {
                mCapCreationTime = value;
            }
            if (name == ATTR_JC_CONVERTER_PROVIDER) {
                mConverterProvider = value;
            }
            if (name == ATTR_JC_CONVERTER_VERSION) {
                mConverterVersion = value;
            }
            if (name == ATTR_JC_PACKAGE_NAME) {
                mPackageName = value;
            }
            if (name == ATTR_JC_PACKAGE_VERSION) {
                mPackageVersion = value;
            }
            if (name == ATTR_JC_PACKAGE_AID) {
                mPackageAID = AID.fromArrayString(value);
            }
            if (name == ATTR_JC_INT_SUPPORT_REQUIRED) {
                mIntSupportRequired = value.equals("TRUE");
            }
            // applets
            if (name.startsWith(ATTR_JC_APPLET_PREFIX)) {
                String rest = name.substring(ATTR_JC_APPLET_PREFIX.length());
                String[] split = rest.split("-");
                int index = extractAttributeIndex(split);
                String propName = extractAttributeName(split);
                LOG.trace("applet " + index + " attr " + propName + " value " + value);
                CapApplet app = getOrCreateApplet(index - 1);
                app.readAttribute(propName, value);
            }
            // imports
            if (name.startsWith(ATTR_JC_IMPORT_PREFIX)) {
                String rest = name.substring(ATTR_JC_IMPORT_PREFIX.length());
                String[] split = rest.split("-");
                int index = extractAttributeIndex(split);
                String propName = extractAttributeName(split);
                LOG.trace("import " + index + " attr " + propName + " value " + value);
                CapImport imp = getOrCreateImport(index - 1);
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
                    CapComponent com = new CapComponent(type, data);
                    addComponent(com);
                }
            }
        }
    }

    public GPLoadFile generateCombinedLoadFile(int blockSize) {
        GPLoadFile res = new GPLoadFile(getPackageAID());
        try {
            // need to know total length
            int totalSize = 0;
            // need to know components to emit
            List<CapComponent> components = new ArrayList<>();

            // find components in load order
            for (CapComponentType type : CapComponentType.LOAD_ORDER) {
                CapComponent component = getComponentByType(type);
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
            bos.write(TLVUtil.convertTag(0xC4));
            bos.write(TLVUtil.convertLength(totalSize));
            for(CapComponent component : components) {
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
            throw new Error("Error writing load file", e);
        }
        return res;
    }

    private void addComponent(CapComponent component) {
        mComponents.add(component);
        mComponentsByType.put(component.getType(), component);
    }

    private int extractAttributeIndex(String[] split) {
        String indexStr = split[0];
        return Integer.parseInt(indexStr);
    }

    private String extractAttributeName(String[] split) {
        StringBuffer propBuf = new StringBuffer();
        for (int i = 1; i < split.length; i++) {
            propBuf.append(split[i]);
            if (i < (split.length - 1)) {
                propBuf.append('-');
            }
        }
        return propBuf.toString();
    }

    private CapApplet getOrCreateApplet(int index) {
        CapApplet res = null;
        if (index < mApplets.size()) {
            res = mApplets.get(index);
        } else {
            mApplets.setSize(index + 1);
        }
        if (res == null) {
            LOG.trace("new applet " + index);
            res = new CapApplet();
            mApplets.set(index, res);
        }
        return res;
    }

    private CapImport getOrCreateImport(int index) {
        CapImport res = null;
        if (index < mImports.size()) {
            res = mImports.get(index);
        } else {
            mImports.setSize(index + 1);
        }
        if (res == null) {
            LOG.trace("new import " + index);
            res = new CapImport();
            mImports.set(index, res);
        }
        return res;
    }

    public String toString() {
        return "CAP Package " + mPackageName;
    }

    @Override
    public String toVerboseString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CAP package" + mPackageName + ":\n");
        sb.append("  File version: " + mCapFileVersion + "\n");
        sb.append("  File created: " + mCapCreationTime + "\n");
        sb.append("  Converter provider: " + mConverterProvider + "\n");
        sb.append("  Converter version: " + mConverterVersion + "\n");
        sb.append("  Package version: " + mPackageVersion + "\n");
        sb.append("  Package AID: " + mPackageAID + "\n");
        return sb.toString();
    }

}
