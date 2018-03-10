package better.smartcard.gp.scp;

import better.smartcard.util.HexUtil;

import javax.smartcardio.CardException;

/**
 * Represents SCP parameters for SCP01 and SCP02
 */
public class SCP0102Protocol extends SCPProtocol {

    /** Number of keys required (usually 1 or 3) */
    public final int numKeys;
    /** If true then C-MAC is computed on the unmodified CardAPDU (original CLA, INS, length) */
    public final boolean cmacUnmodified;
    /** If true then initialization must be explicit*/
    public final boolean initExplicit;
    /** If true then the ICV (initial IV) is computed as MAC(AID), else it is MAC(ZEROBLOCK) */
    public final boolean icvMACAID;
    /** If true then the ICV goes through an additional encryption step */
    public final boolean icvEncrypt;
    /** True if R-MAC is supported */
    public final boolean rmacSupport;
    /** True if the card uses the well-known deterministic challenge generator */
    public final boolean wellKnown;

    /**
     * Parse the given protocol parameters
     * @param protocol to be used
     * @param parameters to be used
     */
    SCP0102Protocol(int protocol, int parameters) {
        super(protocol, parameters);

        switch (protocol) {
            case 1:
                numKeys = 3;
                cmacUnmodified = true;
                initExplicit = true;
                icvMACAID = false;
                rmacSupport = false;
                wellKnown = false;
                switch (parameters) {
                    case 0x05:
                        icvEncrypt = false;
                        break;
                    case 0x15:
                        icvEncrypt = true;
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                "Unknown SCP" + HexUtil.hex8(protocol)
                                        + "(" + HexUtil.hex8(parameters) + ")");
                }
                break;
            case 2:
                numKeys = ((parameters & 0x01) != 0) ? 3 : 1;
                cmacUnmodified = (parameters & 0x02) != 0;
                initExplicit = (parameters & 0x04) != 0;
                icvMACAID = (parameters & 0x08) != 0;
                icvEncrypt = (parameters & 0x10) != 0;
                rmacSupport = (parameters & 0x20) != 0;
                wellKnown = (parameters & 0x40) != 0;
                if ((parameters & 0x80) != 0) {
                    throw new UnsupportedOperationException("Unknown SCP02 parameter 0x80");
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown protocol SCP" + HexUtil.hex8(protocol));
        }
    }

    @Override
    public void checkSecuritySupported(SCPSecurityPolicy securityPolicy) throws CardException {
        /* CMAC and CENC are always supported in SCP01/02 */
        /* check for RMAC */
        if(securityPolicy.requireRMAC) {
            if(!rmacSupport) {
                throw new CardException("Security policy error: card does not support RMAC");
            }
        }
        /* check for RENC */
        if(securityPolicy.requireRENC) {
            throw new CardException("Security policy error: SCP01/02 does not support RENC");
        }
    }

    @Override
    public String toVerboseString() {
        String shortString = toString();
        if (scpParameters != 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Secure Channel Protocol " + shortString + ":");
            sb.append("\n  " + numKeys + " keys used");
            if (cmacUnmodified) {
                sb.append("\n  CMAC unmodified CardAPDU");
            } else {
                sb.append("\n  CMAC modified CardAPDU");
            }
            if (initExplicit) {
                sb.append("\n  explicit initiation");
            } else {
                sb.append("\n  implicit initiation");
            }
            if (icvMACAID) {
                sb.append("\n  ICV initialized to MAC(AID)");
            } else {
                sb.append("\n  ICV initialized to null");
            }
            if (icvEncrypt) {
                sb.append("\n  ICV encryption");
            } else {
                sb.append("\n  no ICV encryption");
            }
            if (rmacSupport) {
                sb.append("\n  RMAC support");
            } else {
                sb.append("\n  no RMAC support");
            }
            if (wellKnown) {
                sb.append("\n  Well-known challenge generator");
            }
            return sb.toString();
        } else {
            return shortString;
        }
    }

}
