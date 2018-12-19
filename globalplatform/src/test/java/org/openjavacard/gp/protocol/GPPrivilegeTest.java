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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.util.HexUtil;

import java.util.ArrayList;

@RunWith(BlockJUnit4ClassRunner.class)
public class GPPrivilegeTest {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(GPPrivilegeTest.class);
    }

    @Test
    public void testPrivilegeEncode() {
        // encode empty set
        ArrayList<GPPrivilege> p0 = new ArrayList<>();
        byte[] b0 = GPPrivilege.toBytes(p0);
        Assert.assertArrayEquals(HexUtil.hexToBytes("00"), b0);

        // encode simple list of privs
        ArrayList<GPPrivilege> p1 = new ArrayList<>();
        p1.add(GPPrivilege.CARD_LOCK);
        p1.add(GPPrivilege.CARD_RESET);
        p1.add(GPPrivilege.CARD_TERMINATE);
        byte[] b1 = GPPrivilege.toBytes(p1);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1c"), b1);

        // encode second-byte priv
        ArrayList<GPPrivilege> p2 = new ArrayList<>();
        p2.add(GPPrivilege.FINAL_APPLICATION);
        byte[] b2 = GPPrivilege.toBytes(p2);
        Assert.assertArrayEquals(HexUtil.hexToBytes("000200"), b2);

        // encode another second-byte priv
        ArrayList<GPPrivilege> p3 = new ArrayList<>();
        p3.add(GPPrivilege.GLOBAL_SERVICE);
        byte[] b3 = GPPrivilege.toBytes(p3);
        Assert.assertArrayEquals(HexUtil.hexToBytes("000100"), b3);

        // encode two second-byte privs
        ArrayList<GPPrivilege> p4 = new ArrayList<>();
        p4.add(GPPrivilege.FINAL_APPLICATION);
        p4.add(GPPrivilege.GLOBAL_SERVICE);
        byte[] b4 = GPPrivilege.toBytes(p4);
        Assert.assertArrayEquals(HexUtil.hexToBytes("000300"), b4);

        // encode privs in first two bytes
        ArrayList<GPPrivilege> p5 = new ArrayList<>();
        p5.add(GPPrivilege.CARD_LOCK);
        p5.add(GPPrivilege.CARD_RESET);
        p5.add(GPPrivilege.CARD_TERMINATE);
        p5.add(GPPrivilege.FINAL_APPLICATION);
        p5.add(GPPrivilege.GLOBAL_SERVICE);
        byte[] b5 = GPPrivilege.toBytes(p5);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1c0300"), b5);

        // encode a third-byte priv
        ArrayList<GPPrivilege> p6 = new ArrayList<>();
        p6.add(GPPrivilege.CONTACTLESS_ACTIVATION);
        byte[] b6 = GPPrivilege.toBytes(p6);
        Assert.assertArrayEquals(HexUtil.hexToBytes("000020"), b6);

        // encode privs in three bytes
        ArrayList<GPPrivilege> p7 = new ArrayList<>();
        p7.add(GPPrivilege.CARD_LOCK);
        p7.add(GPPrivilege.CARD_RESET);
        p7.add(GPPrivilege.CARD_TERMINATE);
        p7.add(GPPrivilege.FINAL_APPLICATION);
        p7.add(GPPrivilege.GLOBAL_SERVICE);
        p7.add(GPPrivilege.CONTACTLESS_ACTIVATION);
        byte[] b7 = GPPrivilege.toBytes(p7);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1c0320"), b7);
    }

}
