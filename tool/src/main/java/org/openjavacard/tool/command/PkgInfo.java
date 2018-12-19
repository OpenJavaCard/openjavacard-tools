package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackage;
import org.openjavacard.packaging.manager.OJCPackageManager;
import org.openjavacard.packaging.model.OJCPackageInfo;

import javax.smartcardio.CardException;

@Parameters(
        commandNames = "pkg-available",
        commandDescription = "Packages: Show information about a package"
)
public class PkgInfo extends PkgCommand {

    @Parameter(
            required = true,
            description = "Package"
    )
    String packageName;

    public PkgInfo(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(OJCPackageManager manager) throws CardException {
        OJCPackage info = manager.findPackageByName(packageName);
    }

}
