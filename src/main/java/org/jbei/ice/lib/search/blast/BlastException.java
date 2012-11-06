package org.jbei.ice.lib.search.blast;

/**
 * Exception class to throw for Blast.
 *
 * @author Zinovii Dmytriv
 */
public class BlastException extends Exception {
    private static final long serialVersionUID = 1L;

    public BlastException() {
        super();
    }

    public BlastException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlastException(String message) {
        super(message);
    }

    public BlastException(Throwable cause) {
        super(cause);
    }
}
