/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.gp.protocol;

import org.openjavacard.util.HexUtil;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * GlobalPlatform Card Production Life Cycle data
 * <p/>
 * This describes the card and how it has been treated
 * in various phases of production and provisioning.
 * <p/>
 */
public class GPLifeCycle {

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

        public final int fieldLength;

        Field(int length) {
            this.fieldLength = length;
        }
    }

    LinkedHashMap<Field, byte[]> mValues;

    public byte[] getFieldValue(Field field) {
        if (mValues == null) {
            return null;
        } else {
            return mValues.get(field).clone();
        }
    }

    public String getFieldHex(Field field) {
        byte[] value = getFieldValue(field);
        if(value == null) {
            return null;
        }
        return HexUtil.bytesToHex(value);
    }

    public String getLifetimeIdentifier() {
        return "card"
                + "-f" + getFieldHex(GPLifeCycle.Field.ICFabricator)
                + "-t" + getFieldHex(GPLifeCycle.Field.ICType)
                + "-s" + getFieldHex(GPLifeCycle.Field.ICSerialNumber);
    }

    public void read(byte[] buf) {
        read(buf, 0, buf.length);
    }

    public void read(byte[] buf, int off, int len) {
        LinkedHashMap<Field, byte[]> values = new LinkedHashMap<>();
        int fieldOff = off + 3; // XXX: need to parse TLV with long tag
        // XXX length check before looping?
        for (Field field : Field.values()) {
            int fieldLen = field.fieldLength;

            if (fieldOff >= len) {
                throw new IllegalArgumentException("CPLC too short");
            }

            values.put(field, Arrays.copyOfRange(buf, fieldOff, fieldOff + fieldLen));

            fieldOff += fieldLen;
        }
        mValues = values;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GP Card Production Lifecycle:");
        if (mValues == null) {
            sb.append("  EMPTY");
        } else {
            for (Field field : Field.values()) {
                byte[] value = mValues.get(field);
                if (value != null) {
                    sb.append("\n  " + field + ": " + HexUtil.bytesToHex(value));
                }
            }
        }
        return sb.toString();
    }

}
