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

package org.openjavacard.cap.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * In-memory representation of a CAP file
 * <p/>
 * This contains the raw binary components of all packages in the
 * CAP file as well as the relevant metadata from the manifest.
 */
public class CapFile {

    private static final Logger LOG = LoggerFactory.getLogger(CapFile.class);

    private static final String ATTR_MANIFEST_VERSION = "Manifest-Version";
    private static final String ATTR_CREATED_BY = "Created-By";

    private Manifest mManifest;

    private String mManifestVersion;

    private String mCreatedBy;

    private ArrayList<CapFilePackage> mPackages = new ArrayList<>();

    CapFile() {
    }

    public Manifest getManifest() {
        return mManifest;
    }

    public String getManifestVersion() {
        return mManifestVersion;
    }

    public String getCreatedBy() {
        return mCreatedBy;
    }

    public CapFilePackage getPackage() {
        if(mPackages.size() > 1) {
            throw new Error("CAP file has more than one package");
        }
        return mPackages.get(0);
    }

    public List<CapFilePackage> getPackages() {
        return mPackages;
    }

    /**
     * Internal: parse a CAP file
     * @param manifest
     * @param files
     */
    void read(Manifest manifest, Map<String, byte[]> files) {
        // remember manifest
        mManifest = manifest;
        // read main attributes
        Attributes attributes = manifest.getMainAttributes();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            String name = entry.getKey().toString();
            String value = (String) entry.getValue();
            LOG.trace("attribute " + name + " = " + value);
            if (name == ATTR_MANIFEST_VERSION) {
                mManifestVersion = value;
            }
            if (name == ATTR_CREATED_BY) {
                mCreatedBy = value;
            }
        }
        // read entries
        for (Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet()) {
            String pkgName = entry.getKey();
            LOG.debug("reading package " + pkgName);
            Attributes pkgAttributes = entry.getValue();
            CapFilePackage pkg = new CapFilePackage();
            pkg.read(pkgName, pkgAttributes, files);
            mPackages.add(pkg);
        }
    }

}
