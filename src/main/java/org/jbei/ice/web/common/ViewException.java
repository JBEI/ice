package org.jbei.ice.web.common;

/**
 * Exception class for web page views.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class ViewException extends RuntimeException {
    private static final long serialVersionUID = -528990844260541803L;

    public ViewException() {
    }

    public ViewException(String message) {
        super(message);
    }

    public ViewException(Throwable cause) {
        super(cause);
    }

    public ViewException(String message, Throwable cause) {
        super(message, cause);
    }
}
