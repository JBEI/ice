package org.jbei.ice.lib.authentication;

/**
 * Exception class for the Authentication Backends
 *
 * @author Zinovii Dmytriv
 */
public class AuthenticationBackendException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AuthenticationBackendException() {
    }

    /**
     * Create a new AuthenticationBackendException with a message.
     *
     * @param message
     */
    public AuthenticationBackendException(String message) {
        super(message);
    }

    /**
     * Create a new AuthenticationBackendException with a Throwable.
     *
     * @param cause
     */
    public AuthenticationBackendException(Throwable cause) {
        super(cause);
    }

    /**
     * Create a new AuthenticationBackendException with a message and a Throwable.
     *
     * @param message
     * @param cause
     */
    public AuthenticationBackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
