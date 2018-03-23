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

/**
 * Representation of GP privileges
 * <p/>
 * Privileges are given to applets and security domains.
 * <p/>
 * Some privileges are specific to one type or the other, which is not yet represented here.
 */
public enum GPPrivilege {
    SECURITY_DOMAIN(0, GP.PRIV1_SECURITY_DOMAIN, "Security Domain"),
    DAP_VERIFICATION(0, GP.PRIV1_DAP_VERIFICATION, "DAP Verification"),
    DELEGATE_MANAGEMENT(0, GP.PRIV1_DELEGATE_MANAGEMENT, "Delegate Management"),
    CARD_LOCK(0, GP.PRIV1_CARD_LOCK, "Card Lock"),
    CARD_TERMINATE(0, GP.PRIV1_CARD_TERMINATE, "Card Terminate"),
    CARD_RESET(0, GP.PRIV1_CARD_RESET, "Card Reset"),
    CVM_MANAGEMENT(0, GP.PRIV1_CVM_MANAGEMENT, "CVM Management"),
    DAP_MANDATORY(0, GP.PRIV1_DAP_MANDATORY, "DAP Mandatory"),
    TRUSTED_PATH(1, GP.PRIV2_TRUSTED_PATH, "Trusted Path"),
    AUTHORIZED_MANAGEMENT(1, GP.PRIV2_AUTHORIZED_MANAGEMENT, "Authorized Management"),
    TOKEN_MANAGEMENT(1, GP.PRIV2_TOKEN_MANAGEMENT, "Token Management"),
    GLOBAL_DELETE(1, GP.PRIV2_GLOBAL_DELETE, "Global Delete"),
    GLOBAL_LOCK(1, GP.PRIV2_GLOBAL_LOCK, "Global Lock"),
    GLOBAL_REGISTRY(1, GP.PRIV2_GLOBAL_REGISTRY, "Global Registry"),
    FINAL_APPLICATION(1, GP.PRIV2_FINAL_APPLICATION, "Final Application"),
    GLOBAL_SERVICE(1, GP.PRIV2_GLOBAL_SERVICE, "Global Service"),
    RECEIPT_GENERATION(2, GP.PRIV3_RECEIPT_GENERATION, "Receipt Generation"),
    CIPHERED_LOAD_DATA_BLOCK(2, GP.PRIV3_CIPHERED_LOAD_DATA_BLOCK, "Ciphered Load Data Block"),
    CONTACTLESS_ACTIVATION(2, GP.PRIV3_CONTACTLESS_ACTIVATION, "Contactless Activation"),
    CONTACTLESS_SELF_ACTIVATION(2, GP.PRIV3_CONTACTLESS_SELF_ACTIVATION, "Contactless Self-Activation");

    public final int privilegeByte;
    public final int privilegeBits;

    public final String label;

    GPPrivilege(int privByte, byte privBits, String label) {
        this.privilegeByte = privByte;
        this.privilegeBits = privBits;
        this.label = label;
    }

    public String toString() {
        return label;
    }

    public static String printPrivileges(byte[] privs, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        for (GPPrivilege priv : GPPrivilege.values()) {
            if (priv.privilegeByte < privs.length) {
                byte privByte = privs[priv.privilegeByte];
                if ((privByte & priv.privilegeBits) != 0) {
                    sb.append(prefix);
                    sb.append(priv.label);
                    sb.append(suffix);
                }
            }
        }
        return sb.toString();
    }

    public static byte[] toBytes(Iterable<GPPrivilege> privilegeIterable) {
        byte[] res = new byte[1];
        for(GPPrivilege privilege: privilegeIterable) {
            if(privilege.privilegeByte > 0 && res.length < 1) {
                byte[] nres = new byte[3];
                System.arraycopy(res, 0, nres, 0, res.length);
                res = nres;
            }
            res[privilege.privilegeByte] |= privilege.privilegeBits;
        }
        return res;
    }

}
