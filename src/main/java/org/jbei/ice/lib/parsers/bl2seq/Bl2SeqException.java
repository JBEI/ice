package org.jbei.ice.lib.parsers.bl2seq;

/**
 * Exception class for Bl2SeqParser.
 *
 * @author Zinovii Dmytriv
 */
public class Bl2SeqException extends Exception {
    private static final long serialVersionUID = -2399411731925185230L;

    public Bl2SeqException() {
        super();
    }

    public Bl2SeqException(String message, Throwable cause) {
        super(message, cause);
    }

    public Bl2SeqException(String message) {
        super(message);
    }

    public Bl2SeqException(Throwable cause) {
        super(cause);
    }
}
