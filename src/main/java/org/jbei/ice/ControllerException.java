package org.jbei.ice;

/**
 * Exception class for controllers.
 *
 * @author Zinovii Dmytriv
 */
public class ControllerException extends Exception {
    private static final long serialVersionUID = -4198825232452614310L;

    /**
     * Base constructor.
     */
    public ControllerException() {
    }

    /**
     * Create a new ControllerException with a message.
     *
     * @param message
     */
    public ControllerException(String message) {
        super(message);
    }

    /**
     * Create a new ControllerException with a {@link Throwable}.
     *
     * @param cause
     */
    public ControllerException(Throwable cause) {
        super(cause);
    }

    /**
     * Create a new ControllerException with a message and a {@link Throwable}.
     *
     * @param message
     * @param cause
     */
    public ControllerException(String message, Throwable cause) {
        super(message, cause);
    }
}
