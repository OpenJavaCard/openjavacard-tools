package org.openjavacard.jackson.iso;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import org.openjavacard.gp.protocol.GPLifeCycle;
import org.openjavacard.util.HexUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPLCDeserializer extends StdNodeBasedDeserializer<GPLifeCycle> {

    public CPLCDeserializer() {
        super(GPLifeCycle.class);
    }

    @Override
    public GPLifeCycle convert(JsonNode root, DeserializationContext ctxt)
            throws IOException {
        Map<GPLifeCycle.Field, byte[]> values = new HashMap<>();
        for(GPLifeCycle.Field field: GPLifeCycle.Field.values()) {
            JsonNode fieldNode = root.get(field.name());
            byte[] value = HexUtil.hexToBytes(fieldNode.asText());
            values.put(field, value);
        }
        return new GPLifeCycle(values);
    }

}
