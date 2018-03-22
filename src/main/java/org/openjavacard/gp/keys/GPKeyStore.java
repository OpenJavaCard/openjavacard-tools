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

package org.openjavacard.gp.keys;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.protocol.GPKeyInfo;
import org.openjavacard.gp.protocol.GPKeyInfoEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPKeyStore {

    private static final Logger LOG = LoggerFactory.getLogger(GPKeyStore.class);

    public GPKeyStore() {
    }

    GPKeySet selectKeys(GPCard card) {
        String cardId = card.getLifetimeIdentifier();
        LOG.info("selecting keys for " + cardId);
        GPKeyInfo ki = card.getCardKeyInfo();
        for(GPKeyInfoEntry ke: ki.getKeyInfos()) {
            int keyId = ke.getKeyId();
            int keyVersion = ke.getKeyVersion();
            LOG.info("need key id " + keyId + " version " + keyVersion);
            String name = "globalplatform/" + cardId + "/version-" + keyVersion + "/id-" + keyId;
            LOG.info("querying for " + name);
        }
        return null;
    }

}
