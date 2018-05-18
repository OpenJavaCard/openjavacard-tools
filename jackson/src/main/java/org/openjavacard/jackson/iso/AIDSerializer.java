package org.openjavacard.jackson.iso;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openjavacard.iso.AID;

import java.io.IOException;

public class AIDSerializer extends StdSerializer<AID> {

    public AIDSerializer() {
        super(AID.class);
    }

    @Override
    public void serialize(AID value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }

}
