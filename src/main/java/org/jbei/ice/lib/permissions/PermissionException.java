package org.jbei.ice.lib.permissions;

public class PermissionException extends Exception {
    private static final long serialVersionUID = 1L;

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
