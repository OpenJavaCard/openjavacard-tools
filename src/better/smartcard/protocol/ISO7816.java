/*
 * Copyright 2011 Licel LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package better.smartcard.protocol;

/**
 * <code>ISO7816</code> encapsulates constants related to ISO 7816-3 and ISO 7816-4.
 * <code>ISO7816</code> interface contains only static fields.<p>
 * The static fields with <code>SW_</code> prefixes define constants for the ISO 7816-4 defined response
 * status word. The fields which use the <code>_00</code> suffix require the low order byte to be
 * customized appropriately e.g (ISO7816.SW_CORRECT_LENGTH_00 + (0x0025 & 0xFF)).<p>
 * The static fields with <code>OFFSET_</code> prefixes define constants to be used to index into
 * the CardAPDU buffer byte array to access ISO 7816-4 defined header information.
 */
public interface ISO7816 {
    public static final int SW_NO_ERROR = 0x9000;
    public static final int SW_BYTES_REMAINING_00 = 0x6100;
    public static final int SW_WRONG_LENGTH = 0x6700;
    public static final int SW_SECURITY_STATUS_NOT_SATISFIED = 0x6982;
    public static final int SW_FILE_INVALID = 0x6983;
    public static final int SW_DATA_INVALID = 0x6984;
    public static final int SW_CONDITIONS_NOT_SATISFIED = 0x6985;
    public static final int SW_COMMAND_NOT_ALLOWED = 0x6986;
    public static final int SW_APPLET_SELECT_FAILED = 0x6999;
    public static final int SW_WRONG_DATA = 0x6A80;
    public static final int SW_FUNC_NOT_SUPPORTED = 0x6A81;
    public static final int SW_FILE_NOT_FOUND = 0x6A82;
    public static final int SW_RECORD_NOT_FOUND = 0x6A83;
    public static final int SW_INCORRECT_P1P2 = 0x6A86;
    public static final int SW_REFERENCED_DATA_NOT_FOUND = 0x6A88;
    public static final int SW_WRONG_P1P2 = 0x6B00;
    public static final int SW_CORRECT_LENGTH_00 = 0x6C00;
    public static final int SW_INS_NOT_SUPPORTED = 0x6D00;
    public static final int SW_CLA_NOT_SUPPORTED = 0x6E00;
    static final int SW_COMMAND_CHAINING_NOT_SUPPORTED = 0x6884;
    static final int SW_LAST_COMMAND_EXPECTED = 0x6883;
    public static final int SW_UNKNOWN = 0x6F00;
    public static final int SW_FILE_FULL = 0x6A84;
    public static final int SW_LOGICAL_CHANNEL_NOT_SUPPORTED = 0x6881;
    public static final int SW_SECURE_MESSAGING_NOT_SUPPORTED = 0x6882;
    public static final int SW_WARNING_STATE_UNCHANGED = 0x6200;

    public static final byte OFFSET_CLA = 0;
    public static final byte OFFSET_INS = 1;
    public static final byte OFFSET_P1 = 2;
    public static final byte OFFSET_P2 = 3;
    public static final byte OFFSET_LC = 4;
    public static final byte OFFSET_CDATA = 5;

    public static final byte CLA_ISO7816 = 0;
    public static final byte INS_EXTERNAL_AUTHENTICATE = -126;
}
