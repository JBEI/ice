package org.jbei.ice.lib.authentication;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.models.Account;

/**
 * No password backend for testing. This backend accepts any password as long as the user exists in
 * the system.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public class NullAuthenticationBackend implements IAuthenticationBackend {
    @Override
    public String getBackendName() {
        return "NullAuthenticationBackend";
    }

    @Override
    public Account authenticate(String userId, String password)
            throws AuthenticationBackendException, InvalidCredentialsException {
        if (userId == null || password == null) {
            throw new InvalidCredentialsException("Username and Password are mandatory!");
        }

        Account account = null;
        AccountController controller = new AccountController();

        try {
            account = controller.getByEmail(userId);
        } catch (ControllerException e) {
            throw new AuthenticationBackendException(e);
        }

        if (account == null) {
            throw new InvalidCredentialsException("Invalid Username or Password!");
        }

        return account;
    }
}
