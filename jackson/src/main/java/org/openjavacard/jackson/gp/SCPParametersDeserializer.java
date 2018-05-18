package org.openjavacard.jackson.gp;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.openjavacard.gp.scp.SCPParameters;

import java.io.IOException;

public class SCPParametersDeserializer extends StdDeserializer {

    public SCPParametersDeserializer() {
        super(SCPParameters.class);
    }

    @Override
    public SCPParameters deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            return SCPParameters.fromString(p.getText());
        }
        return (SCPParameters) ctxt.handleUnexpectedToken(_valueClass, p);
    }

}
