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
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class SCP02WrapperTest extends TestCase {

    private SCP0102Parameters SCP02_15 = (SCP0102Parameters) SCPParameters.decode(0x02, 0x15);

    // sizes chosen to trigger padding issues
    private CommandAPDU plain5 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5});
    private CommandAPDU plain8 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
    private CommandAPDU plain12 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4});
    private CommandAPDU plain16 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8});

    private ResponseAPDU buildResponse(String hex) {
        return new ResponseAPDU(HexUtil.hexToBytes(hex));
    }

    public void testReference() {
        Assert.assertArrayEquals(HexUtil.hexToBytes("1020003005010203040500"), plain5.getBytes());
        Assert.assertArrayEquals(HexUtil.hexToBytes("1020003008010203040506070800"), plain8.getBytes());
        Assert.assertArrayEquals(HexUtil.hexToBytes("102000300c01020304050607080102030400"), plain12.getBytes());
        Assert.assertArrayEquals(HexUtil.hexToBytes("10200030100102030405060708010203040506070800"), plain16.getBytes());
    }

    public void test_SCP02_15_EncryptSensitive() throws CardException {
        SCP0102Wrapper wrap = new SCP0102Wrapper(GPKeySet.GLOBALPLATFORM, SCP02_15);
        byte [] encNUL = wrap.encryptSensitiveData(new byte[16]);
        System.out.println("encNUL " + HexUtil.bytesToHex(encNUL));
        Assert.assertArrayEquals(HexUtil.hexToBytes("8baf473f2f8fd0948baf473f2f8fd094"), encNUL);
        byte [] encGP = wrap.encryptSensitiveData(GPKey.GLOBALPLATFORM_MASTER_SECRET);
        System.out.println("encGP " + HexUtil.bytesToHex(encGP));
        Assert.assertArrayEquals(HexUtil.hexToBytes("b4baa89a8cd0292b45210e1bc84b1c31"), encGP);
    }

    public void test_SCP02_15_WrapUnwrap_CMAC() throws CardException {
        SCP0102Wrapper wrap = new SCP0102Wrapper(GPKeySet.GLOBALPLATFORM, SCP02_15);
        CommandAPDU c0 = wrap.wrap(plain5);
        System.out.println("c0 " + HexUtil.bytesToHex(c0.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000300d0102030405fb4034e025786ab100"), c0.getBytes());
        ResponseAPDU r0 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r0.getBytes());
        CommandAPDU c1 = wrap.wrap(plain5);
        System.out.println("c1 " + HexUtil.bytesToHex(c1.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000300d01020304050a3bebcf697d082900"), c1.getBytes());
        ResponseAPDU r1 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r1.getBytes());
        CommandAPDU c2 = wrap.wrap(plain8);
        System.out.println("c2 " + HexUtil.bytesToHex(c2.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000301001020304050607081bebeae51e869dac00"), c2.getBytes());
        ResponseAPDU r2 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r2.getBytes());
        CommandAPDU c3 = wrap.wrap(plain8);
        System.out.println("c3 " + HexUtil.bytesToHex(c3.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003010010203040506070840f75851a11470c500"), c3.getBytes());
        ResponseAPDU r3 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r3.getBytes());
        CommandAPDU c4 = wrap.wrap(plain12);
        System.out.println("c4 " + HexUtil.bytesToHex(c4.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003014010203040506070801020304e651f07b2bddccc000"), c4.getBytes());
        ResponseAPDU r4 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r4.getBytes());
        CommandAPDU c5 = wrap.wrap(plain12);
        System.out.println("c5 " + HexUtil.bytesToHex(c5.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003014010203040506070801020304b4744d06cf32ce3600"), c5.getBytes());
        ResponseAPDU r5 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r5.getBytes());
        CommandAPDU c6 = wrap.wrap(plain16);
        System.out.println("c6 " + HexUtil.bytesToHex(c6.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000301801020304050607080102030405060708a3efc1f8df5c466000"), c6.getBytes());
        ResponseAPDU r6 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r6.getBytes());
        CommandAPDU c7 = wrap.wrap(plain16);
        System.out.println("c7 " + HexUtil.bytesToHex(c7.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000301801020304050607080102030405060708f590535e9ec765cd00"), c7.getBytes());
        ResponseAPDU r7 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r7.getBytes());
    }

    public void test_SCP02_15_WrapUnwrap_CENC() throws CardException {
        SCP0102Wrapper wrap = new SCP0102Wrapper(GPKeySet.GLOBALPLATFORM, SCP02_15);
        wrap.startENC();
        CommandAPDU c0 = wrap.wrap(plain5);
        System.out.println("c0 " + HexUtil.bytesToHex(c0.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003010ca0868ab790be10ffb4034e025786ab100"), c0.getBytes());
        ResponseAPDU r0 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r0.getBytes());
        CommandAPDU c1 = wrap.wrap(plain5);
        System.out.println("c1 " + HexUtil.bytesToHex(c1.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003010ca0868ab790be10f0a3bebcf697d082900"), c1.getBytes());
        ResponseAPDU r1 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r1.getBytes());
        CommandAPDU c2 = wrap.wrap(plain8);
        System.out.println("c2 " + HexUtil.bytesToHex(c2.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be73d0941f83dad0fd1bebeae51e869dac00"), c2.getBytes());
        ResponseAPDU r2 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r2.getBytes());
        CommandAPDU c3 = wrap.wrap(plain8);
        System.out.println("c3 " + HexUtil.bytesToHex(c3.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be73d0941f83dad0fd40f75851a11470c500"), c3.getBytes());
        ResponseAPDU r3 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r3.getBytes());
        CommandAPDU c4 = wrap.wrap(plain12);
        System.out.println("c4 " + HexUtil.bytesToHex(c4.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be7af4e62b6085037ae651f07b2bddccc000"), c4.getBytes());
        ResponseAPDU r4 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r4.getBytes());
        CommandAPDU c5 = wrap.wrap(plain12);
        System.out.println("c5 " + HexUtil.bytesToHex(c5.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be7af4e62b6085037ab4744d06cf32ce3600"), c5.getBytes());
        ResponseAPDU r5 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r5.getBytes());
        CommandAPDU c6 = wrap.wrap(plain16);
        System.out.println("c6 " + HexUtil.bytesToHex(c6.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030200e9a7741e84385bea5312176c1f49f83f544aa3defb12223a3efc1f8df5c466000"), c6.getBytes());
        ResponseAPDU r6 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r6.getBytes());
        CommandAPDU c7 = wrap.wrap(plain16);
        System.out.println("c7 " + HexUtil.bytesToHex(c7.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030200e9a7741e84385bea5312176c1f49f83f544aa3defb12223f590535e9ec765cd00"), c7.getBytes());
        ResponseAPDU r7 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r7.getBytes());
    }

}
