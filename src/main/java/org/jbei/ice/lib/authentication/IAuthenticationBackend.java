package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.models.Account;

public interface IAuthenticationBackend {
    Account authenticate(String userId, String password) throws AuthenticationBackendException,
            InvalidCredentialsException;
}
