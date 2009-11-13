package org.jbei.ice.web;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.LblLdapAuth;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.models.Account;

public class LblLdapAuthenticator extends Authenticator{

		public Account authenticate(String loginId, String password) {
			Account result = null;
			LblLdapAuth l = new LblLdapAuth();
			try {
				l.initialize("ldaps://ldapauth.lbl.gov");
				loginId = loginId.toLowerCase();
				
				if (l.isWikiUser(loginId)) {
					l.authenticate(loginId, password);
					result = AccountManager.getByLogin(loginId + "@lbl.gov");
					Logger.debug("User authenticated via lbl-ldap.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.warn("authentication failed for " + loginId + " with "+ e.toString());
			}
			
			return result;
	}

}
