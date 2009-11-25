package org.jbei.ice.web;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SessionManager;

@Entity
@Table(name = "session_data")
public class SessionData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "session_key", length = 40) 
	private String sessionKey;
	
	@Column(name = "session_data")
	private HashMap<String, Object> data;
	
	@Column(name = "expire_date")
	private long expireDate;
	
	public SessionData() {
		
	}
	
	/**
	 * @param host
	 * @param secret
	 * @param keepSignedIn
	 * @throws ManagerException
	 */
	public SessionData(String host, String secret) throws ManagerException {
		String sha = generateSessionKey(host, secret);
		setSessionKey(sha);
		setExpireDate(1L);
		
	}
	
	private static String generateSessionKey(String host, String secret) {
		String result = null;
		String temp = java.util.UUID.randomUUID().toString() + host + secret + "" + Calendar.getInstance().getTimeInMillis();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			
			result = org.jbei.ice.lib.utils.Utils.getHexString(digest.digest(temp.getBytes("UTF-8")));
			
		} catch (NoSuchAlgorithmException e) {
			String msg = "Could not generate Session Key";
			Logger.error(msg);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			String msg = "Could not generate Session Key";
			Logger.error(msg);
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void delete() {
		try {
			SessionManager.delete(this);
		} catch (ManagerException e) {
			
			e.printStackTrace();
		}
	}
	
	//getters and setters
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

}
