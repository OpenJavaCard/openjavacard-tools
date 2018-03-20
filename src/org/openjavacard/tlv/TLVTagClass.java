package org.openjavacard.tlv;

import java.util.HashMap;

public enum  TLVTagClass {
    UNIVERSAL(TLVTag.TAG_CLASS_UNIVERSAL),
    APPLICATION(TLVTag.TAG_CLASS_APPLICATION),
    CONTEXT(TLVTag.TAG_CLASS_CONTEXT),
    PRIVATE(TLVTag.TAG_CLASS_PRIVATE);

    private static final HashMap<Integer,TLVTagClass> CLASSES
            = buildClasses();

    final int value;

    TLVTagClass(int value) {
        this.value = value;
    }

    public static TLVTagClass forValue(int classValue) {
        return CLASSES.get(classValue);
    }

    private static HashMap<Integer,TLVTagClass> buildClasses() {
        HashMap<Integer, TLVTagClass> res = new HashMap<>();
        for(TLVTagClass tc: TLVTagClass.values()) {
            res.put(tc.value, tc);
        }
        return res;
    }

}
