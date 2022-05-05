package org.jbei.ice.lib.search.blast;

/**
 * Program Took Too Long exception
 *
 * @author Zinovii Dmytriv
 */
public class ProgramTookTooLongException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProgramTookTooLongException() {
        super();
    }

    public ProgramTookTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProgramTookTooLongException(String message) {
        super(message);
    }

    public ProgramTookTooLongException(Throwable cause) {
        super(cause);
    }
}
