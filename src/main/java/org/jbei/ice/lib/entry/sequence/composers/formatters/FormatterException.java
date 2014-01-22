package org.jbei.ice.lib.entry.sequence.composers.formatters;

/**
 * Exception class for Formatters.
 *
 * @author Zinovii Dmytriv
 */
public class FormatterException extends Exception {
    private static final long serialVersionUID = 3317923989605978686L;

    /**
     * Constructor.
     */
    public FormatterException() {
        super();
    }

    /**
     * Create a new FormatterException using the message.
     *
     * @param message
     */
    public FormatterException(String message) {
        super(message);
    }

    /**
     * Create a new FormatterException using the message and a Throwable.
     *
     * @param message
     * @param cause
     */
    public FormatterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new FormatterException using a Throwable.
     *
     * @param cause
     */
    public FormatterException(Throwable cause) {
        super(cause);
    }
}
