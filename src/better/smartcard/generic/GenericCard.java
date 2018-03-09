package better.smartcard.generic;

import better.smartcard.gp.GPCard;
import better.smartcard.protocol.ISO7816;
import better.smartcard.protocol.SWException;
import better.smartcard.util.APDUUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;

public class GenericCard {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCard.class);


    GenericContext mContext;
    CardTerminal mTerminal;
    Card mCard;

    public GenericCard(GenericContext context, CardTerminal terminal) {
        mContext = context;
        mTerminal = terminal;
        mCard = null;
    }

    public boolean isConnected() {
        return mCard != null;
    }

    public CardChannel getBasicChannel() {
        return mCard.getBasicChannel();
    }

    public void connect() throws CardException {
        mCard = mTerminal.connect("*");
    }

    /**
     * Exchange APDUs on the given channel
     *
     * @param channel to use
     * @param command to execute
     * @return response to command
     * @throws CardException for terminal and card errors
     */
    public ResponseAPDU transmit(CardChannel channel, CommandAPDU command) throws CardException {
        LOG.info("apdu > " + APDUUtil.toString(command));
        ResponseAPDU response = channel.transmit(command);
        LOG.info("apdu < " + APDUUtil.toString(response));
        return response;
    }

    /**
     * Check the given R-APDU for error codes
     *
     * GlobalPlatform is pleasantly simplistic in its error behaviour.
     *
     * Everything except for 0x9000 is an error.
     *
     * @param response to check
     * @throws CardException when the response is an error
     */
    private void checkResponse(ResponseAPDU response) throws CardException {
        int sw = response.getSW();
        if (sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error in transaction", sw);
        }
    }

}
