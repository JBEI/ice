package org.jbei.ice.lib.utils;

/**
 * Exception class for Utilities.
 *
 * @author Timothy Ham
 */
public class UtilityException extends Exception {

    private static final long serialVersionUID = 1L;

    public UtilityException(Throwable e) {
        super(e);
    }

    public UtilityException(String string) {
        super(string);
    }

}
