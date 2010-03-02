package org.jbei.ice.lib.composers.formatters;

public class FormatterException extends Exception {
    private static final long serialVersionUID = 3317923989605978686L;

    public FormatterException() {
        super();
    }

    public FormatterException(String message) {
        super(message);
    }

    public FormatterException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatterException(Throwable cause) {
        super(cause);
    }
}
