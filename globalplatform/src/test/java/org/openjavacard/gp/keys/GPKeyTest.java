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

package org.openjavacard.gp.keys;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class GPKeyTest extends TestCase {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(GPKeyTest.class);
    }

    @Test
    public void testKeyCheckValueDES3() {
        byte[] kcv = GPKey.GLOBALPLATFORM_MASTER.getCheckValue(GPKeyCipher.DES3);
        Assert.assertArrayEquals(HexUtil.hexToBytes("8baf47"), kcv);
    }

    @Test
    public void testKeyCheckValueAES() {
        byte[] kcv = GPKey.GLOBALPLATFORM_MASTER.getCheckValue(GPKeyCipher.AES);
        Assert.assertArrayEquals(HexUtil.hexToBytes("504A77"), kcv);
    }

}
