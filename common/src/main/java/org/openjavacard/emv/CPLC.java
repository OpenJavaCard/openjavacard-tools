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

package org.openjavacard.emv;

import org.openjavacard.tlv.TLVException;
import org.openjavacard.tlv.TLVPrimitive;
import org.openjavacard.util.HexUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Card Production Life Cycle data
 * <p/>
 * This describes the card and how it has been treated in various phases of production and provisioning.
 * <p/>
 * Many cards intended for EMV use provide this data structure, and it is considered well-known.
 * <p/>
 * Supposedly defined in the proprietary "Visa Integrated Circuit Card Specification".
 * <p/>
 */
public class CPLC {

    public static final byte[] IC_FABRICATOR_NXP = HexUtil.hexToBytes("4790");
    public static final byte[] IC_FABRICATOR_INFINEON = HexUtil.hexToBytes("4090");
    public static final byte[] IC_FABRICATOR_SAMSUNG = HexUtil.hexToBytes("4250");
    public static final byte[] IC_FABRICATOR_RENESAS = HexUtil.hexToBytes("3060");
    public static final byte[] IC_FABRICATOR_ATMEL = HexUtil.hexToBytes("4180");

    /**
     * Definition of fields in a CPLC
     */
    public enum Field {
        ICFabricator(2),
        ICType(2),
        OperatingSystemID(2),
        OperatingSystemReleaseDate(2),
        OperatingSystemReleaseLevel(2),
        ICFabricationDate(2),
        ICSerialNumber(4),
        ICBatchIdentifier(2),
        ICModuleFabricator(2),
        ICModulePackagingDate(2),
        ICCManufacturer(2),
        ICEmbeddingDate(2),
        ICPrePersonalizer(2),
        ICPrePersonalizationEquipmentDate(2),
        ICPrePersonalizationEquipmentID(4),
        ICPersonalizer(2),
        ICPersonalizationDate(2),
        ICPersonalizationEquipmentID(4);

        /** Length of the field in bytes */
        public final int fieldLength;

        /** Main constructor */
        Field(int length) {
            this.fieldLength = length;
        }
    }

    /** HashMap containing field values */
    private final LinkedHashMap<Field, byte[]> mValues;

    /**
     * Constructor for empty life cycle data objects
     */
    public CPLC() {
        mValues = new LinkedHashMap<>();
    }

    /**
     * Constructor for life cycle data objects
     * @param values to populate the object with
     */
    public CPLC(Map<Field, byte[]> values) {
        LinkedHashMap<Field,byte[]> valueMap = new LinkedHashMap<>();
        for(Field field: Field.values()) {
            valueMap.put(field, values.get(field));
        }
        mValues = valueMap;
    }

    /** @return list of fields present in this object */
    public List<Field> getFields() {
        return new ArrayList<>(mValues.keySet());
    }

    /** Get the value of a field in binary form */
    public byte[] getFieldValue(Field field) {
        return mValues.get(field).clone();
    }

    /** Get the value of a field in hexadecimal string form */
    public String getFieldHex(Field field) {
        byte[] value = getFieldValue(field);
        if(value == null) {
            return null;
        }
        return HexUtil.bytesToHex(value);
    }

    /**
     * Get the lifetime identifier for the card
     * <p/>
     * The lifetime identifier tries to capture card identity
     * based on CPLC information about the chip, including
     * the fabricator, type, batch ID and serial number.
     * <p/>
     * @return the lifetime identifier
     */
    public String getLifetimeIdentifier() {
        return "card"
                + "-f" + getFieldHex(Field.ICFabricator)
                + "-t" + getFieldHex(Field.ICType)
                + "-b" + getFieldHex(Field.ICBatchIdentifier)
                + "-s" + getFieldHex(Field.ICSerialNumber);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Card Production Life Cycle:");
        for (Field field : Field.values()) {
            byte[] value = mValues.get(field);
            if (value != null) {
                sb.append("\n  " + field + ": " + HexUtil.bytesToHex(value));
            }
        }
        return sb.toString();
    }

    /**
     * Parse a CPLC from bytes
     * @param buf with data
     */
    public static CPLC read(byte[] buf) {
        return read(buf, 0, buf.length);
    }

    /**
     * Parse a CPLC from bytes
     * @param buf containing data
     * @param off of data
     * @param len of data
     */
    public static CPLC read(byte[] buf, int off, int len) throws IllegalArgumentException {
        try {
            TLVPrimitive tlv = TLVPrimitive.readPrimitive(buf, off, len).asPrimitive(0x9F7F);
            LinkedHashMap<Field, byte[]> values = new LinkedHashMap<>();
            byte[] dataBuf = tlv.getValueBytes();
            int dataOff = 0;
            for (Field field : Field.values()) {
                int fieldLen = field.fieldLength;

                if (dataOff >= dataBuf.length) {
                    throw new IllegalArgumentException("CPLC to short");
                }

                values.put(field, Arrays.copyOfRange(dataBuf, dataOff, dataOff + fieldLen));

                dataOff += fieldLen;
            }
            if(dataOff != dataBuf.length) {
                throw new IllegalArgumentException("CPLC to long (" + (dataBuf.length - dataOff) + " bytes left)");
            }
            // construct and return instance
            return new CPLC(values);
        } catch (TLVException e) {
            throw new IllegalArgumentException("Error parsing CPLC TLV", e);
        }
    }

}
