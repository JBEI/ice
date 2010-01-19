package org.jbei.ice.lib.parsers;

public class ParserException extends Exception {
    private static final long serialVersionUID = 1L;

    public ParserException() {
    }

    public ParserException(String arg0) {
        super(arg0);
    }

    public ParserException(Throwable arg0) {
        super(arg0);
    }

    public ParserException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
