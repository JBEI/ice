package org.jbei.ice.lib.composers;

/**
 * Exception for the {@link SequenceComposer}.
 *
 * @author Zinovii Dmytriv
 */
public class SequenceComposerException extends Exception {
    private static final long serialVersionUID = -5780712618108193606L;

    /**
     * Constructor.
     */
    public SequenceComposerException() {
        super();
    }

    /**
     * Create a new SequenceComposerException using the message.
     *
     * @param message
     */
    public SequenceComposerException(String message) {
        super(message);
    }

    /**
     * Create a new SequenceComposerException using the message, and a Throwable.
     *
     * @param message
     * @param cause
     */
    public SequenceComposerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new SequenceComposerException using a Thorwable.
     *
     * @param cause
     */
    public SequenceComposerException(Throwable cause) {
        super(cause);
    }
}
