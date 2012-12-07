package org.jbei.ice.lib.authentication;

import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;

/**
 * Authentication implementation that only validates the user id
 * with any non empty string as password
 *
 * @author Hector Plahar
 */
public class UserIdAuthentication implements IAuthentication {

    @Override
    public Account authenticate(String userId, String password)
            throws AuthenticationException, InvalidCredentialsException {
        if (userId == null || password == null) {
            throw new InvalidCredentialsException("Username and Password are mandatory!");
        }

        Account account = null;
        AccountController controller = ApplicationController.getAccountController();

        try {
            account = controller.getByEmail(userId);
        } catch (ControllerException e) {
            throw new AuthenticationException("Exception retrieving userId", e);
        }

        if (account == null) {
            throw new InvalidCredentialsException("Invalid Username or Password!");
        }

        return account;
    }
}
