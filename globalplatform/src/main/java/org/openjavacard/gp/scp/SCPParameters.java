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

package org.openjavacard.gp.scp;

import org.openjavacard.util.HexUtil;
import org.openjavacard.util.VerboseString;

/**
 * Represents a parameterized SCP protocol
 * <p/>
 * This is a base class, SCP implementations should subclass it.
 * <p/>
 * These objects are informed about the specific features of
 * their respective SCP protocol with its negotiated parameters.
 * <p/>
 * Contrary to protocol policies these actually know what level
 * of security the protocol supports.
 */
public abstract class SCPParameters implements VerboseString {

    /**
     * Decode SCP parameters
     *
     * @param scpVersion    for decoding
     * @param scpParameters for decoding
     * @return an object describing the parameters
     */
    public static SCPParameters decode(int scpVersion, int scpParameters) {
        // create version-dependent variants
        switch(scpVersion) {
            case 0:
                return new SCP00Parameters(scpParameters);
            case 1:
            case 2:
                return new SCP0102Parameters(scpVersion, scpParameters);
            case 3:
                return new SCP03Parameters(scpParameters);
            default:
                throw new IllegalArgumentException("Unknown protocol SCP" + HexUtil.hex8(scpVersion));
        }
    }

    /**
     * Parse an SCP protocol specification
     * @param scpString to parse
     * @return corresponding protocol
     */
    public static SCPParameters fromString(String scpString) {
        if(!scpString.startsWith("SCP")) {
            throw new IllegalArgumentException("Illegal SCP protocol specifier \"" + scpString + "\"");
        }
        if(scpString.length() != 8) {
            throw new IllegalArgumentException("Illegal SCP protocol specifier \"" + scpString + "\"");
        }
        if(scpString.charAt(5) != '-') {
            throw new IllegalArgumentException("Illegal SCP protocol specifier \"" + scpString + "\"");
        }
        String version = scpString.substring(3, 5);
        String parameters = scpString.substring(6, 8);
        return decode(HexUtil.unsigned8(version), HexUtil.unsigned8(parameters));
    }

    /** SCP protocol version */
    public final int scpVersion;
    /** SCP parameter specification */
    public final int scpParameters;

    /**
     * Main constructor
     *
     * @param version
     * @param parameters
     */
    protected SCPParameters(int version, int parameters) {
        scpVersion = version;
        scpParameters = parameters;
    }

    /**
     * Determine if the given security policy is supported
     * <p/>
     * This is version-specific because protocols express
     * support in version-dependent parameter bits and
     * have varying support for advanced features.
     * <p/>
     * @param securityPolicy to check
     */
    public abstract boolean isSecuritySupported(SCPSecurityPolicy securityPolicy);

    /**
     * Stringify the parameters
     */
    public String toString() {
        String protocol = scpVersion == 0 ? "?" : HexUtil.hex8(scpVersion);
        String parameters = scpParameters == 0 ? "" : ("-" + HexUtil.hex8(scpParameters));
        return "SCP" + protocol + parameters;
    }

    /**
     * Stringify the parameters verbosely
     */
    @Override
    public String toVerboseString() {
        return "Protocol " + toString();
    }

}
