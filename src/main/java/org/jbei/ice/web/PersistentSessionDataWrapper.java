package org.jbei.ice.web;

import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SessionManager;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.JbeirSettings;

public class PersistentSessionDataWrapper {

    private static HashMap<String, SessionData> sessionDataCache = new HashMap<String, SessionData>();
    private static HashMap<String, Long> sessionDataCacheTimeStamp = new HashMap<String, Long>();
    private static String COOKIE_NAME = JbeirSettings.getSetting("COOKIE_NAME");
    private static Long CACHE_TIMEOUT = 60000L; // 1 minute = 60000 ms

    private static class SingletonHolder {
        private static final PersistentSessionDataWrapper INSTANCE = new PersistentSessionDataWrapper();
    }

    public static PersistentSessionDataWrapper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public synchronized SessionData getSessionData(WebRequest request) {
        SessionData sessionData = null;

        Cookie userCookie = (request).getCookie(COOKIE_NAME);

        if (userCookie != null) {
            String sessionKey = userCookie.getValue();
            sessionData = getCachedInstance(sessionKey);
            if (sessionData != null) {
                String savedClientIp = (String) sessionData.getData().get("clientIp");
                String clientIp = (request).getHttpServletRequest().getRemoteAddr();
                if (!clientIp.equals(savedClientIp)) {
                    this.delete(sessionKey);
                    sessionData = null;
                }
            }
        }
        if (sessionData == null) {
            sessionData = newSessionData(request);
        }

        return sessionData;
    }

    private SessionData newSessionData(Request request) {
        pruneCache();

        String clientIp = ((WebRequest) request).getHttpServletRequest().getRemoteAddr();
        SessionData sessionData = new SessionData(clientIp, JbeirSettings.getSetting("SITE_SECRET"));
        sessionData.getData().put("clientIp", clientIp);
        try {
            SessionManager.save(sessionData);
            getSessionDataCache().put(sessionData.getSessionKey(), sessionData);
            Long cacheExpirationTime = Calendar.getInstance().getTimeInMillis() + CACHE_TIMEOUT;
            getSessionDataCacheTimeStamp().put(sessionData.getSessionKey(), cacheExpirationTime);
            Cookie cookie = new Cookie(COOKIE_NAME, sessionData.getSessionKey());
            cookie.setPath("/");
            cookie.setMaxAge(-1);
            try {
                WebResponse response = (WebResponse) RequestCycle.get().getResponse();
                (response).addCookie(cookie);
            } catch (ClassCastException e) {
                /* This is because of a seeming bug in Wicket.  getResponse() sometimes
                    returns a StringResponse instead of WebResponse. Nothing has to be done
                    here anyway.
                 */
                Logger.debug("Could not cast StringResponse to WebResponse");
            }

        } catch (ManagerException e) {
            // SessionData could not be persisted. return null
            String msg = "newSessionData failed: " + e.toString();
            Logger.debug(msg);
            sessionData = null;
        }

        return sessionData;

    }

    public synchronized void persist(SessionData sessionData) throws ManagerException {
        try {
            SessionManager.save(sessionData);
        } catch (ManagerException e) {
            String msg = "Could not persist session data" + e.toString();
            Logger.error(msg);
            throw e;
        }
    }

    public synchronized void delete(String sessionKey) {
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
            try {
                SessionManager.delete(sessionData);
            } catch (ManagerException e) {
                String msg = "Could not delete session Data: " + e.toString();
                Logger.error(msg);
            }
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
                String msg = "Could getCachedInstance: " + e.toString();
                Logger.error(msg);
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