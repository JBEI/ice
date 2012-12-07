package org.jbei.ice.lib.authentication;

import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;

/**
 * Backend for authentication using the database. This is the default backend.
 *
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 */
public class LocalBackend implements IAuthentication {

    public LocalBackend() {}

    @Override
    public Account authenticate(String userId, String password)
            throws AuthenticationException, InvalidCredentialsException {
        if (userId == null || password == null) {
            throw new InvalidCredentialsException("Username and Password are mandatory!");
        }

        Account account;
        AccountController controller = ApplicationController.getAccountController();

        try {
            account = controller.getByEmail(userId);
            if ((account == null) || (!controller.isValidPassword(account, password))) {
                throw new InvalidCredentialsException("Invalid Username or Password!");
            }
        } catch (ControllerException e) {
            throw new AuthenticationException("Exception validating credentials", e);
        }

        return account;
    }
}
