package better.smartcard.cap;

import java.util.HashMap;
import java.util.Map;

public enum CapComponentType {

    Header(1, "Header.cap"),
    Directory(2, "Directory.cap"),
    Applet(3, "Applet.cap"),
    Import(4, "Import.cap"),
    ConstantPool(5, "ConstantPool.cap"),
    Class(6, "Class.cap"),
    Method(7, "Method.cap"),
    StaticField(8, "StaticField.cap"),
    ReferenceLocation(9, "RefLocation.cap"),
    Export(10, "Export.cap"),
    Descriptor(11, "Descriptor.cap"),
    Debug(12, "Debug.cap");

    /**
     * Proper load order according to the JCVM specification
     */
    public static final CapComponentType[] LOAD_ORDER = {
            Header,
            Directory,
            Import,
            Applet,
            Class,
            Method,
            StaticField,
            Export,
            ConstantPool,
            ReferenceLocation,
            Descriptor
    };

    public static CapComponentType forFilename(String name) {
        return BY_FILENAME.get(name);
    }

    private static final Map<String, CapComponentType> BY_FILENAME;

    static {
        HashMap<String, CapComponentType> table = new HashMap<>();
        for(CapComponentType type: CapComponentType.values()) {
            table.put(type.filename, type);
        }
        BY_FILENAME = table;
    }

    private final int tag;

    private final String filename;

    CapComponentType(int tag, String filename) {
        this.tag = tag;
        this.filename = filename;
    }

    public int tag() {
        return this.tag;
    }

    public String filename() {
        return this.filename;
    }

}
