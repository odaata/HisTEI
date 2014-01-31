package eu.emergingstandards.exceptions;

import eu.emergingstandards.utils.EMSTUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Created by mike on 1/26/14.
 */
public class EMSTFileMissingException extends EMSTException {

    private static final Logger logger = Logger.getLogger(EMSTFileMissingException.class.getName());

    private String path = "";

    /**
     * Constructor.
     *
     * @param message The message.
     * @param path The path to the missing file.
     */
    public EMSTFileMissingException(String message, String path) {
        super(message);
        initPath(path);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param path The path to the missing file.
     */
    public EMSTFileMissingException(String message, Path path) {
        super(message);
        initPath(path);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param path    The path to the missing file.
     */
    public EMSTFileMissingException(String message, URL path) {
        super(message);
        initPath(path);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause   The cause of this exception.
     * @param path The path to the missing file.
     */
    public EMSTFileMissingException(String message, Throwable cause, String path) {
        super(message, cause);
        initPath(path);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause   The cause of this exception.
     * @param path    The path to the missing file.
     */
    public EMSTFileMissingException(String message, Throwable cause, Path path) {
        super(message, cause);
        initPath(path);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause   The cause of this exception.
     * @param path    The path to the missing file.
     */
    public EMSTFileMissingException(String message, Throwable cause, URL path) {
        super(message, cause);
        initPath(path);
    }

    private void initPath(String path) {
        this.path = EMSTUtils.decodeURL(path);
    }

    private void initPath(Path path) {
        try {
            this.path = path.toRealPath().toString();
        } catch (IOException e) {
            logger.error(e, e);
        }
    }

    private void initPath(URL path) {
        this.path = EMSTUtils.decodeURL(path);
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this {@code Throwable} instance
     * (which may be {@code null}).
     */
    @Override
    public String getMessage() {
        String message = super.getMessage();

        if (message != null && path != null) {
            return message + "\nPath: " + path;
        } else {
            return message;
        }
    }

    public String getPath() {
        return path;
    }
}
