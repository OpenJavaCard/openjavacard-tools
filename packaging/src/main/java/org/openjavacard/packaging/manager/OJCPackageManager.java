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

package org.openjavacard.packaging.manager;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPRegistry;
import org.openjavacard.iso.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OJCPackageManager {

    private static final Logger LOG = LoggerFactory.getLogger(OJCPackageManager.class);

    private OJCPackageContext mContext;
    private GPCard mCard;

    public OJCPackageManager(OJCPackageContext context, GPCard card) {
        mContext = context;
        mCard = card;
    }

    public OJCPackageContext getContext() {
        return mContext;
    }

    public List<OJCPackage> getInstalledPackages() {
        LOG.debug("getInstalledPackages()");
        GPRegistry registry = mCard.getRegistry();
        registry.getAllELFs();
        return new ArrayList<>();
    }

    public List<OJCPackage> getAvailablePackages() {
        LOG.debug("getAvailablePackages()");
        return new ArrayList<>();
    }

    public OJCPackage findPackageByAID(AID aid) {
        return mContext.findPackageByAID(aid);
    }

    public OJCPackage findPackageByName(String name) {
        return mContext.findPackageByName(name);
    }

    public void installPackage(OJCPackage pkg) {
        LOG.debug("installPackage()");
    }

    public void removePackage(OJCPackage pkg) {
        LOG.debug("removePackage()");
    }

}
