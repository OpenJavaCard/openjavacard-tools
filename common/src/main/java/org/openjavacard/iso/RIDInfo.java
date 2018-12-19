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

import java.util.HashMap;

public class RIDInfo {

    public static final RIDInfo get(AID rid) {
        if(!rid.isRID()) {
            rid = rid.getRID();
        }
        return KNOWN.get(rid);
    }

    private static final HashMap<AID, RIDInfo> KNOWN = new HashMap<>();

    private static final void addKnown(String aid, String label) {
        RIDInfo info = new RIDInfo(new AID(aid), label);
        KNOWN.put(info.rid, info);
    }

    static {
        addKnown("A000000003", "Visa International");
        addKnown("A000000004", "Mastercard International");
        addKnown("A000000151", "GlobalPlatform");
        addKnown("A000000396", "NXP Germany");
        addKnown("D276000085", "NXP Germany");
        addKnown("D276000124", "fsfEurope");
    }

    public final AID rid;
    public final String label;

    public RIDInfo(AID rid, String label) {
        if(!rid.isRID()) {
            throw new IllegalArgumentException("AID " + rid + " is not a RID");
        }
        this.rid = rid;
        this.label = label;
    }

}
