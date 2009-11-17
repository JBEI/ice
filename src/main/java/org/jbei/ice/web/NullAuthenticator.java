package org.jbei.ice.web;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.models.Account;

public class NullAuthenticator extends Authenticator {

	public Account authenticate(String userId, String password) {
		Account account = null;
		try {
			//account = AccountManager.getById(1);
			account = AccountManager.getByLogin(userId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.warn("null authentication failed with "+ e.toString());
		}
		return  account;
	}
}
