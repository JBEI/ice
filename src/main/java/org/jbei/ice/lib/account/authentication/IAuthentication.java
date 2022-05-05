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
     * @param loginId  user login Identifier
     * @param password user password
     * @return the account identifier (usually email) on successful authentication with the credentials provided,
     *         null otherwise
     * @throws AuthenticationException on exception authenticating
     */
    String authenticates(String loginId, String password) throws AuthenticationException;
}
