package better.smartcard.gp.keys;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.protocol.GPKeyInfo;
import better.smartcard.gp.protocol.GPKeyInfoEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;

public class GPKeyStore {

    private static final Logger LOG = LoggerFactory.getLogger(GPKeyStore.class);

    public GPKeyStore() {
    }

    GPKeySet selectKeys(GPCard card) {
        String cardId = card.getCardIdentifier();
        LOG.info("selecting keys for " + cardId);
        GPKeyInfo ki = card.getCardKeyInfo();
        for(GPKeyInfoEntry ke: ki.getKeyInfos()) {
            int keyId = ke.getKeyId();
            int keyVersion = ke.getKeyVersion();
            LOG.info("need key id " + keyId + " version " + keyVersion);
            String name = "globalplatform/" + cardId + "/version-" + keyVersion + "/id-" + keyId;
            LOG.info("querying for " + name);
        }
        return null;
    }

}
