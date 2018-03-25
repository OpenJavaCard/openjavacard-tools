package better.smartcard.tool;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

@Parameters(
        commandNames = "script",
        commandDescription = "Execute a script of tool commands"
)
public class Script implements Runnable {

    JCommander mCommander;

    public Script(JCommander commander) {
        mCommander = commander;
    }

    @Override
    public void run() {

    }

}
