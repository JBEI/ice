package org.jbei.ice.lib.managers;

/**
 * Exception class for Managers.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class ManagerException extends Exception {
    private static final long serialVersionUID = -2173855549557216481L;

    /**
     * Constructor.
     */
    public ManagerException() {
        super();
    }

    /**
     * Create a new ManagerException using the message and Throwable.
     * 
     * @param message
     * @param cause
     */
    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new ManagerException using a message.
     * 
     * @param message
     */
    public ManagerException(String message) {
        super(message);
    }

    /**
     * Create a new ManagerException using a Throwable.
     * 
     * @param cause
     */
    public ManagerException(Throwable cause) {
        super(cause);
    }

}
