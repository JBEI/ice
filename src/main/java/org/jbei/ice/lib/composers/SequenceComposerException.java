package org.jbei.ice.lib.composers;

public class SequenceComposerException extends Exception {
    private static final long serialVersionUID = -5780712618108193606L;

    public SequenceComposerException() {
        super();
    }

    public SequenceComposerException(String message) {
        super(message);
    }

    public SequenceComposerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceComposerException(Throwable cause) {
        super(cause);
    }
}
