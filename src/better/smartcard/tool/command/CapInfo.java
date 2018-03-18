package better.smartcard.tool.command;

import better.smartcard.cap.*;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

@Parameters(
        commandNames = "cap-info",
        commandDescription = "JavaCard CAP: Show information about a cap file"
)
public class CapInfo implements Runnable {

    @Parameter(
            description = "CAP files to show information about",
            required = true
    )
    List<File> capFiles;

    @Parameter(
            names = "--all",
            description = "Show all details"
    )
    boolean showAll = false;

    @Parameter(
            names = "--components",
            description = "Show details of all components"
    )
    boolean showComponents = false;

    @Override
    public void run() {
        PrintStream os = System.out;

        if(showAll) {
            showComponents = true;
        }

        for (File file : capFiles) {
            os.println();
            try {
                os.println("Reading CAP file " + file + "...");
                CapReader reader = new CapReader(file);
                CapFile capFile = reader.open();
                os.println();

                os.println("CAP file " + file.getName());
                os.println("  Manifest version: " + capFile.getManifestVersion());
                os.println("  Created by: " + capFile.getCreatedBy());
                os.println();

                for (CapPackage capPkg : capFile.getPackages()) {
                    os.println("CAP package " + capPkg.getPackageAID());
                    os.println("  Creation time: " + capPkg.getCapCreationTime());
                    os.println("  Converter provider: " + capPkg.getConverterProvider());
                    os.println("  Converter version: " + capPkg.getConverterVersion());
                    os.println("  Format version: " + capPkg.getCapFileVersion());
                    os.println("  Integer required: " + capPkg.isIntSupportRequired());
                    os.println("  Package name: " + capPkg.getPackageName());
                    os.println("  Package version: " + capPkg.getPackageVersion());
                    os.println();

                    for (CapImport capImp : capPkg.getImports()) {
                        os.println("  Import " + capImp.getAID());
                        os.println("    Version: " + capImp.getVersion());
                    }
                    os.println();

                    for (CapApplet capApp : capPkg.getApplets()) {
                        String appName = capApp.getName();
                        String appVersion = capApp.getVersion();
                        os.println("  Applet " + capApp.getAID());
                        os.println("    Name: " + appName);
                        if(appVersion != null) {
                            os.println("    Version: " + appVersion);
                        }
                    }
                    os.println();

                    if(showComponents) {
                        for(CapComponentType type : CapComponentType.values()) {
                            CapComponent capCom = capPkg.getComponentByType(type);
                            if(capCom != null) {
                                os.println("  Component " + capCom.getFilename());
                                os.println("    Type: " + capCom.getName());
                                os.println("    Size: " + capCom.getSize());
                            }
                        }
                        os.println();
                    }
                }
            } catch (Exception ex) {
                throw new Error("Exception reading CAP file", ex);
            }
        }
    }

}
