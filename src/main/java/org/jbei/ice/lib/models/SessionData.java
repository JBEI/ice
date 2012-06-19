package org.jbei.ice.lib.models;

import java.util.Calendar;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.utils.Utils;

/**
 * Store session information for a logged in user.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accounts_id", nullable = true)
    private Account account;

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

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Generate a new session key, and set the expiration time.
     * 
     * @param secret
     *            salt used to generate the random sessionKey.
     */
    public SessionData(String secret) {
        String sha = generateSessionKey(secret);
        setSessionKey(sha);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireDate = currentTime + DEFAULT_EXPIRATION;
        setExpireDate(expireDate);
        setData(new HashMap<String, Object>());
    }

    private static String generateSessionKey(String secret) {
        String temp = java.util.UUID.randomUUID().toString() + secret + ";"
                + Calendar.getInstance().getTimeInMillis();

        return Utils.encryptSHA(temp);
    }

}
