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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.util.HexUtil;

import java.io.IOException;

@RunWith(BlockJUnit4ClassRunner.class)
public class GPKeyInfoTest extends TestCase {

    static final GPKeyInfo KI_01_FF_DES_10 = new GPKeyInfo(1, 255, 0x80, 16);
    static final GPKeyInfo KI_02_FF_DES_10 = new GPKeyInfo(2, 255, 0x80, 16);
    static final GPKeyInfo KI_03_FF_DES_10 = new GPKeyInfo(3, 255, 0x80, 16);

    private byte[] KI_01_FF_DES_10_BYTES = HexUtil.hexToBytes("C00401ff8010");
    private byte[] KI_02_FF_DES_10_BYTES = HexUtil.hexToBytes("C00402ff8010");
    private byte[] KI_03_FF_DES_10_BYTES = HexUtil.hexToBytes("C00403ff8010");

    private byte[] KI_01_FF_DES_10_BYTES_SHORT = HexUtil.hexToBytes("C00401ff80");
    private byte[] KI_01_FF_DES_10_BYTES_LONG = HexUtil.hexToBytes("C00401ff8010FF");

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(GPKeyInfoTest.class);
    }

    static void assertKeyInfoEquals(GPKeyInfo expected, GPKeyInfo other) {
        Assert.assertEquals(expected.getKeyId(), other.getKeyId());
        Assert.assertEquals(expected.getKeyVersion(), other.getKeyVersion());
        Assert.assertArrayEquals(expected.getKeyTypes(), other.getKeyTypes());
        Assert.assertArrayEquals(expected.getKeySizes(), other.getKeySizes());
    }

    @Test
    public void testParse() throws IOException {
        GPKeyInfo ki1 = GPKeyInfo.fromBytes(KI_01_FF_DES_10_BYTES);
        assertKeyInfoEquals(KI_01_FF_DES_10, ki1);
        Assert.assertEquals(1, ki1.getKeyId());
        Assert.assertEquals(255, ki1.getKeyVersion());
        Assert.assertArrayEquals(new int[]{0x80}, ki1.getKeyTypes());
        Assert.assertArrayEquals(new int[]{0x10}, ki1.getKeySizes());
        GPKeyInfo ki2 = GPKeyInfo.fromBytes(KI_02_FF_DES_10_BYTES);
        assertKeyInfoEquals(KI_02_FF_DES_10, ki2);
        Assert.assertEquals(2, ki2.getKeyId());
        Assert.assertEquals(255, ki2.getKeyVersion());
        Assert.assertArrayEquals(new int[]{0x80}, ki2.getKeyTypes());
        Assert.assertArrayEquals(new int[]{0x10}, ki2.getKeySizes());
        GPKeyInfo ki3 = GPKeyInfo.fromBytes(KI_03_FF_DES_10_BYTES);
        assertKeyInfoEquals(KI_03_FF_DES_10, ki3);
        Assert.assertEquals(3, ki3.getKeyId());
        Assert.assertEquals(255, ki3.getKeyVersion());
        Assert.assertArrayEquals(new int[]{0x80}, ki3.getKeyTypes());
        Assert.assertArrayEquals(new int[]{0x10}, ki3.getKeySizes());
    }

    @Test(expected = IOException.class)
    public void testParseShort() throws IOException {
        GPKeyInfo.fromBytes(KI_01_FF_DES_10_BYTES_SHORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLong() throws IOException, IllegalArgumentException {
        GPKeyInfo.fromBytes(KI_01_FF_DES_10_BYTES_LONG);
    }

}
