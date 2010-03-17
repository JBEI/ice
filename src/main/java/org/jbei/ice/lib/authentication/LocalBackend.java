package org.jbei.ice.lib.authentication;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;

public class LocalBackend implements IAuthenticationBackend {
    public Account authenticate(String userId, String password) {
        Account account = null;

        try {
            account = AccountController.getByEmail(userId);

            if ((account == null) || (!AccountController.isValidPassword(account, password))) {
                return null;
            }
        } catch (ControllerException e) {
            // TODO: (Zinovii) Throw AuthenticationFailedException
            Logger.error("Authentication failed!", e);
        }

        return account;
    }
}
