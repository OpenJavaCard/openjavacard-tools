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
