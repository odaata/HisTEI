package eu.emergingstandards.exceptions;

import eu.emergingstandards.utils.EMSTOxygenUtils;
import org.jetbrains.annotations.NotNull;
import ro.sync.ecss.extensions.api.AuthorAccess;

/**
 * Created by mike on 1/26/14.
 * <p/>
 * Generic exception thrown by an EMST object
 */
public class EMSTException extends Exception {

    /**
     * Whether the user has been notified of this error. Can be reset by
     * the caller with this public field
     *
     * @param userNotified whether the user has been notified.
     */
    public boolean userNotified;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public EMSTException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public EMSTException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this {@code Throwable} instance
     * (which may be {@code null}).
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @NotNull
    protected String generateMessage() {
        String message = getMessage();
        if (message == null || message.isEmpty()) {
            message = "An EMST error has occurred!";
        }

        Throwable cause = getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null && !causeMessage.isEmpty()) {
                message = message + "\nCause: " + cause.getMessage();
            }
        }
        return message;
    }

    public void notifyOxygenUser(AuthorAccess authorAccess) {
        if (!userNotified) {
            EMSTOxygenUtils.showErrorMessage(authorAccess, generateMessage());
            userNotified = true;
        }
    }
}
