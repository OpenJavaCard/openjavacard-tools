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

package org.openjavacard.gp.scp;

import junit.framework.TestCase;
import org.junit.Assert;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;

public class SCP02WrapperTest extends TestCase {

    private SCP0102Protocol SCP02_15 = (SCP0102Protocol)SCPProtocol.decode(0x02, 0x15);

    // sizes chosen to trigger padding issues
    private CommandAPDU plain5 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5});
    private CommandAPDU plain8 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
    private CommandAPDU plain12 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4});
    private CommandAPDU plain16 = APDUUtil.buildCommand((byte)0x10, (byte)0x20, (short)0x30,
            new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8});

    public void testReference() {
        Assert.assertArrayEquals(HexUtil.hexToBytes("10200030050102030405"), plain5.getBytes());
        Assert.assertArrayEquals(HexUtil.hexToBytes("10200030080102030405060708"), plain8.getBytes());
        Assert.assertArrayEquals(HexUtil.hexToBytes("102000300c010203040506070801020304"), plain12.getBytes());
        Assert.assertArrayEquals(HexUtil.hexToBytes("102000301001020304050607080102030405060708"), plain16.getBytes());
    }

    public void testSCP02_15WrapCMAC() throws CardException {
        SCP0102Wrapper wrap = new SCP0102Wrapper(GPKeySet.GLOBALPLATFORM, SCP02_15);
        CommandAPDU cmac0 = wrap.wrap(plain5);
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000300d0102030405fb4034e025786ab1"), cmac0.getBytes());
        CommandAPDU cmac1 = wrap.wrap(plain5);
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000300d01020304050a3bebcf697d0829"), cmac1.getBytes());
        CommandAPDU cmac2 = wrap.wrap(plain8);
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000301001020304050607081bebeae51e869dac"), cmac2.getBytes());
        CommandAPDU cmac3 = wrap.wrap(plain8);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003010010203040506070840f75851a11470c5"), cmac3.getBytes());
        CommandAPDU cmac4 = wrap.wrap(plain12);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003014010203040506070801020304e651f07b2bddccc0"), cmac4.getBytes());
        CommandAPDU cmac5 = wrap.wrap(plain12);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003014010203040506070801020304b4744d06cf32ce36"), cmac5.getBytes());
        CommandAPDU cmac6 = wrap.wrap(plain16);
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000301801020304050607080102030405060708a3efc1f8df5c4660"), cmac6.getBytes());
        CommandAPDU cmac7 = wrap.wrap(plain16);
        Assert.assertArrayEquals(HexUtil.hexToBytes("142000301801020304050607080102030405060708f590535e9ec765cd"), cmac7.getBytes());
    }
    public void testSCP02_15WrapCENC() throws CardException {
        SCP0102Wrapper wrap = new SCP0102Wrapper(GPKeySet.GLOBALPLATFORM, SCP02_15);
        wrap.startENC();
        CommandAPDU cenc0 = wrap.wrap(plain5);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003010ca0868ab790be10ffb4034e025786ab1"), cenc0.getBytes());
        CommandAPDU cenc1 = wrap.wrap(plain5);
        Assert.assertArrayEquals(HexUtil.hexToBytes("1420003010ca0868ab790be10f0a3bebcf697d0829"), cenc1.getBytes());
        CommandAPDU cenc2 = wrap.wrap(plain8);
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be73d0941f83dad0fd1bebeae51e869dac"), cenc2.getBytes());
        CommandAPDU cenc3 = wrap.wrap(plain8);
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be73d0941f83dad0fd40f75851a11470c5"), cenc3.getBytes());
        CommandAPDU cenc4 = wrap.wrap(plain12);
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be7af4e62b6085037ae651f07b2bddccc0"), cenc4.getBytes());
        CommandAPDU cenc5 = wrap.wrap(plain12);
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030180e9a7741e84385be7af4e62b6085037ab4744d06cf32ce36"), cenc5.getBytes());
        CommandAPDU cenc6 = wrap.wrap(plain16);
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030200e9a7741e84385bea5312176c1f49f83f544aa3defb12223a3efc1f8df5c4660"), cenc6.getBytes());
        CommandAPDU cenc7 = wrap.wrap(plain16);
        Assert.assertArrayEquals(HexUtil.hexToBytes("14200030200e9a7741e84385bea5312176c1f49f83f544aa3defb12223f590535e9ec765cd"), cenc7.getBytes());
    }

}
