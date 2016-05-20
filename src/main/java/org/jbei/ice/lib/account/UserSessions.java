package org.jbei.ice.lib.account;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and maintains the web application sessions for users who have successfully authenticated
 *
 * @author Hector Plahar
 */
public class UserSessions {

    private final static ConcurrentHashMap<String, Set<String>> userSessionMap = new ConcurrentHashMap<>();

    public static String getUserIdBySession(String sessionId) {
        for (Map.Entry<String, Set<String>> entrySet : userSessionMap.entrySet()) {
            if (entrySet.getValue().contains(sessionId)) {
                return entrySet.getKey();
            }
        }
        return null;
    }

    /**
     * Creates a new session id for the specified user
     * stores and returns it
     *
     * @param userId unique user identifier
     * @return newly created session id
     */
    public static String createNewSessionForUser(String userId) {
        String newSession = UUID.randomUUID().toString();
        putSession(userId, newSession);
        return newSession;
    }

    protected static void putSession(String userId, String sessionId) {
        Set<String> sessionIds = userSessionMap.get(userId);
        if (sessionIds == null) {
            sessionIds = new HashSet<>();
            userSessionMap.put(userId, sessionIds);
        }
        sessionIds.add(sessionId);
    }

    /**
     * Uses the session id passed in the parameter and sets it as the session id for the
     * user as long as it meets a specified criteria (currently must be at least 5 xters long)
     *
     * @param userId    unique user identifier
     * @param sessionId optional session id to set for that user
     * @return session id set for specified user. It will be the same as that passed in the parameter if
     * it meets the criteria
     */
    public static String createSessionForUser(String userId, String sessionId) {
        if (StringUtils.isEmpty(sessionId) || sessionId.length() < 5)
            return createNewSessionForUser(userId);

        putSession(userId, sessionId);
        return sessionId;
    }

    /**
     * Invalidates the session id for the specified user
     * by removing the stored session id
     *
     * @param userId unique user id
     */
    public static void invalidateSession(String userId) {
        if (userId == null)
            return;
        userSessionMap.remove(userId);
    }

    /**
     * Retrieves the account object associated with the session identifier.
     * If the requesting user is not the same as the user associated with the session, then the requesting
     * user is required to have administrative privileges on their account
     *
     * @param requestingUser unique identifier for user making request
     * @param token          unique session identifier
     * @return account associated with the session token/identifier  or null if no account is located
     * @throws PermissionException if the user Id associated with the session token is not the same as the requesting
     *                             user but the requesting user's account does not have administrative privileges
     */
    public static AccountTransfer getUserAccount(String requestingUser, String token) {
        String userId = getUserIdBySession(token);
        if (StringUtils.isEmpty(userId))
            return null;

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account == null) {
            Logger.error("Account for userId returned by session (\"" + userId + "\") cannot be found");
            return null;
        }
        AccountController accountController = new AccountController();

        if (!requestingUser.equalsIgnoreCase(userId) && !accountController.isAdministrator(requestingUser))
            throw new PermissionException();

        AccountTransfer accountTransfer = account.toDataTransferObject();
        accountTransfer.setAdmin(accountController.isAdministrator(userId));
        return accountTransfer;
    }
}
