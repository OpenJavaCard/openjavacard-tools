package better.smartcard.protocol;

import better.smartcard.util.HexUtil;

import java.util.HashMap;
import java.util.Map;

public class SW implements ISO7816 {

    private static final Map<Integer, SW> ENTRIES = new HashMap<Integer, SW>();

    private static void addISO(int sw, String name) {
        ENTRIES.put(sw, new SW(sw, name));
    }

    public static SW get(int sw) {
        return ENTRIES.get(sw);
    }

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
        SW entry = get(sw);
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
        return sb.toString();
    }

    static {
        addISO(SW_NO_ERROR, "no error");
        addISO(SW_UNKNOWN, "unknown");
        addISO(SW_APPLET_SELECT_FAILED, "applet select failed");
        addISO(SW_COMMAND_NOT_ALLOWED, "command not allowed");
        addISO(SW_CONDITIONS_NOT_SATISFIED, "conditions not satisfied");
        addISO(SW_DATA_INVALID, "data invalid");
        addISO(SW_FILE_FULL, "file full");
        addISO(SW_FILE_INVALID, "file invalid");
        addISO(SW_FILE_NOT_FOUND, "file not found");
        addISO(SW_INCORRECT_P1P2, "incorrect P1P2");
        addISO(SW_LAST_COMMAND_EXPECTED, "last command expected");
        addISO(SW_RECORD_NOT_FOUND, "record not found");
        //addISO(SW_WARNING_STATE_UNCHANGED, "warning state unchanged");
        addISO(SW_SECURITY_STATUS_NOT_SATISFIED, "security status not satisfied");
        addISO(SW_WRONG_P1P2, "wrong P1P2");
        addISO(SW_WRONG_DATA, "wrong data");
        addISO(SW_WRONG_LENGTH, "wrong length");
        addISO(SW_CLA_NOT_SUPPORTED, "CLA not supported");
        addISO(SW_INS_NOT_SUPPORTED, "INS not supported");
        addISO(SW_FUNC_NOT_SUPPORTED, "function not supported");
        addISO(SW_LOGICAL_CHANNEL_NOT_SUPPORTED, "logical channel not supported");
        addISO(SW_COMMAND_CHAINING_NOT_SUPPORTED, "command chaining not supported");
        addISO(SW_SECURE_MESSAGING_NOT_SUPPORTED, "secure messaging not supported");
        addISO(SW_REFERENCED_DATA_NOT_FOUND, "referenced data not found");
    }

    public final int sw;
    public final String name;

    private SW(int sw, String name) {
        this.sw = sw;
        this.name = name;
    }

    public String toString() {
        return "SW=" + HexUtil.hex16(sw) + " [" + toString(sw) + "]";
    }

}
