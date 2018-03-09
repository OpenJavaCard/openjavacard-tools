package better.smartcard.tool.converter;

import better.smartcard.util.HexUtil;
import com.beust.jcommander.IStringConverter;

public class BytesConverter implements IStringConverter<byte[]> {
    @Override
    public byte[] convert(String value) {
        System.out.println("BYTECONVERT: " + value);
        return HexUtil.hexToBytes(value);
    }
}
