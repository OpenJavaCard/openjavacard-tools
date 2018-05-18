package org.openjavacard.jackson.gp;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openjavacard.gp.scp.SCPParameters;

import java.io.IOException;

public class SCPParametersSerializer extends StdSerializer<SCPParameters> {

    public SCPParametersSerializer() {
        super(SCPParameters.class);
    }

    @Override
    public void serialize(SCPParameters value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }

}
