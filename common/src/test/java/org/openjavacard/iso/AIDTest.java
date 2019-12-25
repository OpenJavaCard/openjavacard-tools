package org.openjavacard.iso;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class AIDTest {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(AIDTest.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorShortBytes() {
        new AID(new byte[]{(byte)0xA0, 0x00, 0x00});
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorShortString() {
        new AID("A00000");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLongBytes() {
        new AID(new byte[]{(byte)0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLongString() {
        new AID("A000000000000000000000000000000000");
    }

}
