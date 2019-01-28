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

import org.openjavacard.gp.protocol.GP;
import org.openjavacard.gp.protocol.GPPrivilege;
import org.openjavacard.gp.wrapper.GPSecureWrapper;
import org.openjavacard.iso.AID;
import org.openjavacard.iso.AIDInfo;
import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SWException;
import org.openjavacard.tlv.TLVPrimitive;
import org.openjavacard.util.VerboseString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for GlobalPlatform registry functionality
 * <p/>
 * The registry is the on-card database for metadata about:
 * <ul>
 * <li>Security domains (ISD and SSD)</li>
 * <li>Executable load files (ELF) - equivalent to a JavaCard package</li>
 * <li>Executable modules (ExM) - equivalent to a loaded JavaCard applet</li>
 * <li>Applications (App) - equivalent to an installed JavaCard applet</li>
 * </ul>
 */
public class GPRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(GPRegistry.class);

    private static final int TAG_GP_REGISTRY = 0xE300;

    private static final int TAG_GP_REGISTRY_AID = 0x4F00;
    private static final int TAG_GP_REGISTRY_STATE = 0x9F70;
    private static final int TAG_GP_REGISTRY_PRIVILEGES = 0xC500;
    private static final int TAG_GP_REGISTRY_MODULE = 0x8400;
    private static final int TAG_GP_REGISTRY_PACKAGE = 0xC400;
    private static final int TAG_GP_REGISTRY_DOMAIN = 0xCC00;
    private static final int TAG_GP_REGISTRY_VERSION = 0xCE00;
    private static final int TAG_GP_REGISTRY_IMPLICIT_SELECTION = 0xCF00;
    private static final byte IMPLICIT_SELECTION_CONTACTLESS = (byte)0x80;
    private static final byte IMPLICIT_SELECTION_CONTACT = (byte)0x40;
    private static final byte IMPLICIT_SELECTION_CHANNEL_MASK = (byte)0x1f;

    /** Card being operated on */
    private final GPCard mCard;

    /** Secure command wrapper */
    private final GPSecureWrapper mWrapper;

    /** True if data needs refreshing */
    private boolean mDirty;

    /** Flag to indicate use of the legacy entry format */
    private boolean mUseLegacy;

    /** List of all registry entries */
    private ArrayList<Entry> mAllEntries = new ArrayList<>();

    /** Entry for the ISD */
    private ISDEntry mISD = null;

    /** List of all applet entries */
    private ArrayList<AppEntry> mAllApps = new ArrayList<>();
    /** List of all SSD entries */
    private ArrayList<AppEntry> mAllSSDs = new ArrayList<>();
    /** List of all ELF entries */
    private ArrayList<ELFEntry> mAllELFs = new ArrayList<>();

    /**
     * Main constructor
     * @param card to operate on
     */
    GPRegistry(GPCard card, GPSecureWrapper wrapper) {
        mCard = card;
        mWrapper = wrapper;
        mDirty = true;
    }

    /** @return registry entry for the ISD */
    public ISDEntry getISD() {
        ensureUpdated();
        return mISD;
    }

    /** @return all registry entries */
    public List<Entry> getAllEntries() {
        ensureUpdated();
        return new ArrayList<>(mAllEntries);
    }

    /** @return list of applet entries */
    public List<AppEntry> getAllApps() {
        ensureUpdated();
        return new ArrayList<>(mAllApps);
    }

    /** @return list of SSD entries */
    public List<AppEntry> getAllSSDs() {
        ensureUpdated();
        return new ArrayList<>(mAllSSDs);
    }

    /** @return list of ELF entries */
    public List<ELFEntry> getAllELFs() {
        ensureUpdated();
        return new ArrayList<>(mAllELFs);
    }

    /**
     * Find an applet or package
     *
     * Used specifically for deletion.
     *
     * @param aid to search for
     */
    public Entry findAppletOrPackage(AID aid) {
        Entry res;
        res = findApplet(aid);
        if(res != null) {
            return res;
        }
        res = findPackage(aid);
        if(res != null) {
            return res;
        }
        return res;
    }

    /**
     * Find an applet entry
     * @param aid to search for
     * @return applet entry or null
     */
    public AppEntry findApplet(AID aid) {
        ensureUpdated();
        for(AppEntry app: mAllApps) {
            if(app.mAID.equals(aid)) {
                return app;
            }
        }
        return null;
    }

    /**
     * Return true if applet is present
     * @param aid to search for
     * @return true if present
     */
    public boolean hasApplet(AID aid) {
        return findApplet(aid) != null;
    }

    /**
     * Find a package entry
     * @param aid to search for
     * @return package entry or null
     */
    public ELFEntry findPackage(AID aid) {
        ensureUpdated();
        for (ELFEntry elf : mAllELFs) {
            if (elf.mAID.equals(aid)) {
                return elf;
            }
        }
        return null;
    }

    /**
     * Return true if package is present
     * @param aid to search for
     * @return true if present
     */
    public boolean hasPackage(AID aid) {
        return findPackage(aid) != null;
    }

    /**
     * Find a package entry containing the specified module
     * @param aid to search for
     * @return package entry or null
     */
    public ELFEntry findPackageForModule(AID aid) {
        ensureUpdated();
        for (ELFEntry elf : mAllELFs) {
            for (AID mod : elf.mModules) {
                if (mod.equals(aid)) {
                    return elf;
                }
            }
        }
        return null;
    }

    /**
     * Mark registry as dirty
     */
    public void dirty() {
        LOG.debug("dirty()");
        mDirty = true;
    }

    /**
     * Perform a full update of the registry
     */
    public void update() throws CardException {
        LOG.debug("update()");

        try {
            // read all entries on the card
            List<ISDEntry> isdEntries = readEntriesISD();
            List<AppEntry> appEntries = readEntriesAppAndSSD();
            List<ELFEntry> elfEntries = readEntriesELF();

            // we produce various lists sorted by type
            ISDEntry isdEntry = null;
            ArrayList<Entry> allEntries = new ArrayList<>();
            ArrayList<AppEntry> allApps = new ArrayList<>();
            ArrayList<AppEntry> allSSDs = new ArrayList<>();
            ArrayList<ELFEntry> allELFs = new ArrayList<>();

            // check each set of results
            if (!isdEntries.isEmpty()) {
                allEntries.addAll(isdEntries);
                isdEntry = isdEntries.get(0);
            }
            if (!appEntries.isEmpty()) {
                allEntries.addAll(appEntries);
                for (AppEntry appEntry : appEntries) {
                    allApps.add(appEntry);
                }
            }
            if (!elfEntries.isEmpty()) {
                allEntries.addAll(elfEntries);
                allELFs.addAll(elfEntries);
            }

            // update state
            mISD = isdEntry;
            mAllEntries = allEntries;
            mAllApps = allApps;
            mAllELFs = allELFs;
            mAllSSDs = allSSDs;

            // no longer dirty
            mDirty = false;
        } catch (CardException e) {
            throw new CardException("Error updating registry", e);
        }
    }

    private void ensureUpdated() {
        LOG.trace("ensureUpdated()");
        if(mDirty) {
            try {
                update();
            } catch (CardException e) {
                mDirty = true;
                throw new RuntimeException("Error updating registry", e);
            }
        }
    }

    /**
     * Read ISD entries using GET STATUS
     * @return entries found
     * @throws CardException
     */
    private List<ISDEntry> readEntriesISD() throws CardException {
        LOG.trace("readEntriesISD()");
        return readStatusGeneric(GP.GET_STATUS_P1_ISD_ONLY, ISDEntry.class);
    }

    /**
     * Read Application and SSD entries using GET STATUS
     * @return entries found
     * @throws CardException
     */
    private List<AppEntry> readEntriesAppAndSSD() throws CardException {
        LOG.trace("readEntriesAppAndSSD()");
        return readStatusGeneric(GP.GET_STATUS_P1_APP_AND_SD_ONLY, AppEntry.class);
    }

    /**
     * Read ELF entries using GET STATUS
     * <p/>
     * The case of ELFs is special because TLV format offers optional ExM information,
     * which some cards do not provide. We therefore have an additional fallback here.
     * <p/>
     * @return entries found
     * @throws CardException
     */
    private List<ELFEntry> readEntriesELF() throws CardException {
        LOG.trace("readEntriesELF()");
        List<ELFEntry> elfEntries = null;

        if(!mUseLegacy) {
            // try TLV with module information
            try {
                elfEntries = readStatusTLV(GP.GET_STATUS_P1_EXM_AND_ELF_ONLY, ELFEntry.class);
            } catch (SWException e) {
                switch(e.getCode()) {
                    case ISO7816.SW_FUNC_NOT_SUPPORTED:
                    case ISO7816.SW_INCORRECT_P1P2:
                        break;
                    default:
                        throw e;
                }
            }
            if(elfEntries != null) {
                return elfEntries;
            }
            // try TLV without module information
            try {
                elfEntries = readStatusTLV(GP.GET_STATUS_P1_ELF_ONLY, ELFEntry.class);
            } catch (SWException e) {
                switch(e.getCode()) {
                    case ISO7816.SW_FUNC_NOT_SUPPORTED:
                    case ISO7816.SW_INCORRECT_P1P2:
                        break;
                    default:
                        throw e;
                }
            }
            if(elfEntries != null) {
                return elfEntries;
            }
        }

        // TLV did not work - from now on use legacy format
        mUseLegacy = true;

        // try only the variant without module information
        elfEntries = readStatusLegacy(GP.GET_STATUS_P1_ELF_ONLY, ELFEntry.class);

        return elfEntries;
    }

    /**
     * Perform GET STATUS, retrieving either TLV or legacy entries
     *
     * @param p1Subset subset parameter
     * @param clazz to instantiate
     * @param <E> class of entries
     * @return list of entries
     * @throws CardException on error
     */
    private <E extends Entry>
    List<E> readStatusGeneric(byte p1Subset, Class<E> clazz) throws CardException {
        // use legacy after one failure
        if(mUseLegacy) {
            return readStatusLegacy(p1Subset, clazz);
        }
        // guarded attempt at using TLV
        List<E> res;
        try {
            res = readStatusTLV(p1Subset, clazz);
        } catch (SWException e) {
            if(e.getCode() == ISO7816.SW_INCORRECT_P1P2) {
                // fall back to legacy format
                res = readStatusLegacy(p1Subset, clazz);
                // continue using legacy format
                mUseLegacy = true;
            } else {
                throw e;
            }
        }
        return res;
    }

    /**
     * Perform GET STATUS using legacy format
     *
     * @param p1Subset subset parameter
     * @param clazz to instantiate
     * @param <E> class of entries
     * @return list of entries
     * @throws CardException on error
     */
    private <E extends Entry>
    List<E> readStatusLegacy(byte p1Subset, Class<E> clazz) throws CardException {
        byte format = GP.GET_STATUS_P2_FORMAT_LEGACY;
        List<byte[]> chunks = mWrapper.performReadStatus(p1Subset, format);
        List<E> res = new ArrayList<>();
        for (byte[] chunk: chunks) {
            int off = 0;
            while(off < chunk.length) {
                try {
                    E entry = clazz.newInstance();
                    off = entry.readLegacy(chunk, off);
                    res.add(entry);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new Error("Error instantiating registry entry", e);
                }
            }
        }
        return res;
    }

    /**
     * Perform GET STATUS using TLV format
     *
     * @param p1Subset subset parameter
     * @param clazz to instantiate
     * @param <E> class of entry
     * @return list of entries
     * @throws CardException on error
     */
    private <E extends Entry>
    List<E> readStatusTLV(byte p1Subset, Class<E> clazz) throws CardException {
        byte format = GP.GET_STATUS_P2_FORMAT_TLV;
        List<byte[]> chunks = mWrapper.performReadStatus(p1Subset, format);
        List<E> res = new ArrayList<>();
        try {
            for (byte[] chunk : chunks) {
                List<TLVPrimitive> tlvs = TLVPrimitive.readPrimitives(chunk);
                for (TLVPrimitive tlv : tlvs) {
                    try {
                        E entry = clazz.newInstance();
                        entry.readTLV(tlv.getValueBytes());
                        res.add(entry);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new Error("Error instantiating registry entry", e);
                    }
                }
            }
        } catch (IOException e) {
            throw new Error("Error parsing TLV", e);
        }
        return res;
    }

    /**
     * Types of registry entries
     */
    public enum Type {
        ISD, APP, SSD, ELF
    }

    /**
     * Registry entries
     */
    public static abstract class Entry implements VerboseString {
        final Type mType;
        AID mAID;
        byte mState;
        byte[] mPrivileges;
        List<AID> mModules;
        AID mPackage;
        AID mDomain;
        byte[] mVersion;
        List<Byte> mImplicitSelection;

        Entry(Type type) {
            mType = type;
            mModules = new ArrayList<>();
            mImplicitSelection = new ArrayList<>();
        }

        public Type getType() {
            return mType;
        }

        public AID getAID() {
            return mAID;
        }

        public byte getState() {
            return mState;
        }

        public List<AID> getModules() {
            return mModules;
        }

        abstract boolean isLocked();

        abstract String getStateString();

        void readTLV(byte[] data) throws IOException {
            List<TLVPrimitive> tlvs = TLVPrimitive.readPrimitives(data);
            List<AID> modules = new ArrayList<>();
            for (TLVPrimitive tlv : tlvs) {
                int tag = tlv.getTag();
                switch (tag) {
                    case TAG_GP_REGISTRY_AID:
                        mAID = new AID(tlv.getValueBytes());
                        break;
                    case TAG_GP_REGISTRY_STATE:
                        mState = tlv.getValueBytes()[0];
                        break;
                    case TAG_GP_REGISTRY_PRIVILEGES:
                        mPrivileges = tlv.getValueBytes();
                        break;
                    case TAG_GP_REGISTRY_MODULE:
                        if(this instanceof ELFEntry) {
                            modules.add(new AID(tlv.getValueBytes()));
                        } else {
                            throw new IllegalArgumentException("Module descriptor in non-ELF registry entry");
                        }
                        break;
                    case TAG_GP_REGISTRY_PACKAGE:
                        mPackage = new AID(tlv.getValueBytes());
                        break;
                    case TAG_GP_REGISTRY_DOMAIN:
                        mDomain = new AID(tlv.getValueBytes());
                        break;
                    case TAG_GP_REGISTRY_VERSION:
                        mVersion = tlv.getValueBytes();
                        break;
                    case TAG_GP_REGISTRY_IMPLICIT_SELECTION:
                        mImplicitSelection.add(tlv.getValueBytes()[0]);
                        break;
                    default:
                        if(mAID != null) {
                            LOG.warn("Unknown tag in registry entry " + mAID + ": " + tlv.toString());
                        } else {
                            LOG.warn("Unknown tag in registry entry <unknown>: " + tlv.toString());
                        }
                        break;
                }
            }
            mModules = modules;
        }

        public int readLegacy(byte[] data, int off) {
            return readLegacyCommon(data, off);
        }

        int readLegacyCommon(byte[] data, int off) {
            int aidLen = data[off++];
            mAID = new AID(data, off, aidLen); off += aidLen;
            mState = data[off++];
            mPrivileges = new byte[] { data[off++] };
            return off;
        }

        public String toString() {
            return mType.toString() + " " + mAID.toString();
        }

    }

    public static class AppEntry extends Entry {
        AppEntry() {
            super(Type.APP);
        }

        AppEntry(Type type) {
            super(type);
        }

        public boolean isSSD() {
            return (mPrivileges[0] & GPPrivilege.SECURITY_DOMAIN.privilegeBits) != 0;
        }

        @Override
        boolean isLocked() {
            if(isSSD()) {
                return GP.domainLocked(mState);
            } else {
                return GP.appletLocked(mState);
            }
        }

        @Override
        String getStateString() {
            if(isSSD()) {
                return GP.domainStateString(mState);
            } else {
                return GP.appletStateString(mState);
            }
        }

        int getAppletSpecificState() {
            if(isSSD()) {
                throw new UnsupportedOperationException("Security domains do not have specific state");
            }
            return GP.appletStateSpecific(mState);
        }

        public String toVerboseString() {
            StringBuilder sb = new StringBuilder();

            // header line
            sb.append(mType.toString());
            sb.append(" ");
            sb.append(mAID.toString());
            AIDInfo aidInfo = AIDInfo.get(mAID);
            if(aidInfo != null) {
                sb.append("\n  Label: ");
                sb.append(aidInfo.label);
                if(aidInfo.protect) {
                    sb.append(" [protected]");
                }
                if(isLocked()) {
                    sb.append(" [locked]");
                }
            }

            // globalplatform state
            sb.append("\n  State: ");
            sb.append(getStateString());
            // applet-specific state
            if(!isSSD()) {
                int appletState = getAppletSpecificState();
                if(appletState != 0) {
                    sb.append("\n  Applet state: ");
                    sb.append(appletState);
                }
            }
            // security domain tag
            if(mDomain != null) {
                sb.append("\n  Domain: ");
                sb.append(mDomain.toString());
            }
            // package tag
            if(mPackage != null) {
                sb.append("\n  Package: ");
                sb.append(mPackage.toString());
            }
            // privileges
            sb.append("\n  Privileges:");
            sb.append(GPPrivilege.printPrivileges(mPrivileges, "\n    ", ""));
            // implicit selection tags
            if(!mImplicitSelection.isEmpty()) {
                sb.append("\n  Implicit selection:");
                for(byte selection: mImplicitSelection) {
                    int channel = selection & IMPLICIT_SELECTION_CHANNEL_MASK;
                    sb.append("\n    Channel ");
                    sb.append(channel);
                    if((selection & IMPLICIT_SELECTION_CONTACT) != 0) {
                        sb.append(" [contact]");
                    }
                    if((selection & IMPLICIT_SELECTION_CONTACTLESS) != 0) {
                        sb.append(" [contactless]");
                    }
                }
            }
            return sb.toString();
        }
    }

    public static class ISDEntry extends AppEntry {
        ISDEntry() {
            super(Type.ISD);
        }

        @Override
        boolean isLocked() {
            return mState == GP.CARD_STATE_LOCKED || mState == GP.CARD_STATE_TERMINATED;
        }

        @Override
        String getStateString() {
            return GP.cardStateString(mState);
        }
    }

    public static class ELFEntry extends Entry {
        ELFEntry() {
            super(Type.ELF);
        }

        @Override
        boolean isLocked() {
            return false;
        }

        @Override
        String getStateString() {
            return GP.elfStateString(mState);
        }

        @Override
        public int readLegacy(byte[] data, int off) {
            off = readLegacyCommon(data, off);
            mModules = new ArrayList<>();
            return off;
        }

        public String toVerboseString() {
            StringBuilder sb = new StringBuilder();

            // header line
            sb.append(mType.toString());
            sb.append(" ");
            sb.append(mAID.toString());
            AIDInfo aidInfo = AIDInfo.get(mAID);
            if(aidInfo != null) {
                sb.append("\n  Label: ");
                sb.append(aidInfo.label);
                if(aidInfo.protect) {
                    sb.append(" [protected]");
                }
            }

            // globalplatform state
            sb.append("\n  State: ");
            sb.append(getStateString());
            // version information
            if(mVersion != null) {
                sb.append("\n  Version: ");
                for(int i = 0; i < mVersion.length; i++) {
                    if(i > 0) {
                        sb.append(".");
                    }
                    sb.append(Integer.toString(mVersion[i] & 0xFF));
                }
            }
            // security domain tag
            if(mDomain != null) {
                sb.append("\n  Domain: ");
                sb.append(mDomain.toString());
            }
            // module information
            if (mModules != null && !mModules.isEmpty()) {
                sb.append("\n");
                for (AID module : mModules) {
                    sb.append("\n  ExM ");
                    sb.append(module.toString());
                }
            }
            return sb.toString();
        }
    }

}
