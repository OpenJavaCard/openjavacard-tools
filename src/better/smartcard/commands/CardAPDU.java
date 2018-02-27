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

    public static final byte SELECT_P1_BY_NAME = 0x04;
    public static final byte SELECT_P2_FIRST = 0x00;

    @Parameter(
            names = "--raw",
            description = "APDU to execute",
            required = true
    )
    String apdu;

    @Parameter(
            names = "--select",
            description = "Applet to select before executing"
    )
    String select;

    @Parameter(
            names = "--data",
            description = "Command data"
    )
    String apduData;

    public CardAPDU(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(Card card) throws CardException {
        PrintStream os = System.out;
        CardChannel channel = card.getBasicChannel();

        if(select != null) {
            byte[] said = HexUtil.hexToBytes(select);
            CommandAPDU scapdu = APDUUtil.buildCommand(
                    ISO7816.CLA_ISO7816, ISO7816.INS_SELECT,
                    SELECT_P1_BY_NAME, SELECT_P2_FIRST,
                    said);
            ResponseAPDU srapdu = channel.transmit(scapdu);
            int sw = srapdu.getSW();
            if(sw != ISO7816.SW_NO_ERROR) {
                throw new SWException("Error selecting", sw);
            }
        }

        //for(String capduStr: apdus) {
            String capduStr = apdu;
            capduStr = capduStr.replace(":", "");
            capduStr = capduStr.replace(" ", "");
            if(apduData != null) {
                byte[] data = HexUtil.hexToBytes(apduData);
                capduStr += HexUtil.hex8(data.length);
                capduStr += apduData;
            }
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
        //}
    }

}
