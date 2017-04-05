package better.smartcard.util;

public class ArrayUtil {

    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] s = new byte[a.length + b.length];
        System.arraycopy(a, 0, s, 0, a.length);
        System.arraycopy(b, 0, s, a.length, b.length);
        return s;
    }

    public static boolean startsWith(byte[] that, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (that[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

}
