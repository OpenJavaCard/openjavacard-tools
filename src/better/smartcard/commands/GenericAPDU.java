package better.smartcard.commands;

import better.smartcard.generic.GenericCard;
import better.smartcard.generic.GenericContext;
import better.smartcard.protocol.ISO7816;
import better.smartcard.protocol.SWException;
import better.smartcard.util.APDUUtil;
import better.smartcard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.*;
import java.io.*;

@Parameters(
        commandNames = "apdu",
        commandDescription = "Send an APDU to the card "
)
public class GenericAPDU extends GenericCommand {

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
    String dataLiteral;

    @Parameter(
            names = "--data-file",
            description = "File containing command data"
    )
    File dataFile;

    public GenericAPDU(GenericContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GenericCard card) throws CardException {
        PrintStream os = System.out;
        CardChannel channel = card.getBasicChannel();

        if(select != null) {
            os.println("  SELECT " + select);
            byte[] said = HexUtil.hexToBytes(select);
            CommandAPDU scapdu = APDUUtil.buildCommand(
                    ISO7816.CLA_ISO7816, ISO7816.INS_SELECT,
                    SELECT_P1_BY_NAME, SELECT_P2_FIRST,
                    said);
            ResponseAPDU srapdu = card.transmit(channel, scapdu);
            int sw = srapdu.getSW();
            if(sw != ISO7816.SW_NO_ERROR) {
                throw new SWException("Error selecting", sw);
            }
        }

        String capduStr = apdu;
        capduStr = capduStr.replace(":", "");
        capduStr = capduStr.replace(" ", "");
        if(dataLiteral != null) {
            byte[] data = HexUtil.hexToBytes(dataLiteral);
            capduStr += HexUtil.hex8(data.length);
            capduStr += dataLiteral;
        }
        if(dataFile != null) {
            if(dataFile.exists() && dataFile.isFile()) {
                try {
                    FileInputStream fis = new FileInputStream(dataFile);
                    int len = fis.available();
                    if(len > 255) {
                        throw new RuntimeException("Data file to long");
                    }
                    byte[] data = new byte[len];
                    fis.read(data);
                    capduStr += HexUtil.hex8(len);
                    capduStr += HexUtil.bytesToHex(data);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        byte[] capduBin = HexUtil.hexToBytes(capduStr);
        CommandAPDU capdu = new CommandAPDU(capduBin);
        ResponseAPDU rapdu = card.transmit(channel, capdu);
        int sw = rapdu.getSW();
        if(sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error executing command", sw);
        }
    }

}
