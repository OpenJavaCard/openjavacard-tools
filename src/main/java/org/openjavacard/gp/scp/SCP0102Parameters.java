/*
 * openjavacard-tools: OpenJavaCard Development Tools
 * Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de
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
 *
 */

package org.openjavacard.gp.scp;

import org.openjavacard.util.HexUtil;

/**
 * Protocol parameters for SCP01 and SCP02
 */
public class SCP0102Parameters extends SCPParameters {

    /** Number of keys required (always 1 or 3) */
    public final int numKeys;
    /** If true then C-MAC is computed on the unmodified CardAPDU (original CLA, INS, length) */
    public final boolean cmacUnmodified;
    /** If true then initialization must be explicit */
    public final boolean initExplicit;
    /** If true then the ICV (initial IV) is computed as MAC(AID), else it is MAC(ZEROBLOCK) */
    public final boolean icvMACAID;
    /** If true then the ICV goes through an additional encryption step */
    public final boolean icvEncrypt;
    /** True if RMAC is supported */
    public final boolean rmacSupport;
    /** If true then card challenges are pseudo-random and predictable */
    public final boolean wellKnown;

    /**
     * Parse the given protocol parameters
     * @param protocol to be parsed
     * @param parameters to be parsed
     */
    SCP0102Parameters(int protocol, int parameters) {
        super(protocol, parameters);

        switch (protocol) {
            case 1:
                numKeys = 3;
                cmacUnmodified = true;
                initExplicit = true;
                icvMACAID = false;
                rmacSupport = false;
                wellKnown = false;
                switch (parameters) {
                    case 0x05:
                        icvEncrypt = false;
                        break;
                    case 0x15:
                        icvEncrypt = true;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Unknown SCP01 variant " + HexUtil.hex8(parameters));
                }
                break;
            case 2:
                numKeys = ((parameters & 0x01) != 0) ? 3 : 1;
                cmacUnmodified = (parameters & 0x02) != 0;
                initExplicit = (parameters & 0x04) != 0;
                icvMACAID = (parameters & 0x08) != 0;
                icvEncrypt = (parameters & 0x10) != 0;
                rmacSupport = (parameters & 0x20) != 0;
                wellKnown = (parameters & 0x40) != 0;
                if ((parameters & 0x80) != 0) {
                    throw new IllegalArgumentException("Unknown SCP02 parameter 80");
                }
                break;
            default:
                throw new IllegalArgumentException("Got protocol SCP" + HexUtil.hex8(protocol) + ", need SCP01 or SCP02");
        }
    }

    @Override
    public boolean isSecuritySupported(SCPSecurityPolicy securityPolicy) {
        /* CMAC and CENC are always supported in SCP01/02 */
        if(securityPolicy.requireRMAC && !rmacSupport) {
            return false;
        }
        if(securityPolicy.requireRENC) {
            return false;
        }
        return true;
    }

    @Override
    public String toVerboseString() {
        String shortString = toString();
        if (scpParameters != 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Secure Channel Protocol " + shortString + ":");
            sb.append("\n  " + numKeys + " keys used");
            if (cmacUnmodified) {
                sb.append("\n  CMAC unmodified CardAPDU");
            } else {
                sb.append("\n  CMAC modified CardAPDU");
            }
            if (initExplicit) {
                sb.append("\n  explicit initiation");
            } else {
                sb.append("\n  implicit initiation");
            }
            if (icvMACAID) {
                sb.append("\n  ICV initialized to MAC(AID)");
            } else {
                sb.append("\n  ICV initialized to null");
            }
            if (icvEncrypt) {
                sb.append("\n  ICV encryption");
            } else {
                sb.append("\n  no ICV encryption");
            }
            if (rmacSupport) {
                sb.append("\n  RMAC support");
            } else {
                sb.append("\n  no RMAC support");
            }
            if (wellKnown) {
                sb.append("\n  Well-known challenge generator");
            }
            return sb.toString();
        } else {
            return shortString;
        }
    }

}
