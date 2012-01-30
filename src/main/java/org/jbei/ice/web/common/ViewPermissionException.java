package org.jbei.ice.web.common;

/**
 * Exception class for view permissions.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class ViewPermissionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ViewPermissionException() {
        super();
    }

    public ViewPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViewPermissionException(String message) {
        super(message);
    }

    public ViewPermissionException(Throwable cause) {
        super(cause);
    }
}
