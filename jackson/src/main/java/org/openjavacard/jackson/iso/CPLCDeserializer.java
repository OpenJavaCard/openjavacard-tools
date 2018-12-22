/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.openjavacard.jackson.iso;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import org.openjavacard.emv.CPLC;
import org.openjavacard.util.HexUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPLCDeserializer extends StdNodeBasedDeserializer<CPLC> {

    public CPLCDeserializer() {
        super(CPLC.class);
    }

    @Override
    public CPLC convert(JsonNode root, DeserializationContext ctxt)
            throws IOException {
        Map<CPLC.Field, byte[]> values = new HashMap<>();
        for(CPLC.Field field: CPLC.Field.values()) {
            JsonNode fieldNode = root.get(field.name());
            byte[] value = HexUtil.hexToBytes(fieldNode.asText());
            values.put(field, value);
        }
        return new CPLC(values);
    }

}
