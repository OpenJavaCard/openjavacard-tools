package org.openjavacard.gp.keys;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class GPKeySetTest extends TestCase {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(GPKeySetTest.class);
    }

    @Test
    public void testDiversifyEMV_00000000000000000000() {
        GPKeySet diversified = GPKeySet.GLOBALPLATFORM.diversify(GPKeyDiversification.EMV,
                HexUtil.hexToBytes("00000000000000000000"));
        Assert.assertEquals(0, diversified.getKeyVersion());
        GPKey encKey = diversified.getKeyByType(GPKeyType.ENC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("5d9a62e01df1fa7a93f231000f4ac272"), encKey.getSecret());
        GPKey macKey = diversified.getKeyByType(GPKeyType.MAC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("13179f2220fc6bce5e740387eb78db5c"), macKey.getSecret());
        GPKey kekKey = diversified.getKeyByType(GPKeyType.KEK);
        Assert.assertArrayEquals(HexUtil.hexToBytes("8bbe5457944d18df460af68730f570a2"), kekKey.getSecret());
    }

    @Test
    public void testDiversifyEMV_0102030405060708090A() {
        GPKeySet diversified = GPKeySet.GLOBALPLATFORM.diversify(GPKeyDiversification.EMV,
                HexUtil.hexToBytes("0102030405060708090A"));
        Assert.assertEquals(0, diversified.getKeyVersion());
        GPKey encKey = diversified.getKeyByType(GPKeyType.ENC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("6438a05ac377df302423bc5e2038fb5b"), encKey.getSecret());
        GPKey macKey = diversified.getKeyByType(GPKeyType.MAC);
        Assert.assertArrayEquals(HexUtil.hexToBytes("5a9402ddabc5b9497bbece0884929b83"), macKey.getSecret());
        GPKey kekKey = diversified.getKeyByType(GPKeyType.KEK);
        Assert.assertArrayEquals(HexUtil.hexToBytes("acb4c129b3912fb782a4eaee043bae95"), kekKey.getSecret());
    }

}
