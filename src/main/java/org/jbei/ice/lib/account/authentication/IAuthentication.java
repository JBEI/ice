package org.jbei.ice.lib.account.authentication;

/**
 * Interface for different authentication types.
 *
 * @author Hector Plahar
 */
public interface IAuthentication {

    /**
     * Authenticates a user's name and password against the system's stored credentials
     *
     * @param email    user email
     * @param password user password
     * @return true on successful authentication with the credentials provided, false otherwise
     * @throws AuthenticationException on exception authenticating
     */
    boolean authenticates(String email, String password) throws AuthenticationException;
}
