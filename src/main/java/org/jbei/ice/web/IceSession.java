package org.jbei.ice.web;

import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.jbei.ice.lib.authentication.AuthenticationBackend;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.utils.JbeirSettings;

public class IceSession extends WebSession {

	private static final long serialVersionUID = 1L;
	private Account account = null;
	private AuthenticationBackend authenticator = null;
	private SessionData sessionData = null;
	private AccountPreferences accountPreferences = null;
	private String COOKIE_NAME = JbeirSettings.getSetting("COOKIE_NAME");
	
	public IceSession(Request request, Response response, AuthenticationBackend authenticator2) {
		super(request);
		this.authenticator = authenticator2;
		
		SessionData sessionData = SessionData.getInstance(request, response);
		setSessionData(sessionData);
		HashMap<String, Object> data = sessionData.getData();
		if (data.containsKey("accountId")) {
			Integer accountId = (Integer) data.get("accountId"); 
			try {
				setAccount(AccountManager.get(accountId));
				setAccountPreferences(AccountManager.getAccountPreferences(account));
				
			} catch (ManagerException e) {
				e.printStackTrace();
				sessionData = null;
			}
		}
		
		setSessionData(sessionData);		
	}

	/**
	 * Save account id into SessionData, and save into db for persistent
	 * token based authentication.
	 */
	public void makeSessionPersistent(WebResponse response) {
		SessionData savedSession = getSessionData();
		HashMap<String, Object> data = savedSession.getData();
		if (data == null) {
			data = new HashMap<String, Object> () ;
		}
		data.put("accountId", (Integer) getAccount().getId());
		savedSession.setData(data);
		
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long expireDate = currentTime + 7776000000L; //90 days
		
		Cookie cookie = new Cookie(COOKIE_NAME, savedSession.getSessionKey());
		cookie.setPath("/");
		cookie.setMaxAge(7776000);
		response.addCookie(cookie);
		
		savedSession.setExpireDate(expireDate);
		try {
			savedSession.persist();
		} catch (ManagerException e) {
			e.printStackTrace();
		}

	}
	
	public boolean authenticateUser(String login, String password) {
		Account account = null;
		boolean result = false;
		try {
			account = authenticator.authenticate(login, password);
			
			if (account != null) {
				setAccount(account);
				setAccountPreferences(AccountManager.getAccountPreferences(account));
				SessionData sessionData = getSessionData();				
				sessionData.getData().put("accountId", account.getId());
				sessionData.persist();
				result = true;
			}
		} catch (Exception e) {
			Logger.warn("Could not authenticate user " + login + ": " + e.toString());
			e.printStackTrace();
		}
		return result; 
	}
	
	public void deAuthenticateUser() {
		clearSavedSession();
		account = null;
	}
	
	public boolean isAuthenticated() {
		return (account == null) ? false : true;
	}
	
	//getters and setters
	public static IceSession get() {
		return (IceSession) Session.get();
	}
	
	public void setSessionData(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	public SessionData getSessionData() {
		return sessionData;
	}

	private void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}
	
	public void setAccountPreferences(AccountPreferences accountPreferences) {
		this.accountPreferences = accountPreferences;
	}

	public AccountPreferences getAccountPreferences() {
		return accountPreferences;
	}

	//private methods
	private void clearSavedSession() {
		sessionData.delete();
	}
}
