package org.jbei.ice.lib.authentication;

public class AuthenticationBackendException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuthenticationBackendException() {
    }

    public AuthenticationBackendException(String message) {
        super(message);
    }

    public AuthenticationBackendException(Throwable cause) {
        super(cause);
    }

    public AuthenticationBackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
