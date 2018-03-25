package better.smartcard.commands;

import better.smartcard.debug.DebugCard;
import better.smartcard.debug.record.MemoryStatus;
import better.smartcard.gp.GPContext;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;
import java.io.PrintStream;

@Parameters(
        commandNames = "dbg-mem",
        commandDescription = "Manipulate card memory"
)
public class DbgMemory extends DbgCommand {

    @Parameter(
            names = "--gc",
            description = "Run the garbage collector"
    )
    boolean gc;

    @Parameter(
            names = "--balloon-persistent",
            description = "Change the balloon size for persistent memory"
    )
    int balloonPersistent = -1;

    @Parameter(
            names = "--balloon-reset",
            description = "Change the balloon size for transient-reset memory"
    )
    int balloonTransientReset = -1;

    @Parameter(
            names = "--balloon-deselect",
            description = "Change the balloon size for transient-deselect memory"
    )
    int balloonTransientDeselect = -1;

    public DbgMemory(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(DebugCard card) throws CardException {
        PrintStream os = System.out;
        if(gc) {
            MemoryStatus beforeGC = card.memoryGC();
            os.println("Before GC:\n  " + beforeGC);
        }
        MemoryStatus memstat = card.memoryStatus();
        os.println("Available memory:\n  " + memstat);
    }

}
