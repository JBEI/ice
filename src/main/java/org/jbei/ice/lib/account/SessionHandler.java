package org.jbei.ice.lib.account;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for user sessions.
 *
 * @author Hector Plahar
 */
public class SessionHandler {

    private final static ConcurrentHashMap<String, String> userSessionMap = new ConcurrentHashMap<>();

    public static String getUserIdBySession(String sessionId) {
        for (Map.Entry<String, String> entrySet : userSessionMap.entrySet()) {
            if (entrySet.getValue().equalsIgnoreCase(sessionId)) {
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
        userSessionMap.put(userId, newSession);
        return newSession;
    }

    public static boolean isValidSession(String sid) {
        return userSessionMap.containsValue(sid.trim());
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
