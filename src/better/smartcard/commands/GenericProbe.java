package better.smartcard.commands;

import better.smartcard.generic.GenericContext;
import better.smartcard.gp.GPContext;
import com.beust.jcommander.Parameters;

@Parameters(
        commandNames = "probe",
        commandDescription = "Probe for supported readers and cards"
)
public class GenericProbe implements Runnable {

    private GenericContext mContext;

    public GenericProbe(GenericContext context) {
        mContext = context;
    }

    @Override
    public void run() {
        mContext.findTerminals(null);
    }

}
