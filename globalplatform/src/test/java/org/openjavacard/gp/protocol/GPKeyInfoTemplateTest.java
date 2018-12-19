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

package org.openjavacard.gp.protocol;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.util.HexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(BlockJUnit4ClassRunner.class)
public class GPKeyInfoTemplateTest extends TestCase {

    private static final byte[] KIT_SCP02_DEFAULT_BYTES =
            HexUtil.hexToBytes("e012c00401ff8010c00402ff8010c00403ff8010");

    private static final byte[] KIT_SCP02_DEFAULT_SHORT =
            HexUtil.hexToBytes("e012c00401ff8010c00402ff8010c00403ff80");
    private static final byte[] KIT_SCP02_DEFAULT_LONG =
            HexUtil.hexToBytes("e012c00401ff8010c00402ff8010c00403ff8010FF");

    private static final GPKeyInfoTemplate KIT_SCP02_DEFAULT;
    static {
        ArrayList<GPKeyInfo> infos = new ArrayList<>();
        infos.add(GPKeyInfoTest.KI_01_FF_DES_10);
        infos.add(GPKeyInfoTest.KI_02_FF_DES_10);
        infos.add(GPKeyInfoTest.KI_03_FF_DES_10);
        KIT_SCP02_DEFAULT = new GPKeyInfoTemplate(infos);
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(GPKeyInfoTemplateTest.class);
    }

    @Test
    public void testParse() throws IOException {
        // parse reference bytes
        GPKeyInfoTemplate kit = GPKeyInfoTemplate.fromBytes(KIT_SCP02_DEFAULT_BYTES);
        // all there is to check is the key infos
        List<GPKeyInfo> parsedKi = kit.getKeyInfos();
        // check number of key infos
        Assert.assertEquals(3, parsedKi.size());
        // compare key infos
        List<GPKeyInfo> expectedKi = KIT_SCP02_DEFAULT.getKeyInfos();
        for(int i = 0; i < 3; i++) {
            GPKeyInfoTest.assertKeyInfoEquals(expectedKi.get(i), parsedKi.get(i));
        }
    }

    @Test(expected = IOException.class)
    public void testParseShort() throws IOException, IllegalArgumentException {
        GPKeyInfoTemplate.fromBytes(KIT_SCP02_DEFAULT_SHORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLong() throws IOException, IllegalArgumentException {
        GPKeyInfoTemplate.fromBytes(KIT_SCP02_DEFAULT_LONG);
    }

}
