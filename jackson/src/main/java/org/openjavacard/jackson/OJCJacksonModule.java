package org.openjavacard.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.openjavacard.gp.scp.SCPParameters;
import org.openjavacard.iso.AID;
import org.openjavacard.jackson.gp.SCPParametersDeserializer;
import org.openjavacard.jackson.gp.SCPParametersSerializer;
import org.openjavacard.jackson.iso.AIDDeserializer;
import org.openjavacard.jackson.iso.AIDSerializer;

public class OJCJacksonModule extends SimpleModule {

    public OJCJacksonModule() {
        addSerializer(AID.class, new AIDSerializer());
        addDeserializer(AID.class, new AIDDeserializer());
        addSerializer(SCPParameters.class, new SCPParametersSerializer());
        addDeserializer(SCPParameters.class, new SCPParametersDeserializer());
    }

}
