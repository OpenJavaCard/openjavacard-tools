package org.openjavacard.jackson.generic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.openjavacard.util.HexUtil;

import java.io.IOException;

public class HexBytesDeserializer extends StdDeserializer<byte[]> {

    public HexBytesDeserializer() {
        super(byte[].class);
    }

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            return HexUtil.hexToBytes(p.getText());
        }
        return (byte[]) ctxt.handleUnexpectedToken(_valueClass, p);
    }

}
