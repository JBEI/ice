package org.jbei.ice.services.blazeds;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;

/**
 * Base service for BlazeDS.
 *
 * @author Zinovii Dmytriv
 */
public class BaseService {
    public static final String BASE_SERVICE_NAME = "BlazeDS";

    public BaseService() {
    }

    protected Account getAccountBySessionId(String sessionId) {
        String userId = SessionHandler.getUserIdBySession(sessionId);
        Account account = new AccountController().getByEmail(userId);

        if (account == null) {
            Logger.warn(getServiceName() + "User by sessionId doesn't exist: " + sessionId);
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

    protected void logInfo(String message) {
        Logger.info(getLoggerPrefix() + message);
    }
}
