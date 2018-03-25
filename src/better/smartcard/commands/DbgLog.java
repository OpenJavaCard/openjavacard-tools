package better.smartcard.commands;

import better.smartcard.debug.DebugCard;
import better.smartcard.debug.record.LogEntry;
import better.smartcard.debug.record.LogStatus;
import better.smartcard.gp.GPContext;
import better.smartcard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "dbg-log",
        commandDescription = "Read or configure the debug log"
)
public class DbgLog extends DbgCommand {

    @Parameter(
            names = "--drop",
            description = "Drop log entries"
    )
    boolean drop;

    @Parameter(
            names = "--flush",
            description = "Flush all log entries"
    )
    boolean flush;

    @Parameter(
            names = "--write",
            description = "Write a log entry"
    )
    List<String> write;

    public DbgLog(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(DebugCard card) throws CardException {
        PrintStream os = System.out;

        if(flush) {
            card.logFlush();
        } else if(write != null) {
            for(String message: write) {
                byte[] entryBin = HexUtil.hexToBytes(message);
                LogEntry entry = new LogEntry((byte) 0, (byte) 0, entryBin);
                card.logWrite(entry);
            }
        } else {
            LogStatus ls = card.logStatus();
            os.println("Log status: " + ls);
            os.println();

            List<LogEntry> les = card.logReadAll(drop);
            if (les.isEmpty()) {
                os.println("Log is empty.");
            } else {
                for (LogEntry le : les) {
                    os.println(le.toString());
                }
            }
            os.println();
        }
    }
}
