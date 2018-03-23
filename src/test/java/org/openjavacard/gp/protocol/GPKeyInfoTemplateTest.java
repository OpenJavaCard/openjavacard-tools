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
import java.util.ArrayList;
import java.util.List;

public class GPKeyInfoTemplateTest extends TestCase {

    static final GPKeyInfoTemplate KIT_SCP02_DEFAULT;
    static {
        ArrayList<GPKeyInfo> infos = new ArrayList<>();
        infos.add(GPKeyInfoTest.KI_01_FF_DES_10);
        infos.add(GPKeyInfoTest.KI_02_FF_DES_10);
        infos.add(GPKeyInfoTest.KI_03_FF_DES_10);
        KIT_SCP02_DEFAULT = new GPKeyInfoTemplate(infos);
    }

    static final byte[] KIT_SCP02_DEFAULT_BYTES = HexUtil.hexToBytes("e012c00401ff8010c00402ff8010c00403ff8010");

    public void testParse() throws IOException {
        GPKeyInfoTemplate kit = GPKeyInfoTemplate.fromBytes(KIT_SCP02_DEFAULT_BYTES);
        List<GPKeyInfo> parsedKi = kit.getKeyInfos();
        // check number of key infos
        Assert.assertEquals(3, parsedKi.size());
        // compare key infos
        List<GPKeyInfo> expectedKi = KIT_SCP02_DEFAULT.getKeyInfos();
        for(int i = 0; i < 3; i++) {
            GPKeyInfoTest.assertKeyInfoEquals(expectedKi.get(i), parsedKi.get(i));
        }
    }

}
