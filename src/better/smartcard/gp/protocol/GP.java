package better.smartcard.gp.protocol;

import better.smartcard.iso.ISO7816;
import better.smartcard.util.HexUtil;

/**
 * Protocol constants for GlobalPlatform
 * <p/>
 * This contains protocol constants for GlobalPlatform,
 * combined to support at least version 2.1.1 of the spec.
 */
public class GP implements ISO7816 {

    public static final byte CLA_ISO = (byte) CLA_ISO7816;
    public static final byte CLA_GP  = (byte) 0x80;
    public static final byte CLA_MAC = (byte) 0x84;

    public static final byte INS_INITIALIZE_UPDATE = (byte) 0x50;

    public static final byte INS_EXTERNAL_AUTHENTICATE = ISO7816.INS_EXTERNAL_AUTHENTICATE;
    public static final byte EXTERNAL_AUTHENTICATE_P1_MAC = 0x01;
    public static final byte EXTERNAL_AUTHENTICATE_P1_ENC = 0x02;
    public static final byte EXTERNAL_AUTHENTICATE_P1_RMAC = 0x10;
    public static final byte EXTERNAL_AUTHENTICATE_P1_RENC = 0x20;

    public static final byte INS_SELECT = ISO7816.INS_SELECT;
    public static final byte SELECT_P1_BY_NAME = ISO7816.SELECT_P1_BY_NAME;
    public static final byte SELECT_P2_FIRST_OR_ONLY = ISO7816.SELECT_P2_FIRST_OR_ONLY;
    public static final byte SELECT_P2_NEXT = ISO7816.SELECT_P2_NEXT;

    public static final byte INS_MANAGE_CHANNEL = (byte) 0x70;

    public static final byte INS_GET_DATA = (byte) 0xCA;

    /* use for arbitrary tags */
    public static final short get_data_p12(int tag) {
        return (short) tag;
    }

    /* mandatory */
    public static final short GET_DATA_P12_ISSUER_ID_NUMBER = (short) 0x0042;
    public static final short GET_DATA_P12_CARD_IMG_NUMBER = (short) 0x0045;
    public static final short GET_DATA_P12_CARD_DATA = (short) 0x0066;
    public static final short GET_DATA_P12_KEY_INFO_TEMPLATE = (short) 0x00E0;
    /* optional */
    public static final short GET_DATA_P12_CURRENT_SEC_LEVEL = (short) 0x00D3;
    public static final short GET_DATA_P12_APPLICATION_INFO = (short) 0x2F00;
    public static final short GET_DATA_P12_EXTENDED_CARD_RES_INFO = (short) 0xFF12;
    /* when SD has receipt generation privilege */
    public static final short GET_DATA_P12_CONFIRMATION_COUNTER = (short) 0x00C2;
    /* for SCP02 */
    public static final short GET_DATA_P12_KVN_SEQUENCE_COUNTER = (short) 0x00C1;
    /* Card production life cycle data (CPLC) */
    public static final short GET_DATA_P12_CPLC = (short) 0x9F7F;

    public static final byte INS_PUT_KEY = (byte) 0xD8;

    public static final byte INS_STORE_DATA = (byte) 0xE2;
    public static final byte STORE_DATA_P1_MORE_BLOCKS = (byte)0x00;
    public static final byte STORE_DATA_P1_LAST_BLOCK = (byte)0x80;

    public static final byte INS_DELETE = (byte) 0xE4;
    public static final byte DELETE_P2_DELETE_INDICATED = (byte) 0x00;
    public static final byte DELETE_P2_DELETE_RELATED = (byte) 0x80;

    public static final byte INS_INSTALL = (byte) 0xE6;
    public static final byte INSTALL_P1_LAST_OR_ONLY = (byte)0x00;
    public static final byte INSTALL_P1_EXPECT_MORE = (byte)0x80;
    public static final byte INSTALL_P1_FOR_REGISTRY_UPDATE = (byte)0x40;
    public static final byte INSTALL_P1_FOR_PERSONALIZATION = (byte)0x20;
    public static final byte INSTALL_P1_FOR_EXTRADITION = (byte)0x10;
    public static final byte INSTALL_P1_FOR_MAKE_SELECTABLE = (byte)0x08;
    public static final byte INSTALL_P1_FOR_INSTALL = (byte)0x04;
    public static final byte INSTALL_P1_FOR_LOAD = (byte)0x02;
    public static final byte INSTALL_P2_NO_INFORMATION = (byte)0x00;
    public static final byte INSTALL_P2_BEGIN_LIMS_SEQUENCE = (byte)0x01;
    public static final byte INSTALL_P2_FINISH_LIMS_SEQUENCE = (byte)0x03;

