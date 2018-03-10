package better.smartcard.iso;

import better.smartcard.util.HexUtil;

import java.util.HashMap;
import java.util.Map;

public class SWInfo implements ISO7816 {

    public static final SWInfo get(int sw) {
        return ISO.get(sw);
    }

    private static final Map<Integer, SWInfo> ISO = new HashMap<Integer, SWInfo>();

    private static void addISO(int sw, String name) {
        ISO.put(sw, new SWInfo(sw, name));
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

    private SWInfo(int sw, String name) {
        this.sw = sw;
        this.name = name;
    }

    public String toString() {
        return "SW=" + HexUtil.hex16(sw) + " [" + SW.toString(sw) + "]";
    }

}
