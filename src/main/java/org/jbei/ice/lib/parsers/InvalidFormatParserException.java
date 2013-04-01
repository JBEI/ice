package org.jbei.ice.lib.parsers;

/**
 * Exception class for parsers.
 *
 * @author Zinovii Dmytriv
 */
public class InvalidFormatParserException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidFormatParserException() {
        super();
    }

    public InvalidFormatParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFormatParserException(String message) {
        super(message);
    }

    public InvalidFormatParserException(Throwable cause) {
        super(cause);
    }
}
