package org.jbei.ice.lib.parsers;

public class InvalidFormatParserException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidFormatParserException(String arg0) {
        super(arg0);
    }

    public InvalidFormatParserException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
