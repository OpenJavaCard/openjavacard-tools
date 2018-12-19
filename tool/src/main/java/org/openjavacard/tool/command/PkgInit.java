package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackageManager;

import javax.smartcardio.CardException;
import java.io.PrintStream;

@Parameters(
        commandNames = "pkg-init",
        commandDescription = "Packages: Initialize a card with a packaging system"
)
public class PkgInit extends PkgCommand {

    public PkgInit(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(OJCPackageManager manager) throws CardException {

    }

}
