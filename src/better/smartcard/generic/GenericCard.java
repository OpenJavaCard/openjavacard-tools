package better.smartcard.generic;

import better.smartcard.iso.ISO7816;
import better.smartcard.iso.SWException;
import better.smartcard.util.APDUUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;

public class GenericCard {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCard.class);

    GenericContext mContext;
    CardTerminal mTerminal;
    Card mCard;
    CardChannel mBasic;

    public GenericCard(GenericContext context, CardTerminal terminal) {
        mContext = context;
        mTerminal = terminal;
        mCard = null;
    }

    public CardTerminal getTerminal() {
        return mTerminal;
    }

    public boolean isConnected() {
        return mCard != null;
    }

    public Card getCard() {
        return mCard;
    }

    public CardChannel getBasicChannel() {
        return mCard.getBasicChannel();
    }

    public void connect() throws CardException {
        mCard = mTerminal.connect("*");
        mBasic = mCard.getBasicChannel();
    }

    public void reconnect(boolean reset) throws CardException {
        mCard.disconnect(reset);
        connect();
    }

    public void disconnect() {
        try {
            mCard.disconnect(false);
            mCard = null;
        } catch (CardException e) {
        }
    }

    public ResponseAPDU performSelectByName(byte[] name, boolean first) throws CardException {
        byte p2 = first ? ISO7816.SELECT_P2_FIRST_OR_ONLY : ISO7816.SELECT_P2_NEXT;
        return performSelect(ISO7816.SELECT_P1_BY_NAME, p2, name);
    }

    private ResponseAPDU performSelect(byte p1, byte p2, byte[] data) throws CardException {
        CommandAPDU scapdu = APDUUtil.buildCommand(
                ISO7816.CLA_ISO7816, ISO7816.INS_SELECT,
                p1, p2, data);
        return transmitAndCheck(scapdu);
    }

    public ResponseAPDU transmit(CommandAPDU command) throws CardException {
        return transmit(mBasic, command);
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
        LOG.debug("apdu > " + APDUUtil.toString(command));
        ResponseAPDU response = channel.transmit(command);
        LOG.debug("apdu < " + APDUUtil.toString(response));
        return response;
    }

    public ResponseAPDU transmitAndCheck(CommandAPDU command) throws CardException {
        return transmitAndCheck(mBasic, command);
    }

    public ResponseAPDU transmitAndCheck(CardChannel channel, CommandAPDU command) throws CardException {
        ResponseAPDU response = transmit(channel, command);
        checkResponse(response);
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
