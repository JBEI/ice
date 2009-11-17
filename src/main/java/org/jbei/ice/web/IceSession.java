package org.jbei.ice.web;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;

public class IceSession extends WebSession {

	private static final long serialVersionUID = 1L;
	private Account account = null;
	private Authenticator authenticator = null;
	private boolean authenticated = false;
	
	public IceSession(Request request, Authenticator authenticator) {
		super(request);
		this.authenticator = authenticator;
	}

	public static IceSession get() {
		return (IceSession) Session.get();
	}

	private void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}
	
	public boolean authenticateUser(String login, String password) {
		Account account = null;
		boolean result = false;
		try {
			account = this.authenticator.authenticate(login, password);
			if (account != null) {
				result = true;
				setAccount(account);
				this.authenticated = true;
			}
		} catch (Exception e) {
			Logger.warn("Could not authenticate user " + login + ": " + e.toString());
			e.printStackTrace();
		}
		return result; 
	}
	
	public void deAuthenticateUser() {
		account = null;
		authenticated = false;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}
}
