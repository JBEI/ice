package org.jbei.ice.lib.authentication.ldap;

import java.io.Serializable;

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
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.shared.dto.user.User;

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

                if (account == null) {
                    User user = new User();
                    user.setAccountType(AccountType.NORMAL);
                    user.setEmail(lblLdapAuthenticationWrapper.geteMail());
                    user.setFirstName(lblLdapAuthenticationWrapper.getGivenName());
                    user.setLastName(lblLdapAuthenticationWrapper.getSirName());
                    user.setInstitution(lblLdapAuthenticationWrapper.getOrg());
                    user.setDescription(lblLdapAuthenticationWrapper.getDescription());

                    accountController.createNewAccount(user, false);
                    account = accountController.getByEmail(user.getEmail());
                }
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
