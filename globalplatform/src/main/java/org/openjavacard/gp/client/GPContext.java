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

package org.openjavacard.gp.client;

import org.openjavacard.gp.scp.SCPProtocolPolicy;
import org.openjavacard.gp.scp.SCPSecurityPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPContext {

    private static final Logger LOG = LoggerFactory.getLogger(GPContext.class);

    private boolean mKeyLoggingEnabled = false;

    private SCPProtocolPolicy mProtocolPolicy = SCPProtocolPolicy.PERMISSIVE;
    private SCPSecurityPolicy mSecurityPolicy = SCPSecurityPolicy.CMAC;

    /**
     * Main constructor
     */
    public GPContext() {
    }

    /** @return true if key logging has been enabled */
    public boolean isKeyLoggingEnabled() {
        return mKeyLoggingEnabled;
    }

    public SCPProtocolPolicy getProtocolPolicy() {
        return mProtocolPolicy;
    }

    public SCPSecurityPolicy getSecurityPolicy() {
        return mSecurityPolicy;
    }

    public void setProtocolPolicy(SCPProtocolPolicy protocolPolicy) {
        LOG.info("new protocol policy " + protocolPolicy);
        mProtocolPolicy = protocolPolicy;
    }

    public void setSecurityPolicy(SCPSecurityPolicy securityPolicy) {
        LOG.info("new protocol policy " + securityPolicy);
        mSecurityPolicy = securityPolicy;
    }

    /**
     * Enable logging of keys
     */
    public void enableKeyLogging() {
        LOG.warn("key logging enabled");
        mKeyLoggingEnabled = true;
    }

}
