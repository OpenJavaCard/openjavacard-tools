package org.openjavacard.tool.command.base;

import com.beust.jcommander.Parameter;
import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.scp.SCPProtocolPolicy;
import org.openjavacard.iso.AID;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;
import java.io.PrintStream;

public abstract class BasicGPCommand extends BasicSCPCommand {

    @Parameter(
            names = "--isd", order = 200,
            description = "AID of the issuer security domain"
    )
    protected AID isd;

    @Parameter(
            names = "--force-protected", order = 800,
            description = "Force operation on protected object"
    )
    protected boolean forceProtected = false;

    @Parameter(
            names = "--log-keys", order = 900,
            description = "Allow writing keys into the debug log"
    )
    protected boolean logKeys = false;

    GPContext mContext;
    GPCard mCard;

    protected abstract void performOperation(GPContext context, GPCard card) throws CardException;

    @Override
    protected void beforeExecute() throws Exception {
        super.beforeExecute();

        PrintStream os = System.out;

        mContext = new GPContext();
        mCard = findSingleGPCard(isd, getKeySet());

        AID isdConf = mCard.getISD();
        os.println("Host GP configuration:");
        os.println("  ISD " + ((isdConf==null)?"auto":isdConf));
        int protocol = HexUtil.unsigned8(scpProtocol);
        int parameters = HexUtil.unsigned8(scpParameters);
        SCPProtocolPolicy protocolPolicy = new SCPProtocolPolicy(protocol, parameters);
        os.println("  Key diversification " + scpDiversification);
        mCard.setDiversification(scpDiversification);
        os.println("  Protocol policy " + protocolPolicy);
        mCard.setProtocolPolicy(protocolPolicy);
        os.println("  Security policy " + scpSecurity);
        mCard.setSecurityPolicy(scpSecurity);

        mCard.connect();
    }

    @Override
    protected void execute() throws Exception {
        super.execute();
        performOperation(mContext, mCard);
    }

    @Override
    protected void afterExecute() throws Exception {
        super.afterExecute();
        PrintStream os = System.out;

        os.println("DISCONNECTING");
        mCard.disconnect();
        os.println();
    }

    public GPCard findSingleGPCard(AID sd, GPKeySet keys) {
        GPCard card;
        try {
            // check card presence
            if (!mTerminal.isCardPresent()) {
                throw new Error("No card present in terminal");
            }
            // create GP client
            card = new GPCard(mContext, mTerminal);
            // tell the client if we know the SD AID
            if(sd != null) {
                card.setISD(sd);
            }
            if(keys != null) {
                card.setKeys(keys);
            }
            // detect GP applet
            //boolean detected = card.detect();
            //if (!detected) {
            //    throw new Error("Could not find a GlobalPlatform applet on the card");
            //}
        } catch (CardException e) {
            throw new Error("Error detecting card", e);
        }
        return card;
    }

}
