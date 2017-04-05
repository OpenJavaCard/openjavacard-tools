package better.smartcard.tool;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Parameters(
        commandNames = "help",
        commandDescription = "Show help for available commands"
)
public class Help implements Runnable {

    @Parameter(
            description = "Command to show help for"
    )
    List<String> command = new ArrayList<>();

    JCommander mCommander;

    public Help(JCommander commander) {
        mCommander = commander;
    }

    @Override
    public void run() {
        if (command.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Commands:\n");
            Map<String, JCommander> commands = mCommander.getCommands();
            for (Map.Entry<String, JCommander> entry : commands.entrySet()) {
                String name = entry.getKey();
                JCommander command = entry.getValue();
                sb.append("    ");
                sb.append(name);
                sb.append("\t");
                sb.append(mCommander.getCommandDescription(name));
                sb.append("\n");
            }
            System.out.print(sb.toString());
        } else {
            mCommander.usage(command.get(0));
        }
    }

}