    public static final byte INS_LOAD = (byte) 0xE8;
    public static final byte LOAD_P1_MORE_BLOCKS = (byte)0x00;
    public static final byte LOAD_P1_LAST_BLOCK = (byte)0x80;

    public static final byte INS_SET_STATUS = (byte) 0xF0;
    public static final byte SET_STATUS_FOR_ISD = (byte)0x80;
    public static final byte SET_STATUS_FOR_SSD_OR_APP = (byte)0x40;
    public static final byte SET_STATUS_FOR_SD_AND_APPS = (byte)0x60;

    public static final byte INS_GET_STATUS = (byte) 0xF2;
    public static final byte GET_STATUS_P1_ISD_ONLY = (byte) 0x80;
    public static final byte GET_STATUS_P1_APP_AND_SD_ONLY = (byte) 0x40;
    public static final byte GET_STATUS_P1_ELF_ONLY = (byte) 0x20;
    public static final byte GET_STATUS_P1_EXM_AND_ELF_ONLY = (byte) 0x10;
    public static final byte GET_STATUS_P2_GET_FIRST_OR_ALL = (byte) 0x00;
    public static final byte GET_STATUS_P2_GET_NEXT = (byte) 0x01;
    public static final byte GET_STATUS_P2_FORMAT_LEGACY = (byte) 0x00;
    public static final byte GET_STATUS_P2_FORMAT_TLV = (byte) 0x02;

    public static final byte ELF_STATE_LOADED = (byte) 0x01;

    public static String elfStateString(byte elfState) {
        switch (elfState) {
            case ELF_STATE_LOADED:
                return "LOADED";
            default:
                return "UNKNOWN" + elfState;
        }
    }

    public static final byte APPLET_STATE_INSTALLED = (byte) 0x03;
    public static final byte APPLET_STATE_SELECTABLE = (byte) 0x07;

    public static String appletStateString(byte appState) {
        switch (appState) {
            case APPLET_STATE_INSTALLED:
                return "INSTALLED";
            case APPLET_STATE_SELECTABLE:
                return "SELECTABLE";
            default:
                return "UNKNOWN" + appState;
        }
    }

    public static final byte DOMAIN_STATE_INSTALLED = (byte) 0x03;
    public static final byte DOMAIN_STATE_SELECTABLE = (byte) 0x07;
    public static final byte DOMAIN_STATE_PERSONALIZED = (byte) 0x0F;

    public static String domainStateString(byte domainState) {
        switch (domainState) {
            case DOMAIN_STATE_INSTALLED:
                return "INSTALLED";
            case DOMAIN_STATE_SELECTABLE:
                return "SELECTABLE";
            case DOMAIN_STATE_PERSONALIZED:
                return "PERSONALIZED";
            default:
                return "UNKNOWN" + domainState;
        }
    }

    public static final byte CARD_STATE_OP_READY = (byte) 0x01;
    public static final byte CARD_STATE_INITIALIZED = (byte) 0x07;
    public static final byte CARD_STATE_SECURED = (byte) 0x0F;
    public static final byte CARD_STATE_LOCKED = (byte) 0x7F;
    public static final byte CARD_STATE_TERMINATED = (byte) 0xFF;

    public static String cardStateString(byte cardState) {
        switch (cardState) {
            case CARD_STATE_OP_READY:
                return "OP_READY";
            case CARD_STATE_INITIALIZED:
                return "INITIALIZED";
            case CARD_STATE_SECURED:
                return "SECURED";
            case CARD_STATE_LOCKED:
                return "LOCKED";
            case CARD_STATE_TERMINATED:
                return "TERMINATED";
            default:
                return "UNKNOWN" + cardState;
        }
    }

    public static final byte KEY_TYPE_DES = (byte) 0x80;
    public static final byte KEY_TYPE_3DES_CBC = (byte) 0x82;
    public static final byte KEY_TYPE_DES_ECB = (byte) 0x83;
    public static final byte KEY_TYPE_DES_CBC = (byte) 0x84;
    public static final byte KEY_TYPE_TLS_PSK = (byte) 0x85;
    public static final byte KEY_TYPE_AES = (byte) 0x88;
    public static final byte KEY_TYPE_HMAC_SHA1 = (byte) 0x90;
    public static final byte KEY_TYPE_HMAC_SHA1_160 = (byte) 0x91;
    public static final byte KEY_TYPE_RSA_PUBLIC_EXPONENT_CLEARTEXT = (byte) 0xA0;
    public static final byte KEY_TYPE_RSA_MODULUS_CLEARTEXT = (byte) 0xA1;
    public static final byte KEY_TYPE_RSA_MODULUS = (byte) 0xA2;
    public static final byte KEY_TYPE_RSA_PRIVATE_EXPONENT_D = (byte) 0xA3;
    public static final byte KEY_TYPE_RSA_CHINESE_P = (byte) 0xA4;
    public static final byte KEY_TYPE_RSA_CHINESE_Q = (byte) 0xA5;
    public static final byte KEY_TYPE_RSA_CHINESE_PQ = (byte) 0xA6;
    public static final byte KEY_TYPE_RSA_CHINESE_DPI = (byte) 0xA7;
    public static final byte KEY_TYPE_RSA_CHINESE_DQI = (byte) 0xA8;
    public static final byte KEY_TYPE_EXTENDED_FORMAT = (byte) 0xFF;

