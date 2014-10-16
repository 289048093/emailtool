package cn.hofan.email.emailutil.exception;

public class EmailException extends Exception {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -2919776925807264765L;

    /**
     * Constructor with error message
     *
     * @param message Error message
     */
    public EmailException(final String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause
     *
     * @param message Error message
     * @param cause   Cause
     */
    public EmailException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
