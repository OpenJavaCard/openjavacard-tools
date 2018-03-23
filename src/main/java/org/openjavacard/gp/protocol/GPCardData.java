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

package org.openjavacard.gp.protocol;

import org.openjavacard.tlv.TLV;
import org.openjavacard.tlv.TLVConstructed;
import org.openjavacard.tlv.TLVPrimitive;
import org.openjavacard.util.ArrayUtil;
import org.openjavacard.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public static final int TAG_OID = 0x0600;

    public static final int TAG_CARD_DATA = 0x6600;
    public static final int TAG_CARD_RECOGNITION_DATA = 0x7300;

    public static final int TAG_APPLICATION_TAG0 = 0x6000;
    public static final int TAG_APPLICATION_TAG3 = 0x6300;
    public static final int TAG_APPLICATION_TAG4 = 0x6400;
    public static final int TAG_APPLICATION_TAG5 = 0x6500;
    public static final int TAG_APPLICATION_TAG6 = 0x6600;

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

    public void read(byte[] buf) throws IOException {
        // outer layer is the card data
        TLVConstructed cd = TLV.readRecursive(buf).asConstructed(TAG_CARD_DATA);
        // which contains card recognition data
        TLVConstructed crd = cd.getChild(0).asConstructed(TAG_CARD_RECOGNITION_DATA);
        // parse the contents
        parseCRD(crd.getChildren());
    }

    private void parseCRD(List<TLV> tlvs) throws IOException {
        for (TLV tlv : tlvs) {
            int tag = tlv.getTag();
            //LOG.debug("CRD tag " + HexUtil.hex8(tag) + ": "
            //        + HexUtil.bytesToHex(data));
            switch (tag) {
                case TAG_OID:
                    byte[] data = tlv.asPrimitive(TAG_OID).getValueBytes();
                    if (Arrays.equals(data, OID_GP_CARD_RECOGNITION_DATA)) {
                        mIsGlobalPlatform = true;
                    } else {
                        throw new IllegalArgumentException("Not a GlobalPlatform card");
                    }
                    break;
                case TAG_APPLICATION_TAG0:
                    byte[] cmd = parseOID(tlv, OID_GP_CARD_MANAGEMENT_DATA);
                    mGlobalPlatformVersion = cmd;
                    break;
                case TAG_APPLICATION_TAG3:
                    parseOID(tlv, OID_GP_CARD_IDENTIFICATION_DATA);
                    mGlobalPlatformUnique = true;
                    break;
                case TAG_APPLICATION_TAG4:
                    byte[] csd = parseOID(tlv, OID_GP_CARD_SECURITY_DATA);
                    mSecurityProtocol = csd[0];
                    mSecurityParameters = csd[1];
                    break;
                case TAG_APPLICATION_TAG5:
                    LOG.debug("CardData: card details: " + tlv);
                    break;
                case TAG_APPLICATION_TAG6:
                    LOG.debug("CardData: chip details: " + tlv);
                    break;
                default:
                    LOG.warn("Unknown card data tag " + tlv);
            }
        }
    }

    private byte[] parseOID(TLV tlv, byte[] prefix) {
        TLVPrimitive oid = tlv.asConstructed().getChild(0).asPrimitive(TAG_OID);
        int tag = oid.getTag();
        byte[] data = oid.getValueBytes();
        if (!ArrayUtil.startsWith(data, prefix)) {
            throw new IllegalArgumentException("Wrong OID in CRD tag " + HexUtil.hex16(tag));
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
