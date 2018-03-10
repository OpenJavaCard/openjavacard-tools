package better.smartcard.util;

import better.smartcard.iso.ISO7816;
import better.smartcard.iso.SWInfo;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Utilities related to APDU objects
 *
 * Namely verbose APDU printing and convenient APDU construction.
 *
 * This fills some convenience and debuggability gaps in the smartcard API.
 */
public class APDUUtil {

    /**
     * Stringify a command APDU verbosely
     *
     * @param apdu to stringify
     * @return string representing the APDU
     */
    public static String toString(CommandAPDU apdu) {
        byte[] data = apdu.getData();
        String dataString = " (no data)";
        if (data != null) {
            int dataLength = data.length;
            if (dataLength > 0) {
                dataString = " LC=" + HexUtil.hex8(dataLength)
                        + " DATA=" + HexUtil.bytesToHex(data);
            }
        }
        String p12 = HexUtil.bytesToHex(
                new byte[]{
                        (byte) apdu.getP1(),
                        (byte) apdu.getP2()}
        );
        return "CLA=" + HexUtil.hex8(apdu.getCLA())
                + " INS=" + HexUtil.hex8(apdu.getINS())
                + " P12=" + p12
                + dataString;
    }

    /**
     * Stringify a response APDU verbosely
     *
     * @param apdu to stringify
     * @return string representing the APDU
     */
    public static String toString(ResponseAPDU apdu) {
        StringBuffer sb = new StringBuffer();
        int sw = apdu.getSW();
        sb.append("SW=" + HexUtil.hex16(sw));
        SWInfo swData = SWInfo.get(sw);
        if (swData != null) {
            sb.append(" [");
            sb.append(swData.name);
            sb.append("]");
        } else {
            sb.append(" [unknown]");
        }
        byte[] data = apdu.getData();
        if (data == null || data.length == 0) {
            sb.append(" (no data)");
        } else {
            sb.append(" LE=" + data.length);
            sb.append(" DATA=" + HexUtil.bytesToHex(data));
        }
        return sb.toString();
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins) {
        return buildCommand(cla, ins, (byte) 0, (byte) 0, null);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p1
     * @param p2
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, byte p1, byte p2) {
        return buildCommand(cla, ins, p1, p2, null);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param data
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, byte[] data) {
        return buildCommand(cla, ins, (byte) 0, (byte) 0, data);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p12
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, short p12) {
        return buildCommand(cla, ins, p12, null);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p12
     * @param data
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, short p12, byte[] data) {
        return buildCommand(cla, ins,
                (byte) ((p12 >> 8) & 0xFF),
                (byte) ((p12 >> 0) & 0xFF),
                data);
    }

    /**
     * Convenience method for building command APDUs
     * @param cla
     * @param ins
     * @param p1
     * @param p2
     * @param data
     * @return
     */
    public static CommandAPDU buildCommand(byte cla, byte ins, byte p1, byte p2, byte[] data) {
        int length = 5;
        if (data != null) {
            length += data.length;
        }
        byte[] command = new byte[length];
        command[ISO7816.OFFSET_CLA] = cla;
        command[ISO7816.OFFSET_INS] = ins;
        command[ISO7816.OFFSET_P1] = p1;
        command[ISO7816.OFFSET_P2] = p2;
        if (data == null || data.length == 0) {
            command[ISO7816.OFFSET_LC] = 0;
        } else {
            command[ISO7816.OFFSET_LC] = (byte) data.length;
            System.arraycopy(data, 0, command, ISO7816.OFFSET_CDATA, data.length);
        }
        return new CommandAPDU(command);
    }


}
