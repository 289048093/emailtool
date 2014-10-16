package cn.hofan.email.emailutil.exception;

/**
 * @author lizhao  2014/10/10.
 */

public class EmailSendException extends EmailException {
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -7084778830413301933L;


    /**
     * Constructor with error message
     *
     * @param message Error message
     */
    public EmailSendException(final String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause
     *
     * @param message Error message
     * @param cause Cause
     */
    public EmailSendException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
