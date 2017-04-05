package better.smartcard.gp.scp;

import better.smartcard.util.HexUtil;

import javax.smartcardio.CardException;

/**
 * Representation of an SCP protocol selection policy
 *
 * This has been made explicit because card provisioning
 * systems may be confronted with several protocol versions
 * on different cards to prevent downgrade attacks.
 *
 * One such attack would be for an (attacker) card to
 * indicate SCP01 even though the card that the system
 * believes is presented is supposed to support SCP02.
 *
 * In that case the SCP02 keys would be used with SCP01
 * and consequently with less cryptographic protection.
 */
public class SCPPolicy {

    /**
     * Policy that allows any protocol to be used
     */
    public static final SCPPolicy PERMISSIVE = new SCPPolicy(0, 0);

    /**
     * SCP version to be used
     */
    int mScpVersion = 0;

    /**
     * SCP parameters to be used
     */
    int mScpParameters = 0;

    /**
     * Construct an SCP policy allowing only the indicated version and parameters
     * @param scpVersion to allow
     * @param scpParameters to allow
     */
    public SCPPolicy(int scpVersion, int scpParameters) {
        mScpVersion = scpVersion;
        mScpParameters = scpParameters;
    }

    /**
     * Stringify the policy
     */
    public String toString() {
        String version = mScpVersion == 0 ? "*" : HexUtil.hex8(mScpVersion);
        String parameters = mScpParameters == 0 ? "*" : HexUtil.hex8(mScpParameters);
        return "SCP" + version + "(" + parameters + ")";
    }

    /** @return true if the given SCP version is allowed */
    public boolean isVersionAllowed(int version) {
        return mScpVersion == 0 || mScpVersion == version;
    }

    /** @return true if the given SCP parameters are allowed */
    public boolean isParametersAllowed(int parameters) {
        return mScpParameters == 0 || mScpParameters == parameters;
    }

    /** @return true if the given SCP protocol is allowed */
    public boolean isProtocolAllowed(int version, int parameters) {
        return isVersionAllowed(version) && isParametersAllowed(parameters);
    }

    /**
     * Check the given SCP protocol against policy
     *
     * @throws CardException if the protocol is denied use
     */
    public void checkProtocol(int version, int parameters) throws CardException {
        if (!isProtocolAllowed(version, parameters)) {
            throw new CardException("Protocol SCP" + HexUtil.hex8(version)
                    + "(" + HexUtil.hex8(parameters) + ")"
                    + " denied by policy");
        }
    }

}
