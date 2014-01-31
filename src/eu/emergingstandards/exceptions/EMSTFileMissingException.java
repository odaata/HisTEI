package eu.emergingstandards.exceptions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by mike on 1/26/14.
 */
public class EMSTFileMissingException extends EMSTException {

    /**
     * Constructor.
     *
     * @param message The message.
     */
    public EMSTFileMissingException(String message, String path) {
        super(message);
        this.path = path;
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause   The cause of this exception.
     */
    public EMSTFileMissingException(String message, Throwable cause, String path) {
        super(message, cause);
        try {
            this.path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }
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

    private String path = "";

    public String getPath() {
        return path;
    }
}
