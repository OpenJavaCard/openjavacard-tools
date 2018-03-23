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

public class SCP03Protocol extends SCPProtocol {

    public final boolean pseudoRandomChallenge;

    public final boolean rmacSupport;

    public final boolean rencSupport;

    /**
     * Main constructor
     *
     * @param parameters
     */
    protected SCP03Protocol(int parameters) {
        super(3, parameters);

        pseudoRandomChallenge = ((parameters & 0x10) != 0);
        rmacSupport = ((parameters & 0x20) != 0);
        rencSupport = ((parameters & 0x40) != 0);
    }

    @Override
    public boolean isSecuritySupported(SCPSecurityPolicy securityPolicy) {
        /* CMAC and CENC are always supported in SCP03 */
        if(securityPolicy.requireRMAC && !rmacSupport) {
                return false;
        }
        if(securityPolicy.requireRENC && !rencSupport) {
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
            if (pseudoRandomChallenge) {
                sb.append("\n  Pseudo-random challenge");
            } else {
                sb.append("\n  Random challenge");
            }
            if (rmacSupport) {
                sb.append("\n  RMAC support");
            } else {
                sb.append("\n  no RMAC support");
            }
            if (rencSupport) {
                sb.append("\n  RENC support");
            } else {
                sb.append("\n  no RENC support");
            }
            return sb.toString();
        } else {
            return shortString;
        }
    }

}
