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
public class GPCardDataTest extends TestCase {

    private static final byte[] CD_GP211_BYTES =
            HexUtil.hexToBytes(
                    "664c734a06072a864886fc6b01600c060a2a864886fc6b02020101630906072a"+
                    "864886fc6b03640b06092a864886fc6b040215650b06092b8510864864020103"+
                    "660c060a2b060104012a026e0102");

    private static final byte[] CD_GP211_SHORT =
            HexUtil.hexToBytes(
                    "664c734a06072a864886fc6b01600c060a2a864886fc6b02020101630906072a"+
                            "864886fc6b03640b06092a864886fc6b040215650b06092b8510864864020103"+
                            "660c060a2b060104012a026e01");

    private static final byte[] CD_GP211_LONG =
            HexUtil.hexToBytes(
                    "664c734a06072a864886fc6b01600c060a2a864886fc6b02020101630906072a"+
                            "864886fc6b03640b06092a864886fc6b040215650b06092b8510864864020103"+
                            "660c060a2b060104012a026e0102FF");

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(GPCardDataTest.class);
    }

    @Test
    public void testParse() throws IOException {
        GPCardData cd = GPCardData.fromBytes(CD_GP211_BYTES);
        Assert.assertTrue(cd.isGlobalPlatform());
        Assert.assertTrue(cd.isUniquelyIdentifiable());
        Assert.assertArrayEquals(new byte[]{2,1,1}, cd.getGlobalPlatformVersion());
        Assert.assertEquals("2.1.1", cd.getGlobalPlatformVersionString());
        Assert.assertEquals(0x02, cd.getSecurityProtocol());
        Assert.assertEquals(0x15, cd.getSecurityParameters());
    }

    @Test(expected = IOException.class)
    public void testParseShort() throws IOException {
        GPCardData.fromBytes(CD_GP211_SHORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLong() throws IOException {
        GPCardData.fromBytes(CD_GP211_LONG);
    }

}
