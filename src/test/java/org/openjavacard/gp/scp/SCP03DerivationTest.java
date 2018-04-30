package org.openjavacard.gp.scp;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyType;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class SCP03DerivationTest extends TestCase {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SCP03DerivationTest.class);
    }

    @Test
    public void testDeriveSCP03_000010() {
        byte[] cardSequence = HexUtil.hexToBytes("000010");
        byte[] hostChallenge = HexUtil.hexToBytes("A7F76C713F0A713D");
        byte[] cardChallenge = HexUtil.hexToBytes("31900058C1C451A2");
        GPKeySet derived = SCP03Derivation.deriveSessionKeys(GPKeySet.GLOBALPLATFORM, cardSequence, hostChallenge, cardChallenge);
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByType(GPKeyType.ENC);
        Assert.assertEquals(GPKeyCipher.AES, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("258a78866f41482bef482dc8ca976ccd"), encKey.getSecret());
        GPKey macKey = derived.getKeyByType(GPKeyType.MAC);
        Assert.assertEquals(GPKeyCipher.AES, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("053db6abc7fdf3b63a0d965ee16b0255"), macKey.getSecret());
        GPKey rmacKey = derived.getKeyByType(GPKeyType.RMAC);
        Assert.assertEquals(GPKeyCipher.AES, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("eda0b4f2ec0345bfc50f3bc59cfef936"), rmacKey.getSecret());
    }

}
