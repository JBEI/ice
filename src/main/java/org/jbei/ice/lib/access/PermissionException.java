package org.jbei.ice.lib.access;

/**
 * Exception thrown when user is authorized to access the application but does not
 * have the required permission or role to access requested elements
 *
 * @author Hector Plahar
 */
public class PermissionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException() {
        super("Administrative privileges required");
    }

    public PermissionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
