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
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class SCP03WrapperTest extends TestCase {

    private SCP03Parameters SCP03_70 = (SCP03Parameters) SCPParameters.decode(0x03, 0x70);

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

    public void test_SCP03_70_EncryptSensitive() throws CardException {
        SCP03Wrapper wrap = new SCP03Wrapper(GPKeySet.GLOBALPLATFORM, SCP03_70);
        byte [] encNUL = wrap.encryptSensitiveData(new byte[16]);
        System.out.println("encNUL " + HexUtil.bytesToHex(encNUL));
        Assert.assertArrayEquals(HexUtil.hexToBytes("101899564a9da8de833d25c71739eaadce"), encNUL);
        byte [] encGP = wrap.encryptSensitiveData(GPKey.GLOBALPLATFORM_MASTER_SECRET);
        System.out.println("encGP " + HexUtil.bytesToHex(encGP));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1080d2a5b08fa0ee51143b459e638106df"), encGP);
    }

    public void test_SCP03_70_WrapUnwrap_CMAC() throws CardException {
        SCP03Wrapper wrap = new SCP03Wrapper(GPKeySet.GLOBALPLATFORM, SCP03_70);
        CommandAPDU c0 = wrap.wrap(plain5);
        System.out.println("c0 " + HexUtil.bytesToHex(c0.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000300d01020304056cba3161d0e5f44e00"), c0.getBytes());
        ResponseAPDU r0 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r0.getBytes());
        CommandAPDU c1 = wrap.wrap(plain5);
        System.out.println("c1 " + HexUtil.bytesToHex(c1.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000300d010203040557255cc86d20e01800"), c1.getBytes());
        ResponseAPDU r1 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r1.getBytes());
        CommandAPDU c2 = wrap.wrap(plain8);
        System.out.println("c2 " + HexUtil.bytesToHex(c2.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030100102030405060708b8598e9fa5dd06de00"), c2.getBytes());
        ResponseAPDU r2 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r2.getBytes());
        CommandAPDU c3 = wrap.wrap(plain8);
        System.out.println("c3 " + HexUtil.bytesToHex(c3.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003010010203040506070828c6bd13325427fa00"), c3.getBytes());
        ResponseAPDU r3 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r3.getBytes());
        CommandAPDU c4 = wrap.wrap(plain12);
        System.out.println("c4 " + HexUtil.bytesToHex(c4.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030140102030405060708010203045c3b91bf4e621a3900"), c4.getBytes());
        ResponseAPDU r4 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r4.getBytes());
        CommandAPDU c5 = wrap.wrap(plain12);
        System.out.println("c5 " + HexUtil.bytesToHex(c5.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030140102030405060708010203048d584b413e7ed05e00"), c5.getBytes());
        ResponseAPDU r5 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r5.getBytes());
        CommandAPDU c6 = wrap.wrap(plain16);
        System.out.println("c6 " + HexUtil.bytesToHex(c6.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003018010203040506070801020304050607081b348b67ab35562700"), c6.getBytes());
        ResponseAPDU r6 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r6.getBytes());
        CommandAPDU c7 = wrap.wrap(plain16);
        System.out.println("c7 " + HexUtil.bytesToHex(c7.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000301801020304050607080102030405060708b3b02253b71d686e00"), c7.getBytes());
        ResponseAPDU r7 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r7.getBytes());
    }

    public void test_SCP03_70_WrapUnwrap_CENC() throws CardException {
        SCP03Wrapper wrap = new SCP03Wrapper(GPKeySet.GLOBALPLATFORM, SCP03_70);
        wrap.startENC();
        CommandAPDU c0 = wrap.wrap(plain5);
        System.out.println("c0 " + HexUtil.bytesToHex(c0.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030182212fadeae0797fbc19f54b8b46faba0d33f17e5a347c6b900"), c0.getBytes());
        ResponseAPDU r0 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r0.getBytes());
        CommandAPDU c1 = wrap.wrap(plain5);
        System.out.println("c1 " + HexUtil.bytesToHex(c1.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030187f0a9451c29d6f9cbab646603a2ef4127ec180644bbeae1f00"), c1.getBytes());
        ResponseAPDU r1 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r1.getBytes());
        CommandAPDU c2 = wrap.wrap(plain8);
        System.out.println("c2 " + HexUtil.bytesToHex(c2.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003018f31d489def5d524ff436bd6b25a62b021a6582122a3073dd00"), c2.getBytes());
        ResponseAPDU r2 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r2.getBytes());
        CommandAPDU c3 = wrap.wrap(plain8);
        System.out.println("c3 " + HexUtil.bytesToHex(c3.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030186c54833926d632469dc470498150dea971f03daedfa257ec00"), c3.getBytes());
        ResponseAPDU r3 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r3.getBytes());
        CommandAPDU c4 = wrap.wrap(plain12);
        System.out.println("c4 " + HexUtil.bytesToHex(c4.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003018ae446f7d65b9d5bd8ec84d01d25407f3f29fca132313a0d600"), c4.getBytes());
        ResponseAPDU r4 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r4.getBytes());
        CommandAPDU c5 = wrap.wrap(plain12);
        System.out.println("c5 " + HexUtil.bytesToHex(c5.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003018ff39cc0690ece8489d4efa15adbcaa894b855c9081dc758d00"), c5.getBytes());
        ResponseAPDU r5 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r5.getBytes());
        CommandAPDU c6 = wrap.wrap(plain16);
        System.out.println("c6 " + HexUtil.bytesToHex(c6.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003028eb8d9fef342798360af04bc594f470d1c1256a2b4212e8cf66383ee0d93b4cde3d45b455b5eaaf4400"), c6.getBytes());
        ResponseAPDU r6 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r6.getBytes());
        CommandAPDU c7 = wrap.wrap(plain16);
        System.out.println("c7 " + HexUtil.bytesToHex(c7.getBytes()));
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003028c013afb7a2a2b1ea0e28d1a05df632819359347968e97bc64d3ad9e3f187026a32730a30fdfef7de00"), c7.getBytes());
        ResponseAPDU r7 = wrap.unwrap(buildResponse("9000"));
        Assert.assertArrayEquals(HexUtil.hexToBytes("9000"), r7.getBytes());
    }

}
