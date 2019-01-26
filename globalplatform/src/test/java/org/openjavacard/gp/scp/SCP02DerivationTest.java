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

package org.openjavacard.gp.scp;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class SCP02DerivationTest extends TestCase {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SCP02DerivationTest.class);
    }

    @Test
    public void testDeriveSCP02_0000() {
        GPKeySet derived = SCP02Derivation.deriveSessionKeys(GPKeySet.GLOBALPLATFORM, HexUtil.hexToBytes("0000"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByUsage(GPKeyUsage.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("010b0371d78377b801f2d62afc671d95"), encKey.getSecret());
        GPKey macKey = derived.getKeyByUsage(GPKeyUsage.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("d1c28c601652a4770d67ad82d2d2e1c4"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByUsage(GPKeyUsage.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("e11987ee331b417a5d67d760692f89d4"), kekKey.getSecret());
    }

    @Test
    public void testDeriveSCP02_0001() {
        GPKeySet derived = SCP02Derivation.deriveSessionKeys(GPKeySet.GLOBALPLATFORM,HexUtil.hexToBytes("0001"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByUsage(GPKeyUsage.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("25c9794a1205ff244f5fa0378d2f8d59"), encKey.getSecret());
        GPKey macKey = derived.getKeyByUsage(GPKeyUsage.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("9bed98891580c3b245fe9ec58bfa8d2a"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByUsage(GPKeyUsage.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("0e51fdf196141f227a57bd154012fd39"), kekKey.getSecret());
    }

    @Test
    public void testDeriveSCP02_007F() {
        GPKeySet derived = SCP02Derivation.deriveSessionKeys(GPKeySet.GLOBALPLATFORM,HexUtil.hexToBytes("007F"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByUsage(GPKeyUsage.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("f463d35410fd6ed925c7b07b333cfaf9"), encKey.getSecret());
        GPKey macKey = derived.getKeyByUsage(GPKeyUsage.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("161c8aa468e658c45ae3eca8387dfb19"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByUsage(GPKeyUsage.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("369a67c04590b93101ca413613db706a"), kekKey.getSecret());
    }

    @Test
    public void testDeriveSCP02_FFFF() {
        GPKeySet derived = SCP02Derivation.deriveSessionKeys(GPKeySet.GLOBALPLATFORM,HexUtil.hexToBytes("FFFF"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByUsage(GPKeyUsage.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("e75adaaf916de18a01ab9acc6ef84b8b"), encKey.getSecret());
        GPKey macKey = derived.getKeyByUsage(GPKeyUsage.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("66f6b778a1fafccc9de6c253233be33d"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByUsage(GPKeyUsage.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("3c750e0f60899ffc8f0d26d860ae6c95"), kekKey.getSecret());
    }

}
