package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.models.Account;

public interface IAuthenticationBackend {
    String getBackendName();

    Account authenticate(String userId, String password) throws AuthenticationBackendException,
            InvalidCredentialsException;

    Account authenticate(String userId, String password, String ip)
            throws AuthenticationBackendException, InvalidCredentialsException;
}
