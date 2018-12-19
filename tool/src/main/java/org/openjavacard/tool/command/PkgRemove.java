package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackage;
import org.openjavacard.packaging.manager.OJCPackageManager;
import org.openjavacard.packaging.model.OJCPackageInfo;

import javax.smartcardio.CardException;

@Parameters(
        commandNames = "pkg-remove",
        commandDescription = "Packages: Remove a package from the card"
)
public class PkgRemove extends PkgCommand {

    @Parameter(
            required = true
    )
    String packageName;

    public PkgRemove(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(OJCPackageManager manager) throws CardException {
        OJCPackage info = manager.findPackageByName(packageName);
    }

}
