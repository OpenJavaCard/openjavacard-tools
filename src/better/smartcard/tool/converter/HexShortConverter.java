package better.smartcard.tool.converter;

import better.smartcard.util.HexUtil;
import com.beust.jcommander.IStringConverter;

public class HexShortConverter implements IStringConverter<Short> {
    @Override
    public Short convert(String value) {
        return HexUtil.short16(value);
    }
}
