package org.openjavacard.tool.converter;

import org.openjavacard.util.HexUtil;
import com.beust.jcommander.IStringConverter;

public class HexByteConverter implements IStringConverter<Byte> {
    @Override
    public Byte convert(String value) {
        return HexUtil.byte8(value);
    }
}
