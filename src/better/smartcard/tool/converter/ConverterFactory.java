package better.smartcard.tool.converter;

import better.smartcard.util.AID;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;

public class ConverterFactory implements IStringConverterFactory {
    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class forType) {
        if(forType.equals(AID.class)) {
            return AIDConverter.class;
        }
        return null;
    }
}
