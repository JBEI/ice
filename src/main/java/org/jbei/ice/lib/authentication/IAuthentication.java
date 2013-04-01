package org.jbei.ice.lib.authentication;

import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.account.model.Account;

/**
 * Interface for different authentication types.
 *
 * @author Hector Plahar
 */
public interface IAuthentication {

    /**
     * Determines if there is a valid account using the parameters passed
     *
     * @param userId   unique account identifier. typically user email address
     * @param password password for account
     * @return {@link Account} of the authenticated user
     * @throws AuthenticationException     in the event of any exception validating the credentials
     * @throws InvalidCredentialsException if parameters fails to authenticate
     */
    Account authenticate(String userId, String password) throws AuthenticationException, InvalidCredentialsException;
}
