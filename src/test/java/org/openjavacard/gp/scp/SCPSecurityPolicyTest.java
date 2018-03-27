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

    private SCPProtocol SCP01_05 = SCPProtocol.decode(0x01, 0x05);
    private SCPProtocol SCP01_15 = SCPProtocol.decode(0x01, 0x15);
    private SCPProtocol SCP02_15 = SCPProtocol.decode(0x02, 0x15);
    private SCPProtocol SCP02_55 = SCPProtocol.decode(0x02, 0x55);

    @Test
    public void testCMACAccept() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CMAC;
        pol.checkProtocol(SCP01_05);
        pol.checkProtocol(SCP01_15);
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
    }

    @Test
    public void testCENCAccept() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CENC;
        pol.checkProtocol(SCP01_05);
        pol.checkProtocol(SCP01_15);
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
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

}
