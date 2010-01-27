package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Utils;

public class NullAuthenticationBackend implements IAuthenticationBackend {
    public Account authenticate(String userId, String password) {
        Account account = null;

        try {
            account = AccountManager.getByEmail(userId);
        } catch (Exception e) {
            e.printStackTrace();

            Logger.warn("null authentication failed with " + Utils.stackTraceToString(e));
        }

        return account;
    }
}
