package org.jbei.ice.services.webservices;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;

public class JBEIService {
    protected Account validateAccount(String sessionId) throws SessionException {
        Account account = null;

        try {
            account = AccountController.getAccountByAuthToken(sessionId);
        } catch (ControllerException e) {
            throw new SessionException(e);
        }

        if (account == null) {
            throw new SessionException("Failed to lookup account!");
        }

        return account;
    }
}