    public static String keyTypeString(byte keyType) {
        switch (keyType) {
            case KEY_TYPE_DES:
                return "DES";
            case KEY_TYPE_3DES_CBC:
                return "3DES-CBC";
            case KEY_TYPE_DES_ECB:
                return "DES-ECB";
            case KEY_TYPE_DES_CBC:
                return "DES-CBC";
            case KEY_TYPE_TLS_PSK:
                return "TLS-PSK";
            case KEY_TYPE_AES:
                return "AES";
            case KEY_TYPE_HMAC_SHA1:
                return "HMAC-SHA1";
            case KEY_TYPE_HMAC_SHA1_160:
                return "HMAC-SHA1-160";
            case KEY_TYPE_RSA_PUBLIC_EXPONENT_CLEARTEXT:
                return "RSA-PUBLIC-EXPONENT-CLEARTEXT";
            case KEY_TYPE_RSA_MODULUS_CLEARTEXT:
                return "RSA-MODULUS-CLEARTEXT";
            case KEY_TYPE_RSA_MODULUS:
                return "RSA-MODULUS";
            case KEY_TYPE_RSA_PRIVATE_EXPONENT_D:
                return "RSA-PRIVATE-EXPONENT-D";
            case KEY_TYPE_RSA_CHINESE_P:
                return "RSA-CHINESE-P";
            case KEY_TYPE_RSA_CHINESE_Q:
                return "RSA-CHINESE-Q";
            case KEY_TYPE_RSA_CHINESE_PQ:
                return "RSA-CHINESE-PQ";
            case KEY_TYPE_RSA_CHINESE_DPI:
                return "RSA-CHINESE-DPI";
            case KEY_TYPE_RSA_CHINESE_DQI:
                return "RSA-CHINESE-DQI";
            case KEY_TYPE_EXTENDED_FORMAT:
                return "EXTENDED-FORMAT";
            default:
                return "UNKNOWN" + HexUtil.hex8(keyType);
        }
    }

    public static final byte PRIV1_SECURITY_DOMAIN = (byte) 0x80;
    public static final byte PRIV1_DAP_VERIFICATION = (byte) 0x40;
    public static final byte PRIV1_DELEGATE_MANAGEMENT = (byte) 0x20;
    public static final byte PRIV1_CARD_LOCK = (byte) 0x10;
    public static final byte PRIV1_CARD_TERMINATE = (byte) 0x08;
    public static final byte PRIV1_CARD_RESET = (byte) 0x04;
    public static final byte PRIV1_CVM_MANAGEMENT = (byte) 0x02;
    public static final byte PRIV1_DAP_MANDATORY = (byte) 0x01;

    public static final byte PRIV2_TRUSTED_PATH = (byte) 0x80;
    public static final byte PRIV2_AUTHORIZED_MANAGEMENT = (byte) 0x40;
    public static final byte PRIV2_TOKEN_MANAGEMENT = (byte) 0x20;
    public static final byte PRIV2_GLOBAL_DELETE = (byte) 0x10;
    public static final byte PRIV2_GLOBAL_LOCK = (byte) 0x08;
    public static final byte PRIV2_GLOBAL_REGISTRY = (byte) 0x04;
    public static final byte PRIV2_FINAL_APPLICATION = (byte) 0x02;
    public static final byte PRIV2_GLOBAL_SERVICE = (byte) 0x01;

    public static final byte PRIV3_RECEIPT_GENERATION = (byte) 0x80;
    public static final byte PRIV3_CIPHERED_LOAD_DATA_BLOCK = (byte) 0x40;
    public static final byte PRIV3_CONTACTLESS_ACTIVATION = (byte) 0x20;
    public static final byte PRIV3_CONTACTLESS_SELF_ACTIVATION = (byte) 0x10;

}
