package better.smartcard.gp.protocol;

import better.smartcard.gp.keys.GPKey;
import better.smartcard.gp.keys.GPKeySet;
import better.smartcard.tlv.TLV;
import better.smartcard.tlv.TLVUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * GlobalPlatform Key Information Template
 * <p/>
 * This describes the set of keys that a card holds
 * and/or expects from potential secure hosts.
 * <p/>
 * It contains several Key Information blocks,
 * each of which describes one key with its own ID.
 */
public class GPKeyInfo {

    private static final int TAG_KEY_INFO_TEMPLATE = 0xE0;
    private static final int TAG_KEY_INFO = 0xC0;

    List<GPKeyInfoEntry> mKeyInfos = new ArrayList<>();

    public GPKeyInfo() {
    }

    public List<GPKeyInfoEntry> getKeyInfos() {
        return new ArrayList<>(mKeyInfos);
    }

    public boolean matchesKeysetForUsage(GPKeySet keys) {
        for (GPKeyInfoEntry keyInfo : mKeyInfos) {
            GPKey key = keys.getKeyById(keyInfo.getKeyId());
            if (key == null) {
                return false;
            }
            int keyVersion = keys.getKeyVersion();
            if (keyVersion != 0 && keyInfo.getKeyVersion() != keyVersion) {
                return false;
            }
            if (!keyInfo.matchesKey(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesKeysetForReplacement(GPKeySet keys) {
        for (GPKeyInfoEntry keyInfo : mKeyInfos) {
            GPKey key = keys.getKeyById(keyInfo.getKeyId());
            if (key == null) {
                return false;
            }
            if (!keyInfo.matchesKey(key)) {
                return false;
            }
        }
        return true;
    }

    public void read(byte[] buf) {
        TLV kit = TLVUtil.parseTag(TAG_KEY_INFO_TEMPLATE, buf);
        List<TLV> tlvs = TLVUtil.parseTags(kit.getData());
        ArrayList<GPKeyInfoEntry> infos = new ArrayList<>();
        for (TLV tlv : tlvs) {
            int tag = tlv.getTag();
            if (tag != TAG_KEY_INFO) {
                throw new Error("Invalid key info template - unknown tag "
                        + TLVUtil.stringTag(tag) + ", expected" + TLVUtil.stringTag(TAG_KEY_INFO));
            } else {
                GPKeyInfoEntry info = new GPKeyInfoEntry();
                info.read(tlv.getData());
                infos.add(info);
            }
        }
        mKeyInfos = infos;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GP Key Information Template:");
        for (GPKeyInfoEntry info : mKeyInfos) {
            sb.append("\n  ");
            sb.append(info.toString());
        }
        return sb.toString();
    }

}
