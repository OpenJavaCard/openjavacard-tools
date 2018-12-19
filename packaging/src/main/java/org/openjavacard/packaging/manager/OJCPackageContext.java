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

import org.openjavacard.iso.AID;
import org.openjavacard.packaging.model.OJCPackageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class OJCPackageContext {

    private static final Logger LOG = LoggerFactory.getLogger(OJCPackageContext.class);

    public void initialize() {
        LOG.debug("initialize()");
    }

    public List<OJCPackage> getPackages() {
        return new ArrayList<>();
    }

    public OJCPackage findPackageByAID(AID aid) {
        LOG.debug("findPackageByAID(" + aid + ")");
        return null;
    }

    public OJCPackage findPackageByName(String name) {
        LOG.debug("findPackageByName(" + name + ")");
        return null;
    }

}
