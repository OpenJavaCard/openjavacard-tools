package org.openjavacard.iso;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class CLATest {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(CLATest.class);
    }

    @Test
    public void testValid() {
        // valid interindustry values
        Assert.assertTrue(CLA.isValid((byte)0x00));
        Assert.assertTrue(CLA.isValid((byte)0x02));
        Assert.assertTrue(CLA.isValid((byte)0x04));
        Assert.assertTrue(CLA.isValid((byte)0x0A));
        Assert.assertTrue(CLA.isValid((byte)0x10));
        Assert.assertTrue(CLA.isValid((byte)0x12));
        Assert.assertTrue(CLA.isValid((byte)0x14));
        Assert.assertTrue(CLA.isValid((byte)0x1A));
        // interindustry CLA reserved range
        Assert.assertFalse(CLA.isValid((byte)0x20));
        Assert.assertFalse(CLA.isValid((byte)0x22));
        Assert.assertFalse(CLA.isValid((byte)0x2A));
        Assert.assertFalse(CLA.isValid((byte)0x30));
        Assert.assertFalse(CLA.isValid((byte)0x32));
        Assert.assertFalse(CLA.isValid((byte)0x3A));
        // valid interindustry values
        Assert.assertTrue(CLA.isValid((byte)0x40));
        Assert.assertTrue(CLA.isValid((byte)0x42));
        Assert.assertTrue(CLA.isValid((byte)0x44));
        Assert.assertTrue(CLA.isValid((byte)0x4A));
        Assert.assertTrue(CLA.isValid((byte)0x50));
        Assert.assertTrue(CLA.isValid((byte)0x52));
        Assert.assertTrue(CLA.isValid((byte)0x54));
        Assert.assertTrue(CLA.isValid((byte)0x5A));
        Assert.assertTrue(CLA.isValid((byte)0x60));
        Assert.assertTrue(CLA.isValid((byte)0x62));
        Assert.assertTrue(CLA.isValid((byte)0x64));
        Assert.assertTrue(CLA.isValid((byte)0x6A));
        Assert.assertTrue(CLA.isValid((byte)0x70));
        Assert.assertTrue(CLA.isValid((byte)0x72));
        Assert.assertTrue(CLA.isValid((byte)0x74));
        Assert.assertTrue(CLA.isValid((byte)0x7A));
        // proprietary CLA is always valid
        Assert.assertTrue(CLA.isValid((byte)0x80));
        Assert.assertTrue(CLA.isValid((byte)0x82));
        Assert.assertTrue(CLA.isValid((byte)0x8A));
        Assert.assertTrue(CLA.isValid((byte)0x90));
        Assert.assertTrue(CLA.isValid((byte)0x92));
        Assert.assertTrue(CLA.isValid((byte)0x9A));
        Assert.assertTrue(CLA.isValid((byte)0xA0));
        Assert.assertTrue(CLA.isValid((byte)0xA2));
        Assert.assertTrue(CLA.isValid((byte)0xAA));
    }

    @Test
    public void testInterindustry() {
        // test all possible values
        for(int i = 0; i < 256; i++) {
            byte cla = (byte)i;
            if(i < 0x80) {
                // values below 0x80 are interindustry
                Assert.assertTrue(CLA.isInterindustry(cla));
                Assert.assertFalse(CLA.isProprietary(cla));
            } else {
                // values above 0x80 are proprietary
                Assert.assertFalse(CLA.isInterindustry(cla));
                Assert.assertTrue(CLA.isProprietary(cla));
            }
        }
    }

    @Test
    public void testChaining() {
        // valid interindustry values
        Assert.assertFalse(CLA.isChaining((byte)0x00));
        Assert.assertFalse(CLA.isChaining((byte)0x02));
        Assert.assertFalse(CLA.isChaining((byte)0x04));
        Assert.assertFalse(CLA.isChaining((byte)0x0A));
        Assert.assertTrue(CLA.isChaining((byte)0x10));
        Assert.assertTrue(CLA.isChaining((byte)0x12));
        Assert.assertTrue(CLA.isChaining((byte)0x14));
        Assert.assertTrue(CLA.isChaining((byte)0x1A));
        // interindustry CLA reserved range
        Assert.assertFalse(CLA.isChaining((byte)0x20));
        Assert.assertFalse(CLA.isChaining((byte)0x22));
        Assert.assertFalse(CLA.isChaining((byte)0x2A));
        Assert.assertTrue(CLA.isChaining((byte)0x30));
        Assert.assertTrue(CLA.isChaining((byte)0x32));
        Assert.assertTrue(CLA.isChaining((byte)0x3A));
        // valid interindustry values
        Assert.assertFalse(CLA.isChaining((byte)0x40));
        Assert.assertFalse(CLA.isChaining((byte)0x42));
        Assert.assertFalse(CLA.isChaining((byte)0x44));
        Assert.assertFalse(CLA.isChaining((byte)0x4A));
        Assert.assertTrue(CLA.isChaining((byte)0x50));
        Assert.assertTrue(CLA.isChaining((byte)0x52));
        Assert.assertTrue(CLA.isChaining((byte)0x54));
        Assert.assertTrue(CLA.isChaining((byte)0x5A));
        Assert.assertFalse(CLA.isChaining((byte)0x60));
        Assert.assertFalse(CLA.isChaining((byte)0x62));
        Assert.assertFalse(CLA.isChaining((byte)0x64));
        Assert.assertFalse(CLA.isChaining((byte)0x6A));
        Assert.assertTrue(CLA.isChaining((byte)0x70));
        Assert.assertTrue(CLA.isChaining((byte)0x72));
        Assert.assertTrue(CLA.isChaining((byte)0x74));
        Assert.assertTrue(CLA.isChaining((byte)0x7A));
        // proprietary CLA is always valid
        Assert.assertFalse(CLA.isChaining((byte)0x80));
        Assert.assertFalse(CLA.isChaining((byte)0x82));
        Assert.assertFalse(CLA.isChaining((byte)0x8A));
        Assert.assertTrue(CLA.isChaining((byte)0x90));
        Assert.assertTrue(CLA.isChaining((byte)0x92));
        Assert.assertTrue(CLA.isChaining((byte)0x9A));
        Assert.assertFalse(CLA.isChaining((byte)0xA0));
        Assert.assertFalse(CLA.isChaining((byte)0xA2));
        Assert.assertFalse(CLA.isChaining((byte)0xAA));
    }

}
