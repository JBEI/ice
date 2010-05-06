package org.jbei.ice.lib.authentication;

import java.io.Serializable;
import java.util.Calendar;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.LblLdapAuthenticationWrapper;
import org.jbei.ice.lib.utils.LblLdapAuthenticationWrapper.LblLdapAuthenticationWrapperException;

public class LblLdapAuthenticationBackend implements IAuthenticationBackend, Serializable {
    private static String LBL_LDAP_EMAIL_SUFFIX = "@lbl.gov";

    public String getBackendName() {
        return "LblLdapAuthenticationBackend";
    }

    private static final long serialVersionUID = 1L;

    public Account authenticate(String loginId, String password, String ip)
            throws AuthenticationBackendException, InvalidCredentialsException {
        if (loginId == null || password == null) {
            throw new InvalidCredentialsException("Username and Password are mandatory!");
        }

        Account account = null;

        try {
            LblLdapAuthenticationWrapper lblLdapAuthenticationWrapper = new LblLdapAuthenticationWrapper();

            loginId = loginId.toLowerCase();

            if (lblLdapAuthenticationWrapper.isWikiUser(loginId)) {
                lblLdapAuthenticationWrapper.authenticate(loginId, password);

                account = AccountController.getByEmail(loginId + LBL_LDAP_EMAIL_SUFFIX);

                if (account == null) {
                    account = new Account();
                }

                account.setEmail(lblLdapAuthenticationWrapper.geteMail());
                account.setFirstName(lblLdapAuthenticationWrapper.getGivenName());
                account.setLastName(lblLdapAuthenticationWrapper.getSirName());
                account.setInstitution(lblLdapAuthenticationWrapper.getOrg());
                account.setDescription(lblLdapAuthenticationWrapper.getDescription());
                account.setPassword("");
                account.setIsSubscribed(1);
                account.setInitials("");

                account.setIp(ip);

                account.setLastLoginTime(Calendar.getInstance().getTime());

                AccountController.save(account);
            } else {
                // try local backend
                LocalBackend localBackend = new LocalBackend();
                account = localBackend.authenticate(loginId, password);
            }
        } catch (LblLdapAuthenticationWrapperException e) {
            throw new InvalidCredentialsException("Invalid credentials!");
        } catch (ControllerException e) {
            throw new AuthenticationBackendException("LDAP authentication failed for " + loginId, e);
        }

        return account;
    }

    public Account authenticate(String loginId, String password)
            throws AuthenticationBackendException, InvalidCredentialsException {
        return authenticate(loginId, password, "");
    }
}
