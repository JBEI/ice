package org.jbei.ice.lib.account.authentication;

import org.jbei.ice.lib.account.AccountController;

/**
 * Authentication implementation that only validates the user id
 * with any non empty string as password
 *
 * @author Hector Plahar
 */
public class UserIdAuthentication implements IAuthentication {

    @Override
    public boolean authenticates(String userId, String password) throws AuthenticationException {
        AccountController retriever = new AccountController();
        return retriever.getByEmail(userId) != null;
    }
}
