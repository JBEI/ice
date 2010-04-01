package org.jbei.ice.services.webservices;

public class ServicePermissionException extends Exception {
    private static final long serialVersionUID = 5961860230111600691L;

    public ServicePermissionException() {
    }

    public ServicePermissionException(String message) {
        super(message);
    }

    public ServicePermissionException(Throwable cause) {
        super(cause);
    }

    public ServicePermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
