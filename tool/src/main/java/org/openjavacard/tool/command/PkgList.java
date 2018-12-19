package org.openjavacard.tool.command;

import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackage;
import org.openjavacard.packaging.manager.OJCPackageManager;
import org.openjavacard.packaging.model.OJCPackageInfo;

import javax.smartcardio.CardException;
import java.util.List;

@Parameters(
        commandNames = "pkg-list",
        commandDescription = "Packages: List packages installed on the card"
)
public class PkgList extends PkgCommand {

    public PkgList(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(OJCPackageManager manager) throws CardException {
        List<OJCPackage> packages = manager.getAvailablePackages();
        for(OJCPackage pkg: packages) {

        }
    }

}
