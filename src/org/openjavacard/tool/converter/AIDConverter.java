package org.openjavacard.tool.converter;

import org.openjavacard.iso.AID;
import com.beust.jcommander.IStringConverter;

public class AIDConverter implements IStringConverter<AID> {
    @Override
    public AID convert(String value) {
        return new AID(value);
    }
}
