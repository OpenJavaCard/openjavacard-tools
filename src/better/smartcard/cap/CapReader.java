package better.smartcard.cap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Hashtable;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CapReader {

    private static final Logger LOG = LoggerFactory.getLogger(CapReader.class);

    private static final String FILE_MANIFEST = "META-INF/MANIFEST.MF";

    File mFile;

    Manifest mManifest;

    Hashtable<String, byte[]> mFiles;

    public CapReader(File file) {
        mFile = file;
    }

    public Manifest getManifest() {
        return mManifest;
    }

    public CapFile open() throws IOException {
        LOG.debug("opening CAP " + mFile);

        // create an input stream for the zip file
        ZipInputStream zis = new ZipInputStream(new FileInputStream(mFile));

        // zip entry during iteration
        ZipEntry ze;
        // manifest once read
        Manifest manifest = null;
        // collected files
        Hashtable<String, byte[]> files = new Hashtable<>();

        // read all files in the zip, parsing the manifest
        while ((ze = zis.getNextEntry()) != null) {
            String name = ze.getName();
            byte[] bytes = readZipEntry(zis, ze);
            LOG.debug("file " + name + " (" + bytes.length + " bytes)");
            files.put(name, bytes);
            if (name.equals(FILE_MANIFEST)) {
                manifest = new Manifest(new ByteArrayInputStream(bytes));
            }
        }

        // we should have found a manifest
        if (manifest == null) {
            throw new IOException("CAP file does not contain a manifest");
        }

        // apply to members
        mManifest = manifest;
        mFiles = files;

        // parse the CAP contents
        CapFile file = new CapFile();
        file.read(manifest, files);

        return file;
    }

    private byte[] readZipEntry(ZipInputStream zis, ZipEntry entry) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int c;
        while ((c = zis.read(buf)) > 0) {
            bos.write(buf, 0, c);
        }
        return bos.toByteArray();
    }

}
