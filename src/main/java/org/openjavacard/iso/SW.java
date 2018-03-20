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

package org.openjavacard.iso;

import org.openjavacard.util.HexUtil;

public class SW {

    public static boolean isResponseAvailable(int sw) {
        return sw >= 0x6100 && sw <= 0x61FF;
    }

    public static boolean isCorrectLength(int sw) {
        return sw >= 0x6C00 && sw <= 0x6CFF;
    }

    public static boolean isWarningUnchanged(int sw) {
        return (sw >= 0x6200 && sw <= 0x62FF);
    }

    public static boolean isWarningChanged(int sw) {
        return (sw >= 0x6300 && sw <= 0x63FF);
    }

    public static boolean isWarning(int sw) {
        return isWarningUnchanged(sw)
                || isWarningChanged(sw);
    }

    public static String toString(int sw) {
        StringBuffer sb = new StringBuffer();
        SWInfo entry = SWInfo.get(sw);
        sb.append("SW=");
        sb.append(HexUtil.hex16(sw));
        sb.append(" [");
        if (entry != null) {
            if (isWarning(sw)) {
                sb.append("warning: ");
            }
            sb.append(entry.name);
            if (isWarningChanged(sw)) {
                sb.append(" (state changed)");
            } else if (isWarningUnchanged(sw)) {
                sb.append(" (state unchanged)");
            }
        } else {
            if (isResponseAvailable(sw)) {
                int available = sw & 0xFF;
                sb.append(available + " bytes of data available");
            } else if (isCorrectLength(sw)) {
                int correctLen = sw & 0xFF;
                sb.append("wrong length - expected " + correctLen);
            } else if (isWarning(sw)) {
                sb.append("unknown warning " + HexUtil.hex16(sw));
                if (isWarningChanged(sw)) {
                    sb.append(" (state changed)");
                } else if (isWarningUnchanged(sw)) {
                    sb.append(" (state unchanged)");
                }
            } else {
                return "unknown error " + HexUtil.hex16(sw);
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
