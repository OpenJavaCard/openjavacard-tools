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

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class CPLCTest extends TestCase {

    private static final byte[] CPLC_BYTES =
            HexUtil.hexToBytes(
                     "9f7f2a479050384791007833003332A0"+
                     "B1C2D3ABCD4812333900000000060f20"+
                     "32A0B1C2D30000000000000000");

    private static final byte[] CPLC_SHORT =
            HexUtil.hexToBytes(
                    "9f7f2a479050384791007833003332A0"+
                    "B1C2D3ABCD4812333900000000060f20"+
                    "32A0B1C2D300000000000000");

    private static final byte[] CPLC_LONG =
            HexUtil.hexToBytes(
                    "9f7f2a479050384791007833003332A0"+
                    "B1C2D3ABCD4812333900000000060f20"+
                    "32A0B1C2D30000000000000000ABCD");

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(CPLCTest.class);
    }

    @Test
    public void testParse() {
        CPLC lc = CPLC.read(CPLC_BYTES);
        Assert.assertEquals("4790", lc.getFieldHex(CPLC.Field.ICFabricator));
        Assert.assertEquals("5038", lc.getFieldHex(CPLC.Field.ICType));
        Assert.assertEquals("4791", lc.getFieldHex(CPLC.Field.OperatingSystemID));
        Assert.assertEquals("0078", lc.getFieldHex(CPLC.Field.OperatingSystemReleaseDate));
        Assert.assertEquals("3300", lc.getFieldHex(CPLC.Field.OperatingSystemReleaseLevel));
        Assert.assertEquals("3332", lc.getFieldHex(CPLC.Field.ICFabricationDate));
        Assert.assertEquals("a0b1c2d3", lc.getFieldHex(CPLC.Field.ICSerialNumber));
        Assert.assertEquals("abcd", lc.getFieldHex(CPLC.Field.ICBatchIdentifier));
        Assert.assertEquals("4812", lc.getFieldHex(CPLC.Field.ICModuleFabricator));
        Assert.assertEquals("3339", lc.getFieldHex(CPLC.Field.ICModulePackagingDate));
        Assert.assertEquals("0000", lc.getFieldHex(CPLC.Field.ICCManufacturer));
        Assert.assertEquals("0000", lc.getFieldHex(CPLC.Field.ICEmbeddingDate));
        Assert.assertEquals("060f", lc.getFieldHex(CPLC.Field.ICPrePersonalizer));
        Assert.assertEquals("2032", lc.getFieldHex(CPLC.Field.ICPrePersonalizationEquipmentDate));
        Assert.assertEquals("a0b1c2d3", lc.getFieldHex(CPLC.Field.ICPrePersonalizationEquipmentID));
        Assert.assertEquals("0000", lc.getFieldHex(CPLC.Field.ICPersonalizer));
        Assert.assertEquals("0000", lc.getFieldHex(CPLC.Field.ICPersonalizationDate));
        Assert.assertEquals("00000000", lc.getFieldHex(CPLC.Field.ICPersonalizationEquipmentID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseShort() throws IllegalArgumentException {
        CPLC.read(CPLC_SHORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLong() throws IllegalArgumentException {
        CPLC.read(CPLC_LONG);
    }

}
