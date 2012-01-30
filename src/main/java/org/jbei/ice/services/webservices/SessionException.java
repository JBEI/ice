package org.jbei.ice.services.webservices;

/**
 * Exception for Session.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class SessionException extends Exception {
    private static final long serialVersionUID = -4988352757947157428L;

    public SessionException() {
    }

    public SessionException(String message) {
        super(message);
    }

    public SessionException(Throwable cause) {
        super(cause);
    }

    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
