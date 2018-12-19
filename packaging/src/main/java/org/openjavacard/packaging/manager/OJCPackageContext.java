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
