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
