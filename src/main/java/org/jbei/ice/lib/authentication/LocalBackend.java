package org.jbei.ice.lib.authentication;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;

public class LocalBackend implements IAuthenticationBackend {
    public String getBackendName() {
        return "LocalBackend";
    }

    @Override
    public Account authenticate(String userId, String password)
            throws AuthenticationBackendException, InvalidCredentialsException {
        if (userId == null || password == null) {
            throw new InvalidCredentialsException("Username and Password are mandatory!");
        }

        Account account = null;

        try {
            account = AccountController.getByEmail(userId);

            if ((account == null) || (!AccountController.isValidPassword(account, password))) {
                throw new InvalidCredentialsException("Invalid Username or Password!");
            }
        } catch (ControllerException e) {
            throw new AuthenticationBackendException(e);
        }

        return account;
    }
}
