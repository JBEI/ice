package org.jbei.ice.lib.dao;

public class DAOException extends Exception {
    private static final long serialVersionUID = 8785165201632857140L;

    public DAOException() {
        super();
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(String message) {
        super(message);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }
}
