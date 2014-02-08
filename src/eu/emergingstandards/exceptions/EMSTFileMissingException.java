package eu.emergingstandards.exceptions;

import org.apache.log4j.Logger;

import java.net.URL;

import static eu.emergingstandards.utils.EMSTUtils.castURLToFileString;

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
     * @param path    The path to the missing file.
     */
    public EMSTFileMissingException(String message, URL path) {
        super(message);
        this.path = castURLToFileString(path);
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
