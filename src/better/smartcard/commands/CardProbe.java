package better.smartcard.commands;

import better.smartcard.gp.GPContext;
import com.beust.jcommander.Parameters;

@Parameters(
        commandNames = "probe",
        commandDescription = "Probe for supported readers and cards"
)
public class CardProbe implements Runnable {

    private GPContext mContext;

    public CardProbe(GPContext context) {
        mContext = context;
    }

    @Override
    public void run() {
        mContext.findTerminals(null);
    }
}
