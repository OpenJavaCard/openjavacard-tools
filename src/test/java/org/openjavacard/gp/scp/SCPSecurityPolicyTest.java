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

import javax.smartcardio.CardException;

public class SCPSecurityPolicyTest extends TestCase {

    SCPProtocol SCP02_15 = SCPProtocol.decode(0x02, 0x15);
    SCPProtocol SCP02_55 = SCPProtocol.decode(0x02, 0x55);

    public void testCMAC() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CMAC;
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
    }

    public void testCENC() throws CardException {
        SCPSecurityPolicy pol = SCPSecurityPolicy.CENC;
        pol.checkProtocol(SCP02_15);
        pol.checkProtocol(SCP02_55);
    }

}
