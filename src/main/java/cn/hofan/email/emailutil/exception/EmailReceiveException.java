package cn.hofan.email.emailutil.exception;

/**
 * @author lizhao  2014/10/10.
 */

public class EmailReceiveException extends EmailException {
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8586664443821519121L;


    /**
     * Constructor with error message
     *
     * @param message Error message
     */
    public EmailReceiveException(final String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause
     *
     * @param message Error message
     * @param cause Cause
     */
    public EmailReceiveException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
