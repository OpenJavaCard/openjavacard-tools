package better.smartcard.gp.scp;

import javax.smartcardio.CardException;

public class SCP03Protocol extends SCPProtocol {

    public final boolean pseudoRandomChallenge;

    public final boolean rmacSupport;

    public final boolean rencSupport;

    /**
     * Main constructor
     *
     * @param parameters
     */
    protected SCP03Protocol(int parameters) {
        super(3, parameters);

        pseudoRandomChallenge = ((parameters & 0x10) != 0);
        rmacSupport = ((parameters & 0x20) != 0);
        rencSupport = ((parameters & 0x40) != 0);
    }

    @Override
    public void checkSecuritySupported(SCPSecurityPolicy securityPolicy) throws CardException {
        /* CMAC and CENC are always supported in SCP03 */
        /* check for RMAC */
        if(securityPolicy.requireRMAC) {
            if(!rmacSupport) {
                throw new CardException("Security policy error: card does not support RMAC");
            }
        }
        /* check for RENC */
        if(securityPolicy.requireRENC) {
            if(!rencSupport) {
                throw new CardException("Security policy error: card does not support RENC");
            }
        }
    }

    @Override
    public String toVerboseString() {
        String shortString = toString();
        if (scpParameters != 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Secure Channel Protocol " + shortString + ":");
            if (pseudoRandomChallenge) {
                sb.append("\n  Pseudo-random challenge");
            } else {
                sb.append("\n  Random challenge");
            }
            if (rmacSupport) {
                sb.append("\n  RMAC support");
            } else {
                sb.append("\n  no RMAC support");
            }
            if (rencSupport) {
                sb.append("\n  RENC support");
            } else {
                sb.append("\n  no RENC support");
            }
            return sb.toString();
        } else {
            return shortString;
        }
    }

}
