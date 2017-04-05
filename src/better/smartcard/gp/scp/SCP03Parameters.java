package better.smartcard.gp.scp;

public class SCP03Parameters extends SCPParameters {

    public final boolean pseudoRandomChallenge;

    public final boolean rmacSupport;

    public final boolean rencSupport;

    /**
     * Main constructor
     *
     * @param parameters
     */
    protected SCP03Parameters(int parameters) {
        super(3, parameters);

        pseudoRandomChallenge = ((parameters & 0x10) != 0);
        rmacSupport = ((parameters & 0x20) != 0);
        rencSupport = ((parameters & 0x40) != 0);
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
