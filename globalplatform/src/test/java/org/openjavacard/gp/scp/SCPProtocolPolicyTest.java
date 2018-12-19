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

import javax.smartcardio.CardException;

@RunWith(BlockJUnit4ClassRunner.class)
public class SCPProtocolPolicyTest extends TestCase {

    // all variants of SCP01
    private SCPParameters SCP01_05 = SCPParameters.decode(0x01, 0x05);
    private SCPParameters SCP01_15 = SCPParameters.decode(0x01, 0x15);
    // common variants of SCP02
    private SCPParameters SCP02_15  = SCPParameters.decode(0x02, 0x15);
    private SCPParameters SCP02_55  = SCPParameters.decode(0x02, 0x55);
    // realistic variants of SCP03
    private SCPParameters SCP03_00  = SCPParameters.decode(0x03, 0x00);
    private SCPParameters SCP03_10  = SCPParameters.decode(0x03, 0x10);
    private SCPParameters SCP03_20  = SCPParameters.decode(0x03, 0x20);
    private SCPParameters SCP03_30  = SCPParameters.decode(0x03, 0x30);
    private SCPParameters SCP03_60  = SCPParameters.decode(0x03, 0x60);
    private SCPParameters SCP03_70  = SCPParameters.decode(0x03, 0x70);

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SCPProtocolPolicyTest.class);
    }

    @Test
    public void testPermissiveAccept() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.PERMISSIVE;
        Assert.assertTrue(pol.isVersionAllowed(0x01));
        Assert.assertTrue(pol.isVersionAllowed(0x02));
        Assert.assertTrue(pol.isVersionAllowed(0x03));
        Assert.assertTrue(pol.isProtocolAllowed(0x01, 0x05));
        Assert.assertTrue(pol.isProtocolAllowed(0x01, 0x15));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x55));
        Assert.assertTrue(pol.isProtocolAllowed(SCP01_05));
        Assert.assertTrue(pol.isProtocolAllowed(SCP01_15));
        Assert.assertTrue(pol.isProtocolAllowed(SCP02_15));
        Assert.assertTrue(pol.isProtocolAllowed(SCP02_55));
        pol.checkProtocol(SCP01_05);
        pol.checkProtocol(SCP01_15);
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
    }

    @Test
    public void testSCP01Accept() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP01;
        Assert.assertTrue(pol.isVersionAllowed(0x01));
        Assert.assertFalse(pol.isVersionAllowed(0x02));
        Assert.assertFalse(pol.isVersionAllowed(0x03));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x55));
        Assert.assertTrue(pol.isProtocolAllowed(SCP01_05));
        Assert.assertTrue(pol.isProtocolAllowed(SCP01_15));
        Assert.assertFalse(pol.isProtocolAllowed(SCP02_15));
        Assert.assertFalse(pol.isProtocolAllowed(SCP02_55));
        pol.checkProtocol(SCP01_05);
        pol.checkProtocol(SCP01_15);
    }
    @Test(expected = CardException.class)
    public void testSCP01RejectSCP02_15() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP01;
        pol.checkProtocol(SCP02_15);
    }
    @Test(expected = CardException.class)
    public void testSCP01RejectSCP02_55() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP01;
        pol.checkProtocol(SCP02_55);
    }

    @Test
    public void testSCP02Accept() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP02;
        Assert.assertFalse(pol.isVersionAllowed(0x01));
        Assert.assertTrue(pol.isVersionAllowed(0x02));
        Assert.assertFalse(pol.isVersionAllowed(0x03));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x55));
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
    }
    @Test(expected = CardException.class)
    public void testSCP02RejectSCP01_05() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP02;
        pol.checkProtocol(SCP01_05);
    }
    @Test(expected = CardException.class)
    public void testSCP02RejectSCP01_15() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP02;
        pol.checkProtocol(SCP01_15);
    }

    @Test
    public void testSCP03Accept() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP03;
        Assert.assertFalse(pol.isVersionAllowed(0x01));
        Assert.assertFalse(pol.isVersionAllowed(0x02));
        Assert.assertTrue(pol.isVersionAllowed(0x03));
        Assert.assertFalse(pol.isProtocolAllowed(0x01, 0x05));
        Assert.assertFalse(pol.isProtocolAllowed(0x01, 0x15));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x55));
        Assert.assertTrue(pol.isProtocolAllowed(0x03, 0x00));
        Assert.assertTrue(pol.isProtocolAllowed(0x03, 0x10));
        Assert.assertTrue(pol.isProtocolAllowed(0x03, 0x20));
        Assert.assertTrue(pol.isProtocolAllowed(0x03, 0x30));
        Assert.assertTrue(pol.isProtocolAllowed(0x03, 0x60));
        Assert.assertTrue(pol.isProtocolAllowed(0x03, 0x70));
        Assert.assertFalse(pol.isProtocolAllowed(SCP01_05));
        Assert.assertFalse(pol.isProtocolAllowed(SCP01_15));
        Assert.assertFalse(pol.isProtocolAllowed(SCP02_15));
        Assert.assertFalse(pol.isProtocolAllowed(SCP02_55));
        Assert.assertTrue(pol.isProtocolAllowed(SCP03_00));
        Assert.assertTrue(pol.isProtocolAllowed(SCP03_10));
        Assert.assertTrue(pol.isProtocolAllowed(SCP03_20));
        Assert.assertTrue(pol.isProtocolAllowed(SCP03_30));
        Assert.assertTrue(pol.isProtocolAllowed(SCP03_60));
        Assert.assertTrue(pol.isProtocolAllowed(SCP03_70));
        pol.checkProtocol(SCP03_00);
        pol.checkProtocol(SCP03_10);
        pol.checkProtocol(SCP03_20);
        pol.checkProtocol(SCP03_30);
        pol.checkProtocol(SCP03_60);
        pol.checkProtocol(SCP03_70);
    }

    @Test(expected = CardException.class)
    public void testSCP03RejectSCP01_05() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP03;
        pol.checkProtocol(SCP01_05);
    }
    @Test(expected = CardException.class)
    public void testSCP03RejectSCP01_15() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP03;
        pol.checkProtocol(SCP01_15);
    }
    @Test(expected = CardException.class)
    public void testSCP03RejectSCP02_15() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP03;
        pol.checkProtocol(SCP02_15);
    }
    @Test(expected = CardException.class)
    public void testSCP03RejectSCP02_55() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP03;
        pol.checkProtocol(SCP02_55);
    }

}
