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

package org.openjavacard.gp.protocol;

import org.openjavacard.tlv.TLV;
import org.openjavacard.tlv.TLVUtil;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * GlobalPlatform Card Data
 * <p/>
 * This data structure is an OID-ridden descriptor indicating
 * the GP version, the supported security protocol and other bits.
 * <p/>
 * It is supposed to be the dataset needed to initiate communication
 * with a known but so far unidentified card. In practice one would
 * also consult the IIN/CIN identifiers or lifecycle data from CPLC.
 * <p/>
 */
public class GPCardData {

    private static final Logger LOG = LoggerFactory.getLogger(GPCardData.class);

    public static final byte[] OID_GLOBALPLATFORM = HexUtil.hexToBytes("2A864886FC6B");
    public static final byte[] OID_GP_CARD_RECOGNITION_DATA = HexUtil.hexToBytes("2A864886FC6B01");
    public static final byte[] OID_GP_CARD_MANAGEMENT_DATA = HexUtil.hexToBytes("2A864886FC6B02");
    public static final byte[] OID_GP_CARD_IDENTIFICATION_DATA = HexUtil.hexToBytes("2A864886FC6B03");
    public static final byte[] OID_GP_CARD_SECURITY_DATA = HexUtil.hexToBytes("2A864886FC6B04");

    public static final int TAG_OID = 0x06;

    public static final int TAG_CARD_DATA = 0x66;
    public static final int TAG_CARD_RECOGNITION_DATA = 0x73;

    public static final int TAG_APPLICATION_TAG0 = 0x60;
    public static final int TAG_APPLICATION_TAG3 = 0x63;
    public static final int TAG_APPLICATION_TAG4 = 0x64;
    public static final int TAG_APPLICATION_TAG5 = 0x65;
    public static final int TAG_APPLICATION_TAG6 = 0x66;

    private boolean mIsGlobalPlatform = false;

    private byte[] mGlobalPlatformVersion = null;

    private boolean mGlobalPlatformUnique = false;

    private int mSecurityProtocol = 0;
    private int mSecurityParameters = 0;

    public boolean isGlobalPlatform() {
        return mIsGlobalPlatform;
    }

    public byte[] getGlobalPlatformVersion() {
        return mGlobalPlatformVersion.clone();
    }

    public boolean isUniquelyIdentifiable() {
        return mGlobalPlatformUnique;
    }

    public int getSecurityProtocol() {
        return mSecurityProtocol;
    }

    public int getSecurityParameters() {
        return mSecurityParameters;
    }

    public String getGlobalPlatformVersionString() {
        StringBuffer sb = new StringBuffer();
        byte[] version = mGlobalPlatformVersion;
        for (int i = 0; i < version.length; i++) {
            if (i > 0) {
                sb.append(".");
            }
            sb.append(Byte.toString(version[i]));
        }
        return sb.toString();
    }

    public void read(byte[] buf) {
        read(buf, 0, buf.length);
    }

    public void read(byte[] buf, int off, int len) {
        // check consistency of first level
        if (!TLVUtil.checkTags(buf, off, len)) {
            throw new IllegalArgumentException("Invalid card data - inconsistent toplevel");
        }

        // unwrap first level
        off = TLVUtil.findTag(buf, off, len, TAG_CARD_DATA);
        if (off != 0) {
            throw new IllegalArgumentException("Invalid card data - must start with CD");
        }
        int lenCD = TLVUtil.readLength(buf, off + 1, 3);
        int offCD = off + 1 + TLVUtil.sizeLength(len);

        // check consistency of second level
        if (!TLVUtil.checkTags(buf, offCD, lenCD)) {
            throw new IllegalArgumentException("Invalid card data - inconsistent CD");
        }

        // unwrap second level
        off = TLVUtil.findTag(buf, offCD, lenCD, TAG_CARD_RECOGNITION_DATA);
        if (off == -1) {
            throw new IllegalArgumentException("Invalid card data - CD must contain CRD");
        }
        int lenCRD = TLVUtil.readLength(buf, off + 1, 3);
        int offCRD = off + 1 + TLVUtil.sizeLength(lenCRD);

        // parse the contents
        parseCRD(buf, offCRD, lenCRD);
    }

    private void parseCRD(byte[] buf, int off, int len) {
        if (!TLVUtil.checkTags(buf, off, len)) {
            throw new IllegalArgumentException("CRD TLV inconsistent");
        }

        List<TLV> tlvs = TLVUtil.parseTags(buf, off, len);
        for (TLV tlv : tlvs) {
            int tag = tlv.getTag();
            byte[] data = tlv.getData();
            //LOG.debug("CRD tag " + HexUtil.hex8(tag) + ": "
            //        + HexUtil.bytesToHex(data));
            switch (tag) {
                case TAG_OID:
                    if (Arrays.equals(data, OID_GP_CARD_RECOGNITION_DATA)) {
                        mIsGlobalPlatform = true;
                    } else {
                        throw new IllegalArgumentException("Not a GlobalPlatform card");
                    }
                    break;
                case TAG_APPLICATION_TAG0:
                    byte[] cmd = parseOID(tag, data, OID_GP_CARD_MANAGEMENT_DATA);
                    mGlobalPlatformVersion = cmd;
                    break;
                case TAG_APPLICATION_TAG3:
                    parseOID(tag, data, OID_GP_CARD_IDENTIFICATION_DATA);
                    mGlobalPlatformUnique = true;
                    break;
                case TAG_APPLICATION_TAG4:
                    byte[] csd = parseOID(tag, data, OID_GP_CARD_SECURITY_DATA);
                    mSecurityProtocol = csd[0];
                    mSecurityParameters = csd[1];
                    break;
                case TAG_APPLICATION_TAG5:
                    LOG.debug("CardData: card details: " + HexUtil.bytesToHex(data));
                    break;
                case TAG_APPLICATION_TAG6:
                    LOG.debug("CardData: chip details: " + HexUtil.bytesToHex(data));
                    break;
                default:
                    LOG.warn("Unknown card data tag " + HexUtil.hex8(tag)
                            + " data " + HexUtil.bytesToHex(data));
            }
        }
    }

    private byte[] parseOID(int tag, byte[] buf, byte[] prefix) {
        TLV oid = TLVUtil.parseTag(buf);
        if (oid.getTag() != TAG_OID) {
            throw new IllegalArgumentException("Non-OID in CRD tag " + HexUtil.hex8(tag));
        }
        byte[] data = oid.getData();
        if (!ArrayUtil.startsWith(data, prefix)) {
            throw new IllegalArgumentException("Wrong OID in CRD tag " + HexUtil.hex8(tag));
        }
        return Arrays.copyOfRange(data, prefix.length, data.length);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GP Card Data:\n");
        if (mIsGlobalPlatform) {
            sb.append("  Is a GlobalPlatform card");
            if (mGlobalPlatformVersion != null) {
                sb.append(", version " + getGlobalPlatformVersionString());
            }
            sb.append("\n");
            if (mGlobalPlatformUnique) {
                sb.append("  Card is uniquely identifiable\n");
            }
            if (mSecurityProtocol != 0) {
                sb.append("  Security protocol SCP"
                        + HexUtil.hex8(mSecurityProtocol)
                        + "-" + HexUtil.hex8(mSecurityParameters));
            }
        } else {
            sb.append("  Not a GlobalPlatform card!?");
        }
        return sb.toString();
    }

}
