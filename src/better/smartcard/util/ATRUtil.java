package better.smartcard.util;

import javax.smartcardio.ATR;

/**
 * Utilities related to card ATRs
 */
public class ATRUtil {

    public static String toString(ATR atr) {
        return HexUtil.bytesToHex(atr.getBytes());
    }

}
