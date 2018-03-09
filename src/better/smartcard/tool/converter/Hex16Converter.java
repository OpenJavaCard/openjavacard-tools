package better.smartcard.tool.converter;

import better.smartcard.util.HexUtil;
import com.beust.jcommander.IStringConverter;

public class Hex16Converter implements IStringConverter<Integer> {
    @Override
    public Integer convert(String value) {
        return HexUtil.unsigned16(value);
    }
}