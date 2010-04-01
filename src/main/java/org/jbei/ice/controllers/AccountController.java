package org.jbei.ice.controllers;

import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.authentication.AuthenticationBackendException;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager;
import org.jbei.ice.lib.authentication.IAuthenticationBackend;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager.AuthenticationBackendManagerException;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AccountPreferencesManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.PersistentSessionDataWrapper;

public class AccountController {
    public static Account get(int id) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.get(id);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    public static Set<Account> getAllByFirstName() throws ControllerException {
        Set<Account> accounts = null;

        try {
            accounts = AccountManager.getAllByFirstName();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return accounts;
    }

    public static Account getByEmail(String email) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.getByEmail(email);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    public static Account save(Account account) throws ControllerException {
        Account result = null;

        try {
            result = AccountManager.save(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    public static Boolean isModerator(Account account) throws ControllerException {
        Boolean result = false;

        try {
            result = AccountManager.isModerator(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    public static Boolean isValidPassword(Account account, String password)
            throws ControllerException {
        if (account == null) {
            throw new ControllerException("Failed ot verify password for null Account!");
        }

        Boolean result = false;

        if (account.getPassword().equals(encryptPassword(password))) {
            result = true;
        }

        return result;
    }

    public static Account getAccountBySessionKey(String sessionKey) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.getAccountByAuthToken(sessionKey);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    public static String encryptPassword(String password) {
        return Utils.encryptMD5(JbeirSettings.getSetting("SECRET_KEY") + password);
    }

    public static AccountPreferences getAccountPreferences(Account account)
            throws ControllerException {
        AccountPreferences accountPreferences;

        try {
            accountPreferences = AccountPreferencesManager.getAccountPreferences(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return accountPreferences;
    }

    public static SessionData authenticate(String login, String password)
            throws InvalidCredentialsException, ControllerException {
        SessionData result = null;
        IAuthenticationBackend authenticationBackend = null;

        try {
            Thread.sleep(3000); // sets 3 seconds delay on login to prevent login/password bruteforce hacking 
        } catch (InterruptedException e) {
            throw new ControllerException(e);
        }

        try {
            authenticationBackend = AuthenticationBackendManager.loadAuthenticationBackend();
        } catch (AuthenticationBackendManagerException e) {
            throw new ControllerException(e);
        }

        Account account = null;
        try {
            account = authenticationBackend.authenticate(login, password);
        } catch (AuthenticationBackendException e2) {
            throw new InvalidCredentialsException(e2);
        }

        if (account != null) {
            AccountPreferences accountPreferences = AccountController
                    .getAccountPreferences(account);

            if (accountPreferences == null) {
                accountPreferences = new AccountPreferences();

                accountPreferences.setAccount(account);

                AccountController.saveAccountPreferences(accountPreferences);
            }

            try {
                result = PersistentSessionDataWrapper.getInstance().newSessionData(account);
            } catch (ManagerException e) {
                throw new ControllerException(e);
            }
        }

        return result;
    }

    public static boolean isAuthenticated(String sessionKey) throws ControllerException {
        boolean result = false;
        try {
            SessionData sessionData = PersistentSessionDataWrapper.getInstance().getSessionData(
                    sessionKey);
            if (sessionData != null) {
                result = true;
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
        return result;
    }

    public static void deauthenticate(String sessionKey) throws ControllerException {
        if (isAuthenticated(sessionKey)) {
            try {
                PersistentSessionDataWrapper.getInstance().delete(sessionKey);
            } catch (ManagerException e) {
                throw new ControllerException(e);
            }
        }
    }

    public static void saveAccountPreferences(AccountPreferences accountPreferences)

    throws ControllerException {
        try {
            AccountPreferencesManager.save(accountPreferences);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }
}
