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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import javax.smartcardio.CardException;

@RunWith(BlockJUnit4ClassRunner.class)
public class SCPSecurityPolicyTest extends TestCase {

    private SCPParameters SCP00_00 = SCPParameters.decode(0x00, 0x00);
    private SCPParameters SCP01_05 = SCPParameters.decode(0x01, 0x05);
    private SCPParameters SCP01_15 = SCPParameters.decode(0x01, 0x15);
    private SCPParameters SCP02_15 = SCPParameters.decode(0x02, 0x15);
    private SCPParameters SCP02_55 = SCPParameters.decode(0x02, 0x55);
    private SCPParameters SCP03_10 = SCPParameters.decode(0x03, 0x10);
    private SCPParameters SCP03_30 = SCPParameters.decode(0x03, 0x30);
    private SCPParameters SCP03_70 = SCPParameters.decode(0x03, 0x70);

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SCPSecurityPolicyTest.class);
    }

    @Test
    public void testNONEAccept() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.NONE;
        pol.checkProtocol(SCP00_00);
        pol.checkProtocol(SCP01_05);
        pol.checkProtocol(SCP01_15);
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
        pol.checkProtocol(SCP03_30);
        pol.checkProtocol(SCP03_70);
    }

    @Test
    public void testCMACAccept() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CMAC;
        pol.checkProtocol(SCP01_05);
        pol.checkProtocol(SCP01_15);
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
        pol.checkProtocol(SCP03_30);
        pol.checkProtocol(SCP03_70);
    }
    @Test(expected = CardException.class)
    public void testCMACRejectSCP00_00() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CMAC;
        pol.checkProtocol(SCP00_00);
    }

    @Test
    public void testCENCAccept() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CENC;
        pol.checkProtocol(SCP01_05);
        pol.checkProtocol(SCP01_15);
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
        pol.checkProtocol(SCP03_10);
        pol.checkProtocol(SCP03_30);
        pol.checkProtocol(SCP03_70);
    }
    @Test(expected = CardException.class)
    public void testCENCRejectSCP00_00() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CENC;
        pol.checkProtocol(SCP00_00);
    }

    @Test
    public void testRMACAccept() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RMAC;
        pol.checkProtocol(SCP03_30);
        pol.checkProtocol(SCP03_70);
    }
    @Test(expected = CardException.class)
    public void testRMACRejectSCP00_00() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RMAC;
        pol.checkProtocol(SCP00_00);
    }
    @Test(expected = CardException.class)
    public void testRMACRejectSCP01_05() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RMAC;
        pol.checkProtocol(SCP01_05);
    }
    @Test(expected = CardException.class)
    public void testRMACRejectSCP01_15() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RMAC;
        pol.checkProtocol(SCP01_15);
    }
    @Test(expected = CardException.class)
    public void testRMACRejectSCP02_15() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RMAC;
        pol.checkProtocol(SCP02_15);
    }
    @Test(expected = CardException.class)
    public void testRMACRejectSCP02_55() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RMAC;
        pol.checkProtocol(SCP02_55);
    }
    @Test(expected = CardException.class)
    public void testRMACRejectSCP03_10() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RMAC;
        pol.checkProtocol(SCP03_10);
    }

    @Test
    public void testRENCAccept() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP03_70);
    }
    @Test(expected = CardException.class)
    public void testRENCRejectSCP00_00() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP00_00);
    }
    @Test(expected = CardException.class)
    public void testRENCRejectSCP01_05() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP01_05);
    }
    @Test(expected = CardException.class)
    public void testRENCRejectSCP01_15() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP01_15);
    }
    @Test(expected = CardException.class)
    public void testRENCRejectSCP02_15() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP02_15);
    }
    @Test(expected = CardException.class)
    public void testRENCRejectSCP02_55() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP02_55);
    }
    @Test(expected = CardException.class)
    public void testRENCRejectSCP03_10() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP03_10);
    }
    @Test(expected = CardException.class)
    public void testRENCRejectSCP03_30() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.RENC;
        pol.checkProtocol(SCP03_30);
    }

}
