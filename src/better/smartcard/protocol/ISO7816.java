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
    int SW_NO_ERROR = 0x9000;
    int SW_BYTES_REMAINING_00 = 0x6100;
    int SW_WRONG_LENGTH = 0x6700;
    int SW_SECURITY_STATUS_NOT_SATISFIED = 0x6982;
    int SW_FILE_INVALID = 0x6983;
    int SW_DATA_INVALID = 0x6984;
    int SW_CONDITIONS_NOT_SATISFIED = 0x6985;
    int SW_COMMAND_NOT_ALLOWED = 0x6986;
    int SW_APPLET_SELECT_FAILED = 0x6999;
    int SW_WRONG_DATA = 0x6A80;
    int SW_FUNC_NOT_SUPPORTED = 0x6A81;
    int SW_FILE_NOT_FOUND = 0x6A82;
    int SW_RECORD_NOT_FOUND = 0x6A83;
    int SW_INCORRECT_P1P2 = 0x6A86;
    int SW_REFERENCED_DATA_NOT_FOUND = 0x6A88;
    int SW_WRONG_P1P2 = 0x6B00;
    int SW_CORRECT_LENGTH_00 = 0x6C00;
    int SW_INS_NOT_SUPPORTED = 0x6D00;
    int SW_CLA_NOT_SUPPORTED = 0x6E00;
    int SW_COMMAND_CHAINING_NOT_SUPPORTED = 0x6884;
    int SW_LAST_COMMAND_EXPECTED = 0x6883;
    int SW_UNKNOWN = 0x6F00;
    int SW_FILE_FULL = 0x6A84;
    int SW_LOGICAL_CHANNEL_NOT_SUPPORTED = 0x6881;
    int SW_SECURE_MESSAGING_NOT_SUPPORTED = 0x6882;
    int SW_WARNING_STATE_UNCHANGED = 0x6200;

    byte OFFSET_CLA = 0;
    byte OFFSET_INS = 1;
    byte OFFSET_P1 = 2;
    byte OFFSET_P2 = 3;
    byte OFFSET_LC = 4;
    byte OFFSET_CDATA = 5;

    byte CLA_ISO7816 = 0;

    byte INS_SELECT = (byte)0xA4;
    byte SELECT_P1_BY_FILEID = 0x00;
    byte SELECT_P1_BY_NAME = 0x04;
    byte SELECT_P2_FIRST_OR_ONLY = 0x00;
    byte SELECT_P2_NEXT = 0x02;

    byte INS_EXTERNAL_AUTHENTICATE = -126;
}
