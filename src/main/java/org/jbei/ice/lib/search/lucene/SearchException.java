package org.jbei.ice.lib.search.lucene;

@SuppressWarnings("serial")
public class SearchException extends Exception {
    public SearchException() {
    }

    public SearchException(Throwable throwable) {
        super(throwable);
    }

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
