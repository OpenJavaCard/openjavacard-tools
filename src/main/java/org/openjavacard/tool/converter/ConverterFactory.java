/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.tool.converter;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import org.openjavacard.iso.AID;

public class ConverterFactory implements IStringConverterFactory {
    @Override
    public Class<? extends IStringConverter<?>> getConverter(Class forType) {
        if(forType.equals(AID.class)) {
            return AIDConverter.class;
        }
        if(forType.equals(byte[].class)) {
            return BytesConverter.class;
        }
        return null;
    }
}
