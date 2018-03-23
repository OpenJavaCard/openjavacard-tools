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

import javax.smartcardio.CardException;

public class SCPProtocolPolicyTest extends TestCase {

    SCPProtocol SCP02_15 = SCPProtocol.decode(0x02, 0x15);
    SCPProtocol SCP02_55 = SCPProtocol.decode(0x02, 0x55);

    public void testPermissive() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.PERMISSIVE;
        Assert.assertTrue(pol.isVersionAllowed(0x01));
        Assert.assertTrue(pol.isVersionAllowed(0x02));
        Assert.assertTrue(pol.isVersionAllowed(0x03));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x55));
        Assert.assertTrue(pol.isProtocolAllowed(SCP02_15));
        Assert.assertTrue(pol.isProtocolAllowed(SCP02_55));
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
    }

    public void testSCP01() {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP01;
        Assert.assertTrue(pol.isVersionAllowed(0x01));
        Assert.assertFalse(pol.isVersionAllowed(0x02));
        Assert.assertFalse(pol.isVersionAllowed(0x03));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x55));
    }

    public void testSCP02() throws CardException {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP02;
        Assert.assertFalse(pol.isVersionAllowed(0x01));
        Assert.assertTrue(pol.isVersionAllowed(0x02));
        Assert.assertFalse(pol.isVersionAllowed(0x03));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertTrue(pol.isProtocolAllowed(0x02, 0x55));
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
    }

    public void testSCP03() {
        SCPProtocolPolicy pol = SCPProtocolPolicy.SCP03;
        Assert.assertFalse(pol.isVersionAllowed(0x01));
        Assert.assertFalse(pol.isVersionAllowed(0x02));
        Assert.assertTrue(pol.isVersionAllowed(0x03));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x15));
        Assert.assertFalse(pol.isProtocolAllowed(0x02, 0x55));
    }

}
