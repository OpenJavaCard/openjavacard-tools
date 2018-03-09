package better.smartcard.gp;

import better.smartcard.gp.protocol.GP;
import better.smartcard.gp.protocol.GPPrivilege;
import better.smartcard.tlv.TLV;
import better.smartcard.tlv.TLVUtil;
import better.smartcard.util.AID;
import better.smartcard.util.HexUtil;
import better.smartcard.util.VerboseString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import java.util.ArrayList;
import java.util.List;

public class GPRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(GPRegistry.class);

    private static final int TAG_GP_REGISTRY = 0xE3;

    private static final int TAG_GP_REGISTRY_AID = 0x4F;
    private static final int TAG_GP_REGISTRY_STATE = 0x9F70;
    private static final int TAG_GP_REGISTRY_PRIVILEGES = 0xC5;
    private static final int TAG_GP_REGISTRY_MODULE = 0x84;

    GPCard mCard;

    ArrayList<Entry> mAllEntries = new ArrayList<>();

    ISDEntry mISD = null;

    ArrayList<AppEntry> mAllApps = new ArrayList<>();
    ArrayList<AppEntry> mAllSSDs = new ArrayList<>();
    ArrayList<ELFEntry> mAllELFs = new ArrayList<>();

    GPRegistry(GPCard card) {
        mCard = card;
    }

    public ISDEntry getISD() {
        return mISD;
    }

    public List<AppEntry> getAllApps() {
        return new ArrayList<>(mAllApps);
    }

    public List<AppEntry> getAllSSDs() {
        return new ArrayList<>(mAllSSDs);
    }

    public List<ELFEntry> getAllELFs() {
        return new ArrayList<>(mAllELFs);
    }

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

    public AppEntry findApplet(AID aid) {
        for(AppEntry app: mAllApps) {
            if(app.mAID.equals(aid)) {
                return app;
            }
        }
        return null;
    }

    public boolean hasApplet(AID aid) {
        return findApplet(aid) != null;
    }

    public ELFEntry findPackage(AID aid) {
        for(ELFEntry elf: mAllELFs) {
            if(elf.mAID.equals(aid)) {
                return elf;
            }
        }
        return null;
    }

    public boolean hasPackage(AID aid) {
        return findPackage(aid) != null;
    }

    public ELFEntry findPackageForModule(AID aid) {
        for(ELFEntry elf: mAllELFs) {
            for(AID mod: elf.mModules) {
                if(mod.equals(aid)) {
                    return elf;
                }
            }
        }
        return null;
    }

    public void update() {
        LOG.debug("updating registry");

        try {
            ISDEntry isdEntry = null;
            ArrayList<Entry> allEntries = new ArrayList<>();
            ArrayList<AppEntry> allApps = new ArrayList<>();
            ArrayList<AppEntry> allSSDs = new ArrayList<>();
            ArrayList<ELFEntry> allELFs = new ArrayList<>();

            LOG.debug("reading ISD");
            List<ISDEntry> isdEntries = readEntries(mCard.readStatusISD(), ISDEntry.class);
            allEntries.addAll(isdEntries);
            isdEntry = isdEntries.get(0);

            LOG.debug("reading APPs and SSDs");
            List<AppEntry> appEntries = readEntries(mCard.readStatusAppsAndSD(), AppEntry.class);
            allEntries.addAll(appEntries);
            for (AppEntry appEntry : appEntries) {
                allApps.add(appEntry);
            }

            LOG.debug("reading ELFs and EXMs");
            List<ELFEntry> elfEntries = readEntries(mCard.readStatusEXMandELF(), ELFEntry.class);
            allEntries.addAll(elfEntries);
            allELFs.addAll(elfEntries);

            mISD = isdEntry;
            mAllEntries = allEntries;
            mAllApps = allApps;
            mAllELFs = allELFs;
            mAllSSDs = allSSDs;
        } catch (CardException e) {
            throw new Error("Error updating registry", e);
        }
    }

    private <E extends Entry>
    List<E> readEntries(byte[] data, Class<E> clazz) {
        List<E> res = new ArrayList<>();
        List<TLV> tlvs = TLVUtil.parseTags(data);
        for (TLV tlv : tlvs) {
            byte[] entryData = tlv.getData();
            try {
                E entry = clazz.newInstance();
                entry.read(entryData);
                res.add(entry);
            } catch (InstantiationException e) {
                throw new Error("Error instantiating registry entry", e);
            } catch (IllegalAccessException e) {
                throw new Error("Error instantiating registry entry", e);
            }
        }
        return res;
    }

    public enum Type {
        ISD, APP, SSD, ELF
    }

    public static abstract class Entry implements VerboseString {
        protected Type mType;
        protected AID mAID;
        protected byte mState;
        protected byte[] mPrivileges;
        protected List<AID> mModules;

        protected Entry(Type type) {
            mType = type;
            mModules = new ArrayList<>();
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

        public void read(byte[] data) {
            List<TLV> tlvs = TLVUtil.parseTags(data);
            List<AID> modules = new ArrayList<>();
            for (TLV tlv : tlvs) {
                int tag = tlv.getTag();
                switch (tag) {
                    case TAG_GP_REGISTRY_AID:
                        mAID = new AID(tlv.getData());
                        break;
                    case TAG_GP_REGISTRY_STATE:
                        mState = tlv.getData()[0];
                        break;
                    case TAG_GP_REGISTRY_PRIVILEGES:
                        mPrivileges = tlv.getData();
                        break;
                    case TAG_GP_REGISTRY_MODULE:
                        modules.add(new AID(tlv.getData()));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown tag " + HexUtil.hex16(tag) + "in registry entry");
                }
            }
            mModules = modules;
        }

        public String toString() {
            return mType.toString() + " " + mAID.toString();
        }

    }

    public static class AppEntry extends Entry {
        public AppEntry() {
            super(Type.APP);
        }

        protected AppEntry(Type type) {
            super(type);
        }

        public boolean isSSD() {
            return (mPrivileges[0] & GPPrivilege.SECURITY_DOMAIN.privilegeBits) != 0;
        }

        public String toVerboseString() {
            StringBuilder sb = new StringBuilder();
            sb.append(mType.toString());
            sb.append(" ");
            sb.append(mAID.toString());
            sb.append("\n  State: ");
            sb.append(GP.appletStateString(mState));
            sb.append("\n  Privileges:");
            sb.append(GPPrivilege.printPrivileges(mPrivileges, "\n    ", ""));
            return sb.toString();
        }
    }

    public static class ISDEntry extends AppEntry {
        public ISDEntry() {
            super(Type.ISD);
        }

        public String toVerboseString() {
            StringBuilder sb = new StringBuilder();
            sb.append(mType.toString());
            sb.append(" ");
            sb.append(mAID.toString());
            sb.append("\n  State: ");
            sb.append(GP.cardStateString(mState));
            sb.append("\n  Privileges:");
            sb.append(GPPrivilege.printPrivileges(mPrivileges, "\n    ", ""));
            return sb.toString();
        }
    }

    public static class ELFEntry extends Entry {
        public ELFEntry() {
            super(Type.ELF);
        }

        public String toVerboseString() {
            StringBuilder sb = new StringBuilder();
            sb.append(mType.toString());
            sb.append(" ");
            sb.append(mAID.toString());
            sb.append("\n  State: ");
            sb.append(GP.elfStateString(mState));
            if (mModules != null && !mModules.isEmpty()) {
                for (AID module : mModules) {
                    sb.append("\n  ExM ");
                    sb.append(module.toString());
                }
            }
            return sb.toString();
        }
    }

}
