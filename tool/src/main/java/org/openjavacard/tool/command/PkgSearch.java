package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackageManager;

import javax.smartcardio.CardException;

@Parameters(
        commandNames = "pkg-search",
        commandDescription = "Packages: Search for packages"
)
public class PkgSearch extends PkgCommand {

    @Parameter(
            required = true,
            description = "Query"
    )
    String query;

    public PkgSearch(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(OJCPackageManager manager) throws CardException {
    }

}
