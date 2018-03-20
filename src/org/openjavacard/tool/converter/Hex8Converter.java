package org.openjavacard.tool.converter;

import org.openjavacard.util.HexUtil;
import com.beust.jcommander.IStringConverter;

public class Hex8Converter implements IStringConverter<Integer> {
    @Override
    public Integer convert(String value) {
        return HexUtil.unsigned8(value);
    }
}
