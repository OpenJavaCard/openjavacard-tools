package better.smartcard.gp.scp;

import better.smartcard.util.HexUtil;
import better.smartcard.util.VerboseString;

/**
 * Represents a complete set of parameters for SCP
 * <p/>
 * This is a base class, SCP implementations should subclass it.
 */
public abstract class SCPParameters implements VerboseString {

    /**
     * Decode SCP parameters
     *
     * @param scpVersion    for decoding
     * @param scpParameters for decoding
     * @return an object describing the parameters
     */
    public static SCPParameters decode(int scpVersion, int scpParameters) {
        switch(scpVersion) {
            case 1:
            case 2:
                return new SCP0102Parameters(scpVersion, scpParameters);
            case 3:
                return new SCP03Parameters(scpParameters);
            default:
                throw new UnsupportedOperationException("Unknown protocol SCP" + HexUtil.hex8(scpVersion));
        }
    }

    /**
     * SCP protocol
     */
    public final int scpProtocol;
    /**
     * SCP parameter specification
     */
    public final int scpParameters;

    /**
     * Main constructor
     *
     * @param protocol
     * @param parameters
     */
    protected SCPParameters(int protocol, int parameters) {
        scpProtocol = protocol;
        scpParameters = parameters;
    }

    /**
     * Stringify the parameters
     */
    public String toString() {
        String protocol = scpProtocol == 0 ? "??" : HexUtil.hex8(scpProtocol);
        String parameters = scpParameters == 0 ? "??" : HexUtil.hex8(scpParameters);
        return "SCP" + protocol + "(" + parameters + ")";
    }

    /**
     * Stringify the parameters verbosely
     */
    @Override
    public String toVerboseString() {
        return "Protocol " + toString();
    }

}
