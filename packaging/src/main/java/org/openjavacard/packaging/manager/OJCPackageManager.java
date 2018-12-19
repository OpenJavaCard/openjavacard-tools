package org.openjavacard.packaging.manager;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPRegistry;
import org.openjavacard.iso.AID;
import org.openjavacard.packaging.model.OJCPackageInfo;
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
