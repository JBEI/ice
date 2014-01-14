package org.jbei.ice.lib.session;

import java.util.Calendar;
import java.util.HashMap;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.SessionDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.Utils;

/**
 * Cache {@link SessionData} information in a memory cache, as well as in the database. Hide this
 * fact from the outside.
 * <p/>
 * SessionData is kept in a cache in memory to prevent getting of multiple instances from hibernate,
 * creating a race condition. Instead of keeping a dynamically sized cache, consider using a fix
 * sized cache for performance, at the risk of using a too small of a cache which will result in
 * strange session data errors.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class PersistentSessionDataWrapper {

    private static HashMap<String, SessionData> sessionDataCache = new HashMap<String, SessionData>();
    private static HashMap<String, Long> timeStampCache = new HashMap<String, Long>();
    private static Long CACHE_TIMEOUT = 60000L; // 1 minute = 60000 ms

    private final SessionDAO dao;

    private PersistentSessionDataWrapper() {
        dao = new SessionDAO();
    }

    private static class SingletonHolder {
        private static final PersistentSessionDataWrapper INSTANCE = new PersistentSessionDataWrapper();
    }

    /**
     * Retrieve singleton instance.
     *
     * @return Singleton instance.
     */
    public static PersistentSessionDataWrapper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Get {@link SessionData}, either in memory or from the disk.
     * <p/>
     * Synchronized.
     *
     * @param sessionKey
     * @return SessionData object.
     * @throws DAOException
     */
    public synchronized SessionData getSessionData(String sessionKey) throws DAOException {
        SessionData sessionData = getCachedInstance(sessionKey);

        return sessionData;
    }

    /**
     * Create a new {@link SessionData}.
     * <p/>
     * Save into the database and put into the cache.
     *
     * @return Saved SessionData object.
     * @throws DAOException
     */
    public SessionData newSessionData() throws DAOException {
        SessionData sessionData = new SessionData(Utils.getConfigValue(ConfigurationKey.SITE_SECRET));
        persist(sessionData);
        getSessionDataCache().put(sessionData.getSessionKey(), sessionData);
        Long cacheExpirationTime = Calendar.getInstance().getTimeInMillis() + CACHE_TIMEOUT;
        getTimeStampCache().put(sessionData.getSessionKey(), cacheExpirationTime);

        return sessionData;
    }

    /**
     * Create a new {@link SessionData} with the given {@link Account}.
     *
     * @param account Account to associate with the new SessionData.
     * @return SessionData.
     * @throws DAOException
     */
    public SessionData newSessionData(Account account) throws DAOException {
        SessionData sessionData = newSessionData();
        sessionData.setAccount(account);
        persist(sessionData);

        return sessionData;
    }

    /**
     * Save the given {@link SessionData} into the database.
     * <p/>
     * This method is synchronized. Also performs cache cleaning.
     *
     * @param sessionData
     * @throws DAOException
     */
    public synchronized void persist(SessionData sessionData) throws DAOException {
        pruneCache();

        dao.create(sessionData);
    }

    /**
     * Delete the given session key from the cache and database.
     * <p/>
     * Synchronized.
     *
     * @param sessionKey Session key to delete.
     * @throws DAOException
     */
    public synchronized void delete(String sessionKey) throws ControllerException {
        SessionData sessionData = getSessionDataCache().get(sessionKey);
        if (sessionData != null) {
            getSessionDataCache().remove(sessionKey);
            getTimeStampCache().remove(sessionKey);
        } else {
            try {
                sessionData = dao.get(sessionKey);
            } catch (DAOException e) {
                // safe to pass
            }
        }
        if (sessionData != null) {
            try {
                dao.delete(sessionData);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        }
    }

    /**
     * Remove expired sessions from the cache.
     */
    private synchronized void pruneCache() {
        int before = getSessionDataCache().size();
        for (String sessionKey : getTimeStampCache().keySet()) {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now > getTimeStampCache().get(sessionKey)) {
                getSessionDataCache().remove(sessionKey);
            }
        }
        Logger.debug("SessionData cache went from " + before + " to " + getSessionDataCache().size() + " elements");
    }

    /**
     * Get the data cache.
     *
     * @return data cache.
     */
    private HashMap<String, SessionData> getSessionDataCache() {
        return sessionDataCache;
    }

    /**
     * Retrieve {@link SessionData}. Blocking.
     * <p/>
     * If the SessionData was in the cache, retrieve and increase the expiration time. If not in the
     * cache, retrieve from the database, and put into the cache.
     *
     * @param sessionKey
     * @return SessionData object.
     */
    private synchronized SessionData getCachedInstance(String sessionKey) {
        SessionData sessionData = getSessionDataCache().get(sessionKey);

        if (sessionData != null) {
            // In the cache. Just extend cache expire time.
            getTimeStampCache().put(sessionKey, getCacheExpirationTime());
        } else {
            // Not in cache, get from database, then put into cache
            try {
                sessionData = dao.get(sessionKey);
            } catch (DAOException e) {
                sessionData = null;
            }
            if (sessionData != null) {
                getSessionDataCache().put(sessionKey, sessionData);
                getTimeStampCache().put(sessionKey, getCacheExpirationTime());
            }
        }
        return sessionData;
    }

    /**
     * Calculate the expiration time. Current time + timeout.
     *
     * @return Expiration time.
     */
    private long getCacheExpirationTime() {
        return Calendar.getInstance().getTimeInMillis() + CACHE_TIMEOUT;
    }

    /**
     * Get the time stamp cache.
     *
     * @return time stamp cache.
     */
    public HashMap<String, Long> getTimeStampCache() {
        return timeStampCache;
    }
}