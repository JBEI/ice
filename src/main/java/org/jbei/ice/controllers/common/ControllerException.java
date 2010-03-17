package org.jbei.ice.controllers.common;

public class ControllerException extends Exception {
    private static final long serialVersionUID = -4198825232452614310L;

    public ControllerException() {
    }

    public ControllerException(String message) {
        super(message);
    }

    public ControllerException(Throwable cause) {
        super(cause);
    }

    public ControllerException(String message, Throwable cause) {
        super(message, cause);
    }
}
