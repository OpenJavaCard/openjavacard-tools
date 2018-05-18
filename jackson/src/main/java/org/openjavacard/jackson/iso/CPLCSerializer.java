package org.openjavacard.jackson.iso;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openjavacard.gp.protocol.GPLifeCycle;

import java.io.IOException;

public class CPLCSerializer extends StdSerializer<GPLifeCycle> {

    public CPLCSerializer() {
        super(GPLifeCycle.class);
    }

    @Override
    public void serialize(GPLifeCycle value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        for(GPLifeCycle.Field field: GPLifeCycle.Field.values()) {
            gen.writeStringField(field.name(), value.getFieldHex(field));
        }
        gen.writeEndObject();
    }
}
