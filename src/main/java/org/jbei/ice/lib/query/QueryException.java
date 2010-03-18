package org.jbei.ice.lib.query;

public class QueryException extends Exception {
    private static final long serialVersionUID = 6932434513879373344L;

    public QueryException() {
    }

    public QueryException(String message) {
        super(message);
    }

    public QueryException(Throwable cause) {
        super(cause);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
