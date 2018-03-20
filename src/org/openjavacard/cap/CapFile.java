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

package org.openjavacard.cap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class CapFile {

    private static final Logger LOG = LoggerFactory.getLogger(CapFile.class);

    private String ATTR_MANIFEST_VERSION = "Manifest-Version";
    private String ATTR_CREATED_BY = "Created-By";

    private Manifest mManifest;

    String mManifestVersion;

    String mCreatedBy;

    private ArrayList<CapPackage> mPackages = new ArrayList<>();

    public CapFile() {
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

    public CapPackage getPackage() {
        if(mPackages.size() > 1) {
            throw new Error("CAP file has more than one package");
        }
        return mPackages.get(0);
    }

    public List<CapPackage> getPackages() {
        return mPackages;
    }

    public void read(Manifest manifest, Map<String, byte[]> files) {
        // remember manifest
        mManifest = manifest;
        // read main attributes
        Attributes attributes = manifest.getMainAttributes();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            String name = entry.getKey().toString();
            String value = (String) entry.getValue();
            LOG.debug("attribute " + name + " = " + value);
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
            LOG.debug("package " + pkgName);
            Attributes pkgAttributes = entry.getValue();
            CapPackage pkg = new CapPackage();
            pkg.read(pkgName, pkgAttributes, files);
            mPackages.add(pkg);
        }
    }

}
