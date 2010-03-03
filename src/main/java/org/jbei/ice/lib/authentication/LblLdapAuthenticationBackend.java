package org.jbei.ice.lib.authentication;

import java.io.Serializable;
import java.util.Calendar;

import javax.naming.NamingException;

import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.LblLdapAuth;
import org.jbei.ice.web.IceSession;

public class LblLdapAuthenticationBackend implements IAuthenticationBackend, Serializable {
    private static final long serialVersionUID = 1L;

    public Account authenticate(String loginId, String password) {
        Account account = null;
        LblLdapAuth l = null;

        try {
            l = new LblLdapAuth();
        } catch (NamingException e1) {
            Logger.debug("Could not initialize ldap auth");
        }

        try {
            loginId = loginId.toLowerCase();
            if (l == null) {
                throw new Exception("Could not initialize ldap auth object");
            } else if (l.isWikiUser(loginId)) {
                l.authenticate(loginId, password);
                account = AccountManager.getByEmail(loginId + "@lbl.gov");

                if (account == null) {
                    account = new Account();
                }

                account.setEmail(l.geteMail());
                account.setFirstName(l.getGivenName());
                account.setLastName(l.getSirName());
                account.setInstitution(l.getOrg());
                account.setDescription(l.getDescription());
                account.setPassword("");
                account.setIsSubscribed(1);
                account.setInitials("");

                WebClientInfo temp = (WebClientInfo) IceSession.get().getClientInfo();
                String ip = temp.getProperties().getRemoteAddress();

                account.setIp(ip);

                account.setLastLoginTime(Calendar.getInstance().getTime());

                AccountManager.save(account);

                Logger.info("User " + loginId + " authenticated via lbl-ldap.");
            } else {
                // try local backend
                LocalBackend localBackend = new LocalBackend();
                account = localBackend.authenticate(loginId, password);
            }
        } catch (Exception e) {
            Logger.warn("LDAP authentication failed for " + loginId + " with " + e.toString());
        }

        return account;
    }
}
