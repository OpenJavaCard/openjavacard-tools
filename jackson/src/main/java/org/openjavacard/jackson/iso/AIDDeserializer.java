package org.openjavacard.jackson.iso;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.openjavacard.iso.AID;

import java.io.IOException;

public class AIDDeserializer extends StdDeserializer<AID> {

    public AIDDeserializer() {
        super(AID.class);
    }

    @Override
    public AID deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            return new AID(p.getText());
        }
        return (AID) ctxt.handleUnexpectedToken(_valueClass, p);
    }

}
