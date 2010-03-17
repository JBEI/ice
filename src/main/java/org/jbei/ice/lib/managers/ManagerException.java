package org.jbei.ice.lib.managers;

public class ManagerException extends Exception {
    private static final long serialVersionUID = -2173855549557216481L;

    public ManagerException() {
        super();
    }

    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerException(String message) {
        super(message);
    }

    public ManagerException(Throwable cause) {
        super(cause);
    }

}
