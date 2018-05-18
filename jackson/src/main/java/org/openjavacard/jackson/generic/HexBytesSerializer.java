package org.openjavacard.jackson.generic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openjavacard.util.HexUtil;

import java.io.IOException;

public class HexBytesSerializer extends StdSerializer<byte[]> {

    public HexBytesSerializer() {
        super(byte[].class);
    }

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(HexUtil.bytesToHex(value));
    }

}
