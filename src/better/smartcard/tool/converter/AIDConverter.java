package better.smartcard.tool.converter;

import better.smartcard.util.AID;
import com.beust.jcommander.IStringConverter;

public class AIDConverter implements IStringConverter<AID> {
    @Override
    public AID convert(String value) {
        return new AID(value);
    }
}
