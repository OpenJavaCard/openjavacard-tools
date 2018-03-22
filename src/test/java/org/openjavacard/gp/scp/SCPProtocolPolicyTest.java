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
