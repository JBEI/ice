package org.jbei.ice.lib.account.authentication;

import org.apache.commons.codec.binary.StringUtils;
import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;

/**
 * Backend for authentication using the database. This is the default backend.
 *
 * @author Hector Plahar
 */
public class LocalAuthentication implements IAuthentication {

    public LocalAuthentication() {
    }

    @Override
    public boolean authenticates(String userId, String password) throws AuthenticationException {
        if(userId == null || password == null)
            throw new AuthenticationException("Invalid username and password");

        Account account;
        AccountController controller = new AccountController();

        try {
            account = controller.getByEmail(userId);
            if ((account == null) || (!controller.isValidPassword(account, password))) {
                throw new AuthenticationException("Invalid Username or Password!");
            }
        } catch (ControllerException e) {
            throw new AuthenticationException("Exception validating credentials", e);
        }

        return true;
    }
}