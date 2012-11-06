package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.account.model.Account;

/**
 * Interface for authentication backends.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public interface IAuthenticationBackend {
    /**
     * Retrieve the name of the backend.
     *
     * @return backend name.
     */
    String getBackendName();

    /**
     * Interface method to authenticate user for all Authentication Backends.
     *
     * @param userId
     * @param password
     * @return {@link Account} of the user.
     * @throws AuthenticationBackendException
     * @throws InvalidCredentialsException
     */
    Account authenticate(String userId, String password) throws AuthenticationBackendException,
            InvalidCredentialsException;
}
