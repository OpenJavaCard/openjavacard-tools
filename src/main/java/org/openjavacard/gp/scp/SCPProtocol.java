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

package org.openjavacard.gp.scp;

import org.openjavacard.util.HexUtil;
import org.openjavacard.util.VerboseString;

import javax.smartcardio.CardException;

/**
 * Represents a specific variant of an SCP protocol
 *
 * This is a base class, SCP implementations should subclass it.
 *
 * These objects are informed about the specific features of
 * their respective SCP protocol with its negotiated parameters.
 *
 * Contrary to protocol policies these actually know what level
 * of security the protocol supports.
 */
public abstract class SCPProtocol implements VerboseString {

    /**
     * Decode SCP parameters
     *
     * @param scpVersion    for decoding
     * @param scpParameters for decoding
     * @return an object describing the parameters
     */
    public static SCPProtocol decode(int scpVersion, int scpParameters) {
        // check that we actually got a specific protocol
        if(scpVersion == 0 || scpParameters == 0) {
            throw new IllegalArgumentException("SCP protocol is not specific");
        }
        // create version-dependent variants
        switch(scpVersion) {
            case 1:
            case 2:
                return new SCP0102Protocol(scpVersion, scpParameters);
            case 3:
                return new SCP03Protocol(scpParameters);
            default:
                throw new UnsupportedOperationException("Unknown protocol SCP" + HexUtil.hex8(scpVersion));
        }
    }

    /**
     * SCP protocol version
     */
    public final int scpVersion;
    /**
     * SCP parameter specification
     */
    public final int scpParameters;

    /**
     * Main constructor
     *
     * @param version
     * @param parameters
     */
    protected SCPProtocol(int version, int parameters) {
        scpVersion = version;
        scpParameters = parameters;
    }

    /**
     * Determine if the given security policy is supported
     *
     * This is version-specific because protocols express
     * support in version-dependent parameter bits and
     * have varying support for advanced features.
     *
     * @param securityPolicy to check
     */
    public abstract boolean isSecuritySupported(SCPSecurityPolicy securityPolicy) throws CardException;

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
