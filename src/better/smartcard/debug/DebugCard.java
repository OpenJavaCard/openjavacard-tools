package better.smartcard.debug;

import better.smartcard.debug.record.LogEntry;
import better.smartcard.debug.record.LogStatus;
import better.smartcard.debug.record.MemoryStatus;
import better.smartcard.protocol.ISO7816;
import better.smartcard.protocol.SWException;
import better.smartcard.util.AID;
import better.smartcard.util.APDUUtil;
import org.aispring.javacard.debug.protocol.DebugProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;
import java.util.ArrayList;
import java.util.List;

public class DebugCard {

    private static final Logger LOG = LoggerFactory.getLogger(DebugCard.class);

    private static final AID AID_DEBUG   = new AID("a000000290ff0201");
    private static final AID AID_MEMSTAT = new AID("a000000290ff0301");

    private static final byte CLA_ISO = (byte)0x00;

    private static final byte INS_SELECT = (byte)0xA4;
    public static final byte SELECT_P1_BY_NAME = (byte)0x04;
    public static final byte SELECT_P2_FIRST   = (byte)0x00;

    boolean mDetectionDone;

    AID mAID;

    boolean mMemoryOnly;

    Card mCard;

    CardChannel mChannel;

    public DebugCard(CardChannel channel) {
        mCard = channel.getCard();
        mChannel = channel;
    }

    public void detect() throws CardException {
        AID aid = null;
        boolean memoryOnly = false;

        if(mDetectionDone) {
            return;
        }

        ResponseAPDU dbgResponse = transact(buildSelectFileByName(AID_DEBUG));
        if(dbgResponse.getSW() == ISO7816.SW_NO_ERROR) {
            aid = AID_DEBUG;
            memoryOnly = false;
        }

        if(aid == null) {
            ResponseAPDU memResponse = transact(buildSelectFileByName(AID_MEMSTAT));
            if (memResponse.getSW() == ISO7816.SW_NO_ERROR) {
                aid = AID_MEMSTAT;
                memoryOnly = true;
            }
        }

        mAID = aid;
        mMemoryOnly = memoryOnly;
        mDetectionDone = true;
    }

    public MemoryStatus memoryStatus() throws CardException {
        selectDebug();
        ResponseAPDU res = transactAndCheck(
                APDUUtil.buildCommand(
                        DebugProtocol.CLA_DEBUG,
                        DebugProtocol.INS_JC_MEM_STATUS));
        return MemoryStatus.fromBytes(res.getData());
    }

    public MemoryStatus memoryGC() throws CardException {
        selectDebug();
        ResponseAPDU res = transactAndCheck(
                APDUUtil.buildCommand(
                        DebugProtocol.CLA_DEBUG,
                        DebugProtocol.INS_JC_MEM_GC));
        return MemoryStatus.fromBytes(res.getData());
    }

    public LogStatus logStatus() throws CardException {
        selectDebug();
        ResponseAPDU response = transactAndCheck(
                APDUUtil.buildCommand(
                        DebugProtocol.CLA_DEBUG,
                        DebugProtocol.INS_LOG_STATUS
                ));
        LogStatus res = LogStatus.fromBytes(response.getData());
        return res;
    }

    public void logWrite(LogEntry entry) throws CardException {
        selectDebug();
        transactAndCheck(APDUUtil.buildCommand(
                DebugProtocol.CLA_DEBUG,
                DebugProtocol.INS_LOG_WRITE,
                entry.tag,
                entry.level,
                entry.data
                ));
    }

    public void logFlush() throws CardException {
        logReadAll(true);
    }

    public List<LogEntry> logReadAll(boolean take) throws CardException {
        selectDebug();
        ArrayList<LogEntry> res = new ArrayList<>();
        byte index = 0;
        LogEntry entry;
        do {
            if (take) {
                entry = logRead((byte)0, true);
            } else {
                entry = logRead(index++, false);
            }
            if(entry != null) {
                res.add(entry);
            }
        } while(entry != null);
        return res;
    }

    private LogEntry logRead(byte index, boolean take) throws CardException {
        byte flags = 0;
        if(take) {
            flags |= DebugProtocol.LOG_READ_P2_FLAG_TAKE;
        }
        ResponseAPDU response = transact(
                APDUUtil.buildCommand(
                        DebugProtocol.CLA_DEBUG,
                        DebugProtocol.INS_LOG_READ,
                        index,
                        flags
                ));
        int sw = response.getSW();
        LogEntry res = null;
        switch(sw) {
            case ISO7816.SW_NO_ERROR:
                res = LogEntry.fromBytes(response.getData());
                break;
            case ISO7816.SW_FILE_NOT_FOUND:
            case ISO7816.SW_RECORD_NOT_FOUND:
                break;
            default:
                throw new SWException("Error reading debug log", sw);
        }
        return res;
    }

    private void ensure() throws CardException {
        detect();
        if(mAID == null) {
            throw new CardException("Card has no debug applet");
        }
    }

    private void selectDebug() throws CardException {
        ensure();
        selectFileByName(mAID);
    }

    private void selectFileByName(AID name) throws CardException {
        transactAndCheck(buildSelectFileByName(name));
    }

    private CommandAPDU buildSelectFileByName(AID name) {
        return APDUUtil.buildCommand(
                CLA_ISO,
                INS_SELECT,
                SELECT_P1_BY_NAME,
                SELECT_P2_FIRST,
                name.getBytes()
        );
    }

    private ResponseAPDU transactAndCheck(CommandAPDU command) throws CardException {
        ResponseAPDU response = transmit(mChannel, command);
        checkResponse(response);
        return response;
    }

    private ResponseAPDU transact(CommandAPDU command) throws CardException {
        return transmit(mChannel, command);
    }

    private ResponseAPDU transmit(CardChannel channel, CommandAPDU command) throws CardException {
        LOG.trace("apdu > " + APDUUtil.toString(command));
        ResponseAPDU response = channel.transmit(command);
        LOG.trace("apdu < " + APDUUtil.toString(response));
        return response;
    }

    private void checkResponse(ResponseAPDU response) throws CardException {
        int sw = response.getSW();
        if (sw != ISO7816.SW_NO_ERROR) {
            throw new SWException("Error in transaction", sw);
        }
    }

}
