package org.jbei.ice.services.webservices;

/**
 * Exception class for RegistryAPI service.
 *
 * @author Zinovii Dmytriv
 */
public class ServiceException extends Exception {
    private static final long serialVersionUID = 6530223132366268034L;

    public ServiceException() {
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
