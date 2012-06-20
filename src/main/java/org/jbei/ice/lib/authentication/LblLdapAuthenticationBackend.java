package org.jbei.ice.lib.authentication;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.utils.LblLdapAuthenticationWrapper;
import org.jbei.ice.lib.utils.LblLdapAuthenticationWrapper.LblLdapAuthenticationWrapperException;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Authentication Backend for LDAP authentication at JBEI's home institution (Lawrence Berkeley Lab,
 * aka LBL). This class could be used as a template for creating one's own ldap authentication back
 * end.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Joanna Chen, Hector Plahar
 */
public class LblLdapAuthenticationBackend implements IAuthenticationBackend, Serializable {
    private static String LBL_LDAP_EMAIL_SUFFIX = "@lbl.gov";
    private AccountController accountController;

    public LblLdapAuthenticationBackend() {
        accountController = new AccountController();
    }

    @Override
    public String getBackendName() {
        return "LblLdapAuthenticationBackend";
    }

    private static final long serialVersionUID = 1L;

    @Override
    public Account authenticate(String loginId, String password)
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

                account = accountController.getByEmail(loginId + LBL_LDAP_EMAIL_SUFFIX);

                Date currentTime = Calendar.getInstance().getTime();

                if (account == null) {
                    account = new Account();
                    account.setCreationTime(currentTime);
                }

                account.setEmail(lblLdapAuthenticationWrapper.geteMail());
                account.setFirstName(lblLdapAuthenticationWrapper.getGivenName());
                account.setLastName(lblLdapAuthenticationWrapper.getSirName());
                account.setInstitution(lblLdapAuthenticationWrapper.getOrg());
                account.setDescription(lblLdapAuthenticationWrapper.getDescription());
                account.setPassword("");
                account.setIsSubscribed(1);
                account.setInitials("");
                account.setIp("");
                account.setModificationTime(currentTime);

                accountController.save(account);
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
}
