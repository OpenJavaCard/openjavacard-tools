package org.openjavacard.gp.keys;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.gp.protocol.GPCardDataTest;
import org.openjavacard.gp.scp.SCP03Derivation;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class GPKeySetTest extends TestCase {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(GPCardDataTest.class);
    }

    @Test
    public void testDeriveSCP02_0000() {
        GPKeySet derived = GPKeySet.GLOBALPLATFORM.deriveSCP02(HexUtil.hexToBytes("0000"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByType(GPKeyType.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("010b0371d78377b801f2d62afc671d95"), encKey.getSecret());
        GPKey macKey = derived.getKeyByType(GPKeyType.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("d1c28c601652a4770d67ad82d2d2e1c4"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByType(GPKeyType.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("e11987ee331b417a5d67d760692f89d4"), kekKey.getSecret());
    }

    @Test
    public void testDeriveSCP02_0001() {
        GPKeySet derived = GPKeySet.GLOBALPLATFORM.deriveSCP02(HexUtil.hexToBytes("0001"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByType(GPKeyType.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("25c9794a1205ff244f5fa0378d2f8d59"), encKey.getSecret());
        GPKey macKey = derived.getKeyByType(GPKeyType.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("9bed98891580c3b245fe9ec58bfa8d2a"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByType(GPKeyType.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("0e51fdf196141f227a57bd154012fd39"), kekKey.getSecret());
    }

    @Test
    public void testDeriveSCP02_007F() {
        GPKeySet derived = GPKeySet.GLOBALPLATFORM.deriveSCP02(HexUtil.hexToBytes("007F"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByType(GPKeyType.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("f463d35410fd6ed925c7b07b333cfaf9"), encKey.getSecret());
        GPKey macKey = derived.getKeyByType(GPKeyType.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("161c8aa468e658c45ae3eca8387dfb19"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByType(GPKeyType.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("369a67c04590b93101ca413613db706a"), kekKey.getSecret());
    }

    @Test
    public void testDeriveSCP02_FFFF() {
        GPKeySet derived = GPKeySet.GLOBALPLATFORM.deriveSCP02(HexUtil.hexToBytes("FFFF"));
        Assert.assertEquals(0, derived.getKeyVersion());
        GPKey encKey = derived.getKeyByType(GPKeyType.ENC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("e75adaaf916de18a01ab9acc6ef84b8b"), encKey.getSecret());
        GPKey macKey = derived.getKeyByType(GPKeyType.MAC);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("66f6b778a1fafccc9de6c253233be33d"), macKey.getSecret());
        GPKey kekKey = derived.getKeyByType(GPKeyType.KEK);
        Assert.assertEquals(GPKeyCipher.DES3, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("3c750e0f60899ffc8f0d26d860ae6c95"), kekKey.getSecret());
    }

    @Test
    public void testDeriveSCP03() {
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
