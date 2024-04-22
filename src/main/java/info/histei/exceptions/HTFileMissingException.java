package info.histei.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

import static info.histei.utils.MainUtils.castURLToFileString;

/**
 * Created by mike on 1/26/14.
 */
public class HTFileMissingException extends HTException {

    private static final Logger logger = LogManager.getLogger(HTFileMissingException.class.getName());

    private String path = "";

    /**
     * Constructor.
     *
     * @param message The message.
     * @param path    The path to the missing file.
     */
    public HTFileMissingException(String message, URL path) {
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
