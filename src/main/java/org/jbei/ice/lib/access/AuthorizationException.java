package org.jbei.ice.lib.access;

/**
 * exception used in cases where authorization is required but lacking
 * for access
 *
 * @author Hector Plahar
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }
}
