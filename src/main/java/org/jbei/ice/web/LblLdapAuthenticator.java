package org.jbei.ice.web;

import java.util.Calendar;

import javax.naming.NamingException;

import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.LblLdapAuth;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.models.Account;

public class LblLdapAuthenticator extends Authenticator{

		public Account authenticate(String loginId, String password) {
			Account account = null;
			LblLdapAuth l = null;
			try {
				l = new LblLdapAuth();
			} catch (NamingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				loginId = loginId.toLowerCase();
				
				if (l.isWikiUser(loginId)) {
					l.authenticate(loginId, password);
					account = AccountManager.getByLogin(loginId + "@lbl.gov");
					
					if (account == null) {
						account = new Account();
					}
					
					account.setEmail(l.geteMail());
					account.setFirstName(l.getGivenName());
					account.setLastName(l.getSirName());
					account.setInstitution(l.getOrg());
					account.setDescription(l.getDescription());
					
					WebClientInfo temp = (WebClientInfo) IceSession.get().getClientInfo();
					String ip = temp.getProperties().getRemoteAddress();
										
					account.setIp(ip);
					
					account.setLastLoginTime(Calendar.getInstance().getTime());
					
					//AccountManager.dbSave(account);
					
					Logger.info("User " + loginId + " authenticated via lbl-ldap.");

				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.warn("authentication failed for " + loginId + " with "+ e.toString());
			}
			
			return account;
	}

}
