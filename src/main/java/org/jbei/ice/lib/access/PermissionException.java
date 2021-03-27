package org.jbei.ice.lib.access;

/**
 * Exception thrown when user is authorized to access the application but does not
 * have the required permission or role to access requested elements.
 * When this is thrown a <code>403 - Forbidden</code> is typically sent back as response
 * <br>
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

}
