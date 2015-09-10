package org.jbei.ice.lib.account;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for user sessions.
 *
 * @author Hector Plahar
 */
public class SessionHandler {

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
     *         it meets the criteria
     */
    public static String createSessionForUser(String userId, String sessionId) {
        if (StringUtils.isEmpty(sessionId) || sessionId.length() < 5)
            return createNewSessionForUser(userId);

        putSession(userId, sessionId);
        return sessionId;
    }

    public static boolean isValidSession(String sid) {
        if (sid == null)
            return false;

        return getUserIdBySession(sid) != null;
    }

    /**
     * Invalidates the session id for the specified user
     * by removing the stored session id
     *
     * @param userId unique user id
     */
    public static void invalidateSession(String userId) {
        userSessionMap.remove(userId);
    }
}
