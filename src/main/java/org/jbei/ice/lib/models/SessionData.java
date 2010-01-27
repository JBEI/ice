package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.servlet.http.Cookie;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SessionManager;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

/**
 * SessionData keeps a cache in memory to prevent getting of multiple instances
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
@Entity
@Table(name = "session_data")
public class SessionData implements Serializable {

    private static final long serialVersionUID = 1L;
    private static String COOKIE_NAME = JbeirSettings.getSetting("COOKIE_NAME");
    private static Long DEFAULT_EXPIRATION = 259200000L; // 3 days = 259200000
    // ms
    private static Long CACHE_TIMEOUT = 60000L; // 1 minute = 60000 ms

    @Id
    @Column(name = "session_key", length = 40)
    private String sessionKey;

    @Column(name = "session_data")
    private HashMap<String, Object> data;

    @Column(name = "expire_date")
    private long expireDate;

    @Transient
    private static HashMap<String, SessionData> sessionDataCache = new HashMap<String, SessionData>();
    private static HashMap<String, Long> sessionDataCacheTimeStamp = new HashMap<String, Long>();

    // needed for hibernate. use getInstance instead
    public SessionData() {

    }

    public static SessionData getInstance(Request request, Response response) {
        SessionData sessionData = null;

        Cookie userCookie = ((WebRequest) request).getCookie(COOKIE_NAME);

        if (userCookie != null) {
            String sessionKey = userCookie.getValue();
            sessionData = getCachedInstance(sessionKey);
            if (sessionData != null) {
                String savedClientIp = (String) sessionData.getData().get("clientIp");
                String clientIp = ((WebRequest) request).getHttpServletRequest().getRemoteAddr();
                if (!clientIp.equals(savedClientIp)) {
                    sessionData.delete();
                    sessionData = null;
                }
            }
        }

        if (sessionData == null) {
            sessionData = getNewInstance(request, response);
        }

        return sessionData;
    }

    public void delete() {
        getSessionDataCache().remove(this.getSessionKey());

        try {
            SessionManager.delete(this);
        } catch (ManagerException e) {

            e.printStackTrace();
        }
    }

    // getters and setters
    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public SessionData persist() throws ManagerException {
        return SessionManager.save(this);
    }

    private static HashMap<String, SessionData> getSessionDataCache() {
        return sessionDataCache;
    }

    public static HashMap<String, Long> getSessionDataCacheTimeStamp() {
        return sessionDataCacheTimeStamp;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // private methods

    private static synchronized SessionData getCachedInstance(String sessionKey) {
        SessionData sessionData = getSessionDataCache().get(sessionKey);

        if (sessionData == null) {
            try {
                sessionData = SessionManager.get(sessionKey);
            } catch (ManagerException e) {
                e.printStackTrace();
                sessionData = null;
            }
        } else {
            Long time = getSessionDataCacheTimeStamp().get(sessionKey) + CACHE_TIMEOUT;
            getSessionDataCacheTimeStamp().put(sessionKey, time);

        }
        return sessionData;
    }

    private static SessionData getNewInstance(Request request, Response response) {
        pruneCache();
        String clientIp = ((WebRequest) request).getHttpServletRequest().getRemoteAddr();

        SessionData sessionData = new SessionData(clientIp, JbeirSettings.getSetting("SITE_SECRET"));
        sessionData.getData().put("clientIp", clientIp);

        getSessionDataCache().put(sessionData.getSessionKey(), sessionData);
        Long expirationTime = Calendar.getInstance().getTimeInMillis() + CACHE_TIMEOUT;
        getSessionDataCacheTimeStamp().put(sessionData.getSessionKey(), expirationTime);
        Cookie cookie = new Cookie(COOKIE_NAME, sessionData.getSessionKey());
        cookie.setPath("/");
        cookie.setMaxAge(-1);

        ((WebResponse) response).addCookie(cookie);

        try {
            sessionData.persist();
        } catch (ManagerException e) {
            e.printStackTrace();
        }
        return sessionData;
    }

    /**
     * @param clientIp
     * @param secret
     * @param keepSignedIn
     * @throws ManagerException
     */
    private SessionData(String clientIp, String secret) {
        String sha = generateSessionKey(clientIp, secret);
        setSessionKey(sha);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireDate = currentTime + DEFAULT_EXPIRATION;
        setExpireDate(expireDate);
        setData(new HashMap<String, Object>());
    }

    private static String generateSessionKey(String clientIp, String secret) {
        String temp = java.util.UUID.randomUUID().toString() + clientIp + secret + ""
                + Calendar.getInstance().getTimeInMillis();

        return Utils.encryptSHA(temp);
    }

    private static void pruneCache() {
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
}
