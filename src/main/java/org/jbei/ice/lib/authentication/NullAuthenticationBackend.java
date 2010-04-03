package org.jbei.ice.lib.authentication;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;

public class NullAuthenticationBackend implements IAuthenticationBackend {
    public String getBackendName() {
        return "NullAuthenticationBackend";
    }

    @Override
    public Account authenticate(String userId, String password, String ip)
            throws AuthenticationBackendException, InvalidCredentialsException {
        if (userId == null || password == null) {
            throw new InvalidCredentialsException("Username and Password are mandatory!");
        }

        Account account = null;

        try {
            account = AccountController.getByEmail(userId);
        } catch (ControllerException e) {
            throw new AuthenticationBackendException(e);
        }

        if (account == null) {
            throw new InvalidCredentialsException("Invalid Username or Password!");
        }

        return account;
    }

    @Override
    public Account authenticate(String userId, String password)
            throws AuthenticationBackendException, InvalidCredentialsException {
        return authenticate(userId, password);
    }
}
