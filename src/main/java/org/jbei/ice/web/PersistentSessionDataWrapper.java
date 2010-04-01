package org.jbei.ice.web;

import java.util.Calendar;
import java.util.HashMap;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SessionManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.JbeirSettings;

/**
 * SessionData is kept in a cache in memory to prevent getting of multiple instances
 * from
 * hibernate, creating a race condition. Instead of keeping a dynamically sized
 * cache, consider using a
 * fix sized cache for performance, at the risk of using a too small of a cache
 * which
 * will result in strange session data errors.
 * 
 * @author tham
 * 
 */
public class PersistentSessionDataWrapper {

    private static HashMap<String, SessionData> sessionDataCache = new HashMap<String, SessionData>();
    private static HashMap<String, Long> sessionDataCacheTimeStamp = new HashMap<String, Long>();
    private static Long CACHE_TIMEOUT = 60000L; // 1 minute = 60000 ms

    private static class SingletonHolder {
        private static final PersistentSessionDataWrapper INSTANCE = new PersistentSessionDataWrapper();
    }

    public static PersistentSessionDataWrapper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public synchronized SessionData getSessionData(String sessionKey) throws ManagerException {
        SessionData sessionData = null;
        sessionData = getCachedInstance(sessionKey);

        return sessionData;
    }

    public SessionData newSessionData() throws ManagerException {
        SessionData sessionData = new SessionData(JbeirSettings.getSetting("SITE_SECRET"));

        persist(sessionData);
        getSessionDataCache().put(sessionData.getSessionKey(), sessionData);
        Long cacheExpirationTime = Calendar.getInstance().getTimeInMillis() + CACHE_TIMEOUT;
        getSessionDataCacheTimeStamp().put(sessionData.getSessionKey(), cacheExpirationTime);

        return sessionData;
    }

    public SessionData newSessionData(Account account) throws ManagerException {
        SessionData sessionData = newSessionData();
        sessionData.setAccount(account);
        persist(sessionData);

        return sessionData;
    }

    public synchronized void persist(SessionData sessionData) throws ManagerException {
        pruneCache();

        SessionManager.save(sessionData);
    }

    public synchronized void delete(String sessionKey) throws ManagerException {
        SessionData sessionData = getSessionDataCache().get(sessionKey);
        if (sessionData != null) {
            getSessionDataCache().remove(sessionKey);
            getSessionDataCacheTimeStamp().remove(sessionKey);
        } else {
            try {
                sessionData = SessionManager.get(sessionKey);
            } catch (ManagerException e) {
                // safe to pass
            }
        }
        if (sessionData != null) {
            SessionManager.delete(sessionData);
        }
    }

    private synchronized void pruneCache() {
        int before = getSessionDataCache().size();
        for (String sessionKey : getSessionDataCacheTimeStamp().keySet()) {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now > getSessionDataCacheTimeStamp().get(sessionKey)) {
                getSessionDataCache().remove(sessionKey);
            }
        }
        Logger.info("SessionData cache went from " + before + " to " + getSessionDataCache().size()
                + " elements");
    }

    private HashMap<String, SessionData> getSessionDataCache() {
        return sessionDataCache;
    }

    private synchronized SessionData getCachedInstance(String sessionKey) {
        SessionData sessionData = getSessionDataCache().get(sessionKey);

        if (sessionData != null) {
            // In the cache. Just extend cache expire time.
            getSessionDataCacheTimeStamp().put(sessionKey, getCacheExpirationTime());
        } else {
            // Not in cache, get from database, then put into cache
            try {
                sessionData = SessionManager.get(sessionKey);
            } catch (ManagerException e) {
                sessionData = null;
            }
            if (sessionData != null) {
                getSessionDataCache().put(sessionKey, sessionData);
                getSessionDataCacheTimeStamp().put(sessionKey, getCacheExpirationTime());
            }
        }
        return sessionData;
    }

    private long getCacheExpirationTime() {
        return Calendar.getInstance().getTimeInMillis() + CACHE_TIMEOUT;
    }

    public void setSessionDataCacheTimeStamp(HashMap<String, Long> sessionDataCacheTimeStamp) {
        PersistentSessionDataWrapper.sessionDataCacheTimeStamp = sessionDataCacheTimeStamp;
    }

    public HashMap<String, Long> getSessionDataCacheTimeStamp() {
        return sessionDataCacheTimeStamp;
    }
}