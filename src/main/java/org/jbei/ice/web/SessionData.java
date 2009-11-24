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

@Entity
@Table(name = "session_data")
public class SessionData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "session_key", length = 40) 
	private String sessionKey;
	
	@Column(name = "session_data")
	private HashMap<String, Object> sessionData;
	
	//getters and setters
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public HashMap<String, Object> getSessionData() {
		return sessionData;
	}
	public void setSessionData(HashMap<String, Object> sessionData) {
		this.sessionData = sessionData;
	}
	
	public static String generateSessionKey(String host, String secret) {
		String result = null;
		String temp = java.util.UUID.randomUUID().toString() + host + secret + Calendar.getInstance().getTimeInMillis();
		try {
			result = MessageDigest.getInstance("SHA-1").digest(temp.getBytes("UTF-8")).toString();
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
	
}
