package better.smartcard.tool.command;

import better.smartcard.gp.GPContext;
import better.smartcard.tool.Main;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.*;
import java.util.List;

@Parameters(
        commandNames = "script"
)
public class Script implements Runnable {

    @Parameter(
            description = "Script to execute",
            required = true
    )
    List<File> scripts;

    GPContext mContext;

    public Script(GPContext context) {
        mContext = context;
    }

    @Override
    public void run() {
        PrintStream os = System.out;
        try {
            for(File script: scripts) {
                FileReader fr = new FileReader(script);
                BufferedReader br = new BufferedReader(fr);
                for (String line; (line = br.readLine()) != null; ) {
                    if(!line.startsWith("#")) {
                        String[] toks = line.split("\\s+");
                        if (toks.length > 0) {
                            JCommander jc = Main.makeCommander(mContext);
                            jc.parse(toks);
                            String scmd = jc.getParsedCommand();
                            if (scmd != null) {
                                JCommander jcmd = jc.getCommands().get(scmd);
                                for (Object o : jcmd.getObjects()) {
                                    if (o instanceof Runnable) {
                                        ((Runnable) o).run();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
