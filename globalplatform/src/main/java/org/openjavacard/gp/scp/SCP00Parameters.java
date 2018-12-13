package org.openjavacard.gp.scp;

public class SCP00Parameters extends SCPParameters {

    protected SCP00Parameters(int parameters) {
        super(0, parameters);

        if(parameters != 0) {
            throw new IllegalArgumentException("SCP00 does not support parameters");
        }
    }

    @Override
    public boolean isSecuritySupported(SCPSecurityPolicy securityPolicy) {
        return !(securityPolicy.requireCMAC
               ||securityPolicy.requireCENC
               ||securityPolicy.requireRMAC
               ||securityPolicy.requireRENC);
    }

}
