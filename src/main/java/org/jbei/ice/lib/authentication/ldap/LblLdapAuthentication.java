package org.jbei.ice.lib.authentication.ldap;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.authentication.IAuthentication;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.authentication.LocalBackend;
import org.jbei.ice.lib.authentication.ldap.LblLdapAuthenticationWrapper.LblLdapAuthenticationWrapperException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.Utils;

/**
 * Authentication Backend for LDAP authentication at JBEI's home institution (Lawrence Berkeley Lab,
 * aka LBL). This class could be used as a template for creating one's own ldap authentication back
 * end.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Joanna Chen, Hector Plahar
 */
public class LblLdapAuthentication implements IAuthentication, Serializable {
    private static String LBL_LDAP_EMAIL_SUFFIX = "@lbl.gov";

    private static final long serialVersionUID = 1L;

    @Override
    public Account authenticate(String loginId, String password)
            throws AuthenticationException, InvalidCredentialsException {
        if (loginId == null || password == null) {
            throw new InvalidCredentialsException("Username and Password are mandatory!");
        }

        Account account;
        AccountController accountController = ControllerFactory.getAccountController();

        try {
            loginId = loginId.toLowerCase();
            LblLdapAuthenticationWrapper lblLdapAuthenticationWrapper = new LblLdapAuthenticationWrapper();
            if (lblLdapAuthenticationWrapper.isWikiUser(loginId)) {
                lblLdapAuthenticationWrapper.authenticate(loginId, password);

                account = accountController.getByEmail(loginId + LBL_LDAP_EMAIL_SUFFIX);
                Date currentTime = Calendar.getInstance().getTime();

                if (account == null) {
                    account = new Account();
                    account.setCreationTime(currentTime);
                    account.setSalt(Utils.generateUUID());
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
            Logger.warn(e.getMessage());
            throw new InvalidCredentialsException("Invalid credentials!");
        } catch (ControllerException e) {
            throw new AuthenticationException("LDAP authentication failed for " + loginId, e);
        }

        return account;
    }
}
