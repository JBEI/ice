package org.jbei.ice.lib.account.authentication;

/**
 * Exception for Invalid Credentials.
 *
 * @author Zinovii Dmytriv
 */
public class InvalidCredentialsException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Create new InvalidCredentialsException with a message.
     *
     * @param message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

    /**
     * Create a new InvalidCredentialsException with a Throwable.
     *
     * @param e
     */
    public InvalidCredentialsException(Throwable e) {
        super(e);
    }

}
