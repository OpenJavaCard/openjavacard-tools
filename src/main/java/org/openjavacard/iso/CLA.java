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

package org.openjavacard.iso;

/**
 * Constants and helpers related to ISO7816 command CLA fields
 */
public class CLA {

    /** Mask for the proprietary command flag */
    private static final byte DOMAIN_MASK        = (byte)0x80;
    /** Domain value for ISO-specified commands */
    private static final byte DOMAIN_ISO         = (byte)0x00;
    /** Domain value for proprietary commands */
    private static final byte DOMAIN_PROPRIETARY = (byte)0x80;

    /** Mask for the secure messaging field */
    private static final byte SM_MASK        = (byte)0x0C;
    /** Value indicating no secure messaging */
    private static final byte SM_NONE        = (byte)0x00;
    /** Value indicating proprietary secure messaging */
    private static final byte SM_PROPRIETARY = (byte)0x04;
    /** Value indicating ISO secure messaging for data only */
    private static final byte SM_ISO_DATA    = (byte)0x08;
    /** Value indicating ISO secure messaging for everything */
    private static final byte SM_ISO_FULL    = (byte)0x0C;

    /**
     * @param cla to check
     * @return true if the CLA is for an ISO-defined command
     */
    public static boolean isInterindustry(byte cla) {
        return (cla & DOMAIN_MASK) == DOMAIN_ISO;
    }

    /**
     * @param cla to check
     * @return true if the CLA is for a proprietary command
     */
    public static boolean isProprietary(byte cla) {
        return (cla & DOMAIN_MASK) == DOMAIN_PROPRIETARY;
    }

    /**
     * @param cla to check
     * @return true if the CLA indicates secure messaging
     */
    public static boolean isSecureMessaging(byte cla) {
        return (cla & SM_MASK) != SM_NONE;
    }

}
