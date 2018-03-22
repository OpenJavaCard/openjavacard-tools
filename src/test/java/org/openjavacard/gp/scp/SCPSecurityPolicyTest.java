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
