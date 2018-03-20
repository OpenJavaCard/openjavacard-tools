package org.openjavacard.tool.converter;

import org.openjavacard.util.HexUtil;
import com.beust.jcommander.IStringConverter;

public class BytesConverter implements IStringConverter<byte[]> {
    @Override
    public byte[] convert(String value) {
        return HexUtil.hexToBytes(value);
    }
}
