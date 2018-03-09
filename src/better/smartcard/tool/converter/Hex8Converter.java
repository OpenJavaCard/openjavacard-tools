package better.smartcard.tool.converter;

import better.smartcard.util.HexUtil;
import com.beust.jcommander.IStringConverter;

public class Hex8Converter implements IStringConverter<Integer> {
    @Override
    public Integer convert(String value) {
        return HexUtil.unsigned8(value);
    }
}
