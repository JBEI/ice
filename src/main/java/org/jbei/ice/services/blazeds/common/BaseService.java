package org.jbei.ice.services.blazeds.common;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;

public class BaseService {
    public static final String BASE_SERVICE_NAME = "BlazeDS";

    protected Account getAccountByToken(String authToken) {
        Account account = null;

        try {
            account = AccountController.getAccountBySessionKey(authToken);
        } catch (ControllerException e) {
            Logger.error("Failed to get account by token: " + authToken, e);
        }

        if (account == null) {
            Logger.warn(getServiceName() + "User by token doesn't exist: " + authToken);

            return null;
        }

        return account;
    }

    protected String getServiceName() {
        return BASE_SERVICE_NAME;
    }

    protected String getLoggerPrefix() {
        return getServiceName() + ": ";
    }
}
