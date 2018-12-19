package org.openjavacard.tool.command;

import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackage;
import org.openjavacard.packaging.manager.OJCPackageManager;
import org.openjavacard.packaging.model.OJCPackageInfo;

import javax.smartcardio.CardException;
import java.util.List;

@Parameters(
        commandNames = "pkg-available",
        commandDescription = "Packages: List available packages"
)
public class PkgAvailable extends PkgCommand {

    public PkgAvailable(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(OJCPackageManager manager) throws CardException {
        List<OJCPackage> packages = manager.getAvailablePackages();
        for(OJCPackage pkg: packages) {

        }
    }

}
