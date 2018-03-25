package better.smartcard.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class HexUtil {

    public static String hex8(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException();
        }
        return bytesToHex(new byte[]{
                (byte) (value & 0xFF)
        });
    }

    public static String hex16(short value) {
        return hex16(value & 0xFFFF);
    }

    public static String hex16(int value) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException();
        }
        return bytesToHex(new byte[]{
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 0) & 0xFF)
        });
    }

    public static String hex24(int value) {
        if (value < 0 || value > ((1 << 24) - 1)) {
            throw new IllegalArgumentException();
        }
        return bytesToHex(new byte[]{
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 0) & 0xFF)
        });
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "(null)";
        } else if (bytes.length == 0) {
            return "(empty)";
        } else {
            return Hex.encodeHexString(bytes);
        }
    }

    public static byte[] hexToBytes(String hex) {
        byte[] result = null;
        if (hex != null) {
            try {
                result = Hex.decodeHex(hex.toCharArray());
            } catch (DecoderException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return result;
    }

}
