package org.jbei.ice.lib.models;

import java.util.Calendar;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
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
public class SessionData implements IModel {

    private static final long serialVersionUID = 1L;
    private static Long DEFAULT_EXPIRATION = 259200000L; // 3 days = 259200000 ms

    @Id
    @Column(name = "session_key", length = 40)
    private String sessionKey;

    @Column(name = "session_data")
    private HashMap<String, Object> data;

    @Column(name = "expire_date")
    private long expireDate;

    // needed for hibernate. use getInstance instead
    public SessionData() {
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public SessionData(String clientIp, String secret) {
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

}
