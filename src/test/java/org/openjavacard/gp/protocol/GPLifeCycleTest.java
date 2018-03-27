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

package org.openjavacard.gp.protocol;

import junit.framework.TestCase;
import org.junit.Assert;
import org.openjavacard.util.HexUtil;

import java.io.IOException;

public class GPLifeCycleTest extends TestCase {

    private static final byte[] CPLC_BYTES =
            HexUtil.hexToBytes(
                     "9f7f2a479050384791007833003332A0"+
                     "B1C2D3ABCD4812333900000000060f20"+
                     "32A0B1C2D30000000000000000");

    public void testParse() {
        GPLifeCycle lc = GPLifeCycle.read(CPLC_BYTES);
        Assert.assertEquals("4790", lc.getFieldHex(GPLifeCycle.Field.ICFabricator));
        Assert.assertEquals("5038", lc.getFieldHex(GPLifeCycle.Field.ICType));
        Assert.assertEquals("4791", lc.getFieldHex(GPLifeCycle.Field.OperatingSystemID));
        Assert.assertEquals("0078", lc.getFieldHex(GPLifeCycle.Field.OperatingSystemReleaseDate));
        Assert.assertEquals("3300", lc.getFieldHex(GPLifeCycle.Field.OperatingSystemReleaseLevel));
        Assert.assertEquals("3332", lc.getFieldHex(GPLifeCycle.Field.ICFabricationDate));
        Assert.assertEquals("a0b1c2d3", lc.getFieldHex(GPLifeCycle.Field.ICSerialNumber));
        Assert.assertEquals("abcd", lc.getFieldHex(GPLifeCycle.Field.ICBatchIdentifier));
        Assert.assertEquals("4812", lc.getFieldHex(GPLifeCycle.Field.ICModuleFabricator));
        Assert.assertEquals("3339", lc.getFieldHex(GPLifeCycle.Field.ICModulePackagingDate));
        Assert.assertEquals("0000", lc.getFieldHex(GPLifeCycle.Field.ICCManufacturer));
        Assert.assertEquals("0000", lc.getFieldHex(GPLifeCycle.Field.ICEmbeddingDate));
        Assert.assertEquals("060f", lc.getFieldHex(GPLifeCycle.Field.ICPrePersonalizer));
        Assert.assertEquals("2032", lc.getFieldHex(GPLifeCycle.Field.ICPrePersonalizationEquipmentDate));
        Assert.assertEquals("a0b1c2d3", lc.getFieldHex(GPLifeCycle.Field.ICPrePersonalizationEquipmentID));
        Assert.assertEquals("0000", lc.getFieldHex(GPLifeCycle.Field.ICPersonalizer));
        Assert.assertEquals("0000", lc.getFieldHex(GPLifeCycle.Field.ICPersonalizationDate));
        Assert.assertEquals("00000000", lc.getFieldHex(GPLifeCycle.Field.ICPersonalizationEquipmentID));
    }

}
