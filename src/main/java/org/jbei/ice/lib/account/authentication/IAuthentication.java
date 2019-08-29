package org.jbei.ice.lib.account.authentication;

import org.jbei.ice.lib.account.AccountTransfer;

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
     * @param ip       user ip address
     * @return the account identifier (usually email) on successful authentication with the credentials provided,
     * null otherwise
     * @throws AuthenticationException on exception authenticating
     */
    AccountTransfer authenticates(String loginId, String password, String ip) throws AuthenticationException;
}
