package org.jbei.ice.lib.search.blast;

public class BlastException extends Exception {

    public BlastException(Exception e) {
        super(e);
    }

    public BlastException(String errorString) {
        super(errorString);
    }

    private static final long serialVersionUID = 1L;

}
