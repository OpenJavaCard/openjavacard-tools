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
