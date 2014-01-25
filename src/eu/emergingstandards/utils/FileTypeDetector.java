package eu.emergingstandards.utils;

import org.apache.tika.Tika;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by mike on 1/25/14.
 */
public class FileTypeDetector extends java.nio.file.spi.FileTypeDetector {

    private final Tika tika = new Tika();

    /**
     * Probes the given file to guess its content type.
     * <p/>
     * <p> The means by which this method determines the file type is highly
     * implementation specific. It may simply examine the file name, it may use
     * a file <a href="../attribute/package-summary.html">attribute</a>,
     * or it may examines bytes in the file.
     * <p/>
     * <p> The probe result is the string form of the value of a
     * Multipurpose Internet Mail Extension (MIME) content type as
     * defined by <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045:
     * Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet
     * Message Bodies</i></a>. The string must be parsable according to the
     * grammar in the RFC 2045.
     *
     * @param path the path to the file to probe
     * @return The content type or {@code null} if the file type is not
     * recognized
     * @throws java.io.IOException An I/O error occurs
     * @throws SecurityException   If the implementation requires to access the file, and a
     *                             security manager is installed, and it denies an unspecified
     *                             permission required by a file system provider implementation.
     *                             If the file reference is associated with the default file system
     *                             provider then the {@link SecurityManager#checkRead(String)} method
     *                             is invoked to check read access to the file.
     * @see java.nio.file.Files#probeContentType
     */
    @Override
    public String probeContentType(Path path) throws IOException {
        return tika.detect(path.toFile());
    }
}
