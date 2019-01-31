/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2019 Ingo Albrecht <copyright@promovicz.org>
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

package org.openjavacard.gp.scp;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyDiversification;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class SCPDiversificationTest extends TestCase {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SCPDiversificationTest.class);
    }

    @Test
    public void testDiversifyEMV_00000000000000000000() {
        GPKeySet diversified = SCPDiversification.diversify(
                GPKeySet.GLOBALPLATFORM,
                GPKeyDiversification.EMV,
                HexUtil.hexToBytes("00000000000000000000"));
        Assert.assertEquals(0, diversified.getKeyVersion());
        GPKey encKey = diversified.getKeyByUsage(GPKeyUsage.ENC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("5d9a62e01df1fa7a93f231000f4ac272"), encKey.getSecret());
        GPKey macKey = diversified.getKeyByUsage(GPKeyUsage.MAC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("13179f2220fc6bce5e740387eb78db5c"), macKey.getSecret());
        GPKey kekKey = diversified.getKeyByUsage(GPKeyUsage.KEK);
        Assert.assertArrayEquals(HexUtil.hexToBytes("8bbe5457944d18df460af68730f570a2"), kekKey.getSecret());
    }

    @Test
    public void testDiversifyEMV_0102030405060708090A() {
        GPKeySet diversified = SCPDiversification.diversify(
                GPKeySet.GLOBALPLATFORM,
                GPKeyDiversification.EMV,
                HexUtil.hexToBytes("0102030405060708090A"));
        Assert.assertEquals(0, diversified.getKeyVersion());
        GPKey encKey = diversified.getKeyByUsage(GPKeyUsage.ENC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("6438a05ac377df302423bc5e2038fb5b"), encKey.getSecret());
        GPKey macKey = diversified.getKeyByUsage(GPKeyUsage.MAC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("5a9402ddabc5b9497bbece0884929b83"), macKey.getSecret());
        GPKey kekKey = diversified.getKeyByUsage(GPKeyUsage.KEK);
        Assert.assertArrayEquals(HexUtil.hexToBytes("acb4c129b3912fb782a4eaee043bae95"), kekKey.getSecret());
    }

}
