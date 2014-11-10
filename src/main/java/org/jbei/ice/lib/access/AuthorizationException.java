package org.jbei.ice.lib.access;

/**
 * Exception used in cases where authorization is required but lacking
 * for access (i.e. user is not logged in)
 *
 * @author Hector Plahar
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }
}
