package better.smartcard.commands;

import better.smartcard.gp.GPContext;
import better.smartcard.protocol.ISO7816;
import better.smartcard.protocol.SWException;
import better.smartcard.util.APDUUtil;
import better.smartcard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.*;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "apdu",
        commandDescription = "Send an APDU to the card "
)
public class CardAPDU extends CardCommand {

    @Parameter(
            description = "APDUs to execute",
            required = true
    )
    List<String> apdus;

    public CardAPDU(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(Card card) throws CardException {
        PrintStream os = System.out;
        CardChannel channel = card.getBasicChannel();
        for(String capduStr: apdus) {
            capduStr = capduStr.replace(":", "");
            capduStr = capduStr.replace(" ", "");
            byte[] capduBin = HexUtil.hexToBytes(capduStr);
            CommandAPDU capdu = new CommandAPDU(capduBin);
            os.println("> " + APDUUtil.toString(capdu));
            ResponseAPDU rapdu = channel.transmit(capdu);
            os.println("< " + APDUUtil.toString(rapdu));
            os.println();
            int sw = rapdu.getSW();
            if(sw != ISO7816.SW_NO_ERROR) {
                throw new SWException("Error executing command", sw);
            }
        }
    }

}
