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

package org.openjavacard.iso;

/**
 * Constants and helpers related to ISO7816 command CLA fields
 * <p/>
 */
public class CLA {

    /** Mask for the proprietary command flag */
    private static final byte DOMAIN_MASK        = (byte)0x80;
    /** Domain value for ISO-specified commands */
    private static final byte DOMAIN_ISO         = (byte)0x00;
    /** Domain value for proprietary commands */
    private static final byte DOMAIN_PROPRIETARY = (byte)0x80;

    /** Mask for the chaining flag */
    private static final byte CHAINING_MASK      = (byte)0x10;
    /** Flag indicating command chaining */
    private static final byte CHAINING_FLAG      = (byte)0x10;

    /** Mask for identifying first encoding */
    private static final byte FIRST_MASK        = (byte)0x60;
    /** Match value for identifying first encoding */
    private static final byte FIRST_MATCH       = (byte)0x00;

    /** Mask for identifying further encoding */
    private static final byte FURTHER_MASK      = (byte)0x40;
    /** Mask for identifying further encoding */
    private static final byte FURTHER_MATCH     = (byte)0x40;

    /** Mask for the secure messaging field */
    private static final byte FIRST_SM_MASK = (byte)0x0C;
    /** Value indicating no secure messaging indication */
    private static final byte FIRST_SM_NONE = (byte)0x00;
    /** Value indicating proprietary secure messaging */
    private static final byte FIRST_SM_PROPRIETARY = (byte)0x04;
    /** Value indicating ISO secure messaging for data only */
    private static final byte FIRST_SM_ISO_DATA = (byte)0x08;
    /** Value indicating ISO secure messaging for everything */
    private static final byte FIRST_SM_ISO_FULL = (byte)0x0C;

    /** Mask for the channel number field */
    private static final byte FIRST_CHANNEL_MASK = (byte)0x03;

    /** Mask for secure messaging field */
    private static final byte FURTHER_SM_MASK     = (byte)0x20;
    /** Value indicating no secure messaging indication */
    private static final byte FURTHER_SM_NONE     = (byte)0x00;
    /** Value indicating ISO secure messaging */
    private static final byte FURTHER_SM_ISO      = (byte)0x20;

    /** Mask for the channel number field */
    private static final byte FURTHER_CHANNEL_MASK = (byte)0x0F;
    /** Base channel for the channel number field */
    private static final byte FURTHER_CHANNEL_BASE = 4;

    /**
     * Check if a CLA byte is of the first form
     * @param cla to check
     * @return true if the CLA is of the first form
     */
    public static boolean isFirstForm(byte cla) {
        return (cla & FIRST_MASK) == FIRST_MATCH;
    }

    /**
     * Check if a CLA byte is of the further form
     * @param cla to check
     * @return true if the CLA is of the further form
     */
    public static boolean isFurtherForm(byte cla) {
        return (cla & FURTHER_MASK) == FURTHER_MATCH;
    }

    /**
     * Check if a CLA byte is valid
     * @param cla to check
     * @return true if the CLA is valid
     */
    public static boolean isValid(byte cla) {
        return isProprietary(cla)
                || isFirstForm(cla)
                || isFurtherForm(cla);
    }

    /**
     * @param cla to check
     * @return true if the CLA indicates an interindustry command
     */
    public static boolean isInterindustry(byte cla) {
        return (cla & DOMAIN_MASK) == DOMAIN_ISO;
    }

    /**
     * @param cla to check
     * @return true if the CLA indicates a proprietary command
     */
    public static boolean isProprietary(byte cla) {
        return (cla & DOMAIN_MASK) == DOMAIN_PROPRIETARY;
    }

    /**
     * @param cla to check
     * @return true if the CLA indicates chaining
     */
    public static boolean isChaining(byte cla) {
        return (cla & CHAINING_MASK) == CHAINING_FLAG;
    }

    /**
     * @param cla to check
     * @return true if the CLA indicates secure messaging
     */
    public static boolean isSecureMessaging(byte cla) {
        if(isFirstForm(cla)) {
            return (cla & FIRST_SM_MASK) != FIRST_SM_NONE;
        } else if(isFurtherForm(cla)) {
            return (cla & FURTHER_SM_MASK) != FURTHER_SM_NONE;
        } else {
            throw new IllegalArgumentException("Invalid CLA");
        }
    }

    /**
     * Determine logical channel indicated by a CLA byte
     * @param cla to check
     * @return logical channel number
     */
    public static int getLogicalChannel(byte cla) {
        if(isFirstForm(cla)) {
            return cla & FIRST_CHANNEL_MASK;
        } else if(isFurtherForm(cla)) {
            return (cla & FURTHER_CHANNEL_MASK) + FURTHER_CHANNEL_BASE;
        } else {
            throw new IllegalArgumentException("Invalid CLA");
        }
    }

}
