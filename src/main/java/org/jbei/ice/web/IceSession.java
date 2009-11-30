package org.jbei.ice.web;

import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.jbei.ice.lib.authentication.AuthenticationBackend;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SessionManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.JbeirSettings;

public class IceSession extends WebSession {

	private static final long serialVersionUID = 1L;
	private Account account = null;
	private AuthenticationBackend authenticator = null;
	private boolean authenticated = false;
	private SessionData sessionData = null;
	private String COOKIE_NAME = JbeirSettings.getSetting("COOKIE_NAME");
	
	public IceSession(Request request, Response response, AuthenticationBackend authenticator2) {
		super(request);
		this.authenticator = authenticator2;
		
		SessionData sessionData = getSavedSession(request);
		if (sessionData == null) {
			//
		} else if (sessionData.getData() == null) {
			sessionData.delete();
			sessionData = null;
		} else if (!sessionData.getData().containsKey("clientIp")) {
			sessionData.delete();
			sessionData = null;
		} else {
			HashMap<String, Object> data = sessionData.getData();
			String savedClientIp = (String) data.get("clientIp");
			String clientIp = ((WebRequest)request).getHttpServletRequest().getRemoteAddr();
			if (!clientIp.equals(savedClientIp)) {
				sessionData.delete();
				sessionData = null;
			} else if (data.containsKey("accountId")) {
				Integer accountId = (Integer) data.get("accountId"); 
				try {
					setAccount(AccountManager.get(accountId));
					authenticated = true;
					
				} catch (ManagerException e) {
					e.printStackTrace();
					sessionData = null;
				}
			}
			
		}
		
		if (sessionData == null) {
			sessionData = createNewSavedSession(request, response);
			Cookie cookie = new Cookie(COOKIE_NAME, sessionData.getSessionKey());
			cookie.setPath("/");
			cookie.setMaxAge(-1);
			
			((WebResponse)response).addCookie(cookie);
			
			try {
				sessionData.persist();
			} catch (ManagerException e) {
				e.printStackTrace();
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
		
		savedSession.setExpireDate(expireDate);
		
		Cookie cookie = new Cookie(COOKIE_NAME, savedSession.getSessionKey());
		cookie.setPath("/");
		cookie.setMaxAge(7776000);
		response.addCookie(cookie);
		
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
				result = true;
				setAccount(account);
				getSessionData().getData().put("accountId", account.getId());
				this.authenticated = true;
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
		authenticated = false;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
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
	
	//private methods
	private SessionData getSavedSession(Request request) {
		SessionData sessionData = null;
		
		Cookie userCookie = ((WebRequest) request).getCookie(COOKIE_NAME);
		
		if (userCookie != null) {
			try {
				String sessionKey = userCookie.getValue();
				sessionData = SessionManager.get(sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return sessionData;
	}
	
	private SessionData createNewSavedSession(Request request, Response response) {
		String clientIp = ((WebRequest)request).getHttpServletRequest().getRemoteAddr();
		SessionData sessionData = null;
		try {
			sessionData = new SessionData(clientIp,JbeirSettings.getSetting("SITE_SECRET"));
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("clientIp", clientIp);
			sessionData.setData(data);
			
			long currentTime = Calendar.getInstance().getTimeInMillis();
			long expireDate = currentTime + 259200000L; //3 days 
			sessionData.setExpireDate(expireDate);
		} catch (ManagerException e) {
						
						e.printStackTrace();
		}
		return sessionData;
	}
	

	private void clearSavedSession() {
		sessionData.delete();
	}
	
}
