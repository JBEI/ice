package org.jbei.ice.controllers;

import java.util.Calendar;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.authentication.AuthenticationBackendException;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager.AuthenticationBackendManagerException;
import org.jbei.ice.lib.authentication.IAuthenticationBackend;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AccountPreferencesManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.PersistentSessionDataWrapper;

/**
 * ABI to manipulate {@link Account} objects.
 * <p>
 * This class contains methods that wrap {@link AccountManager} to manipulate {@link Account}
 * objects.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 */

public class AccountController {

    /**
     * Retrieve account from the database by database id.
     * 
     * @param id
     *            Database id of account
     * @return Account for the id
     * @throws ControllerException
     */
    public static Account get(long id) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.get(id);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    public static String createNewAccount(String firstName, String lastName, String initials,
            String email, String institution, String description) throws ControllerException {
        if (email == null || email.isEmpty()) {
            throw new ControllerException("Cannot create account with null email");
        }

        if (getByEmail(email) != null) {
            throw new ControllerException("Account with email \"" + email + "\" already exists");
        }

        if (initials == null) {
            initials = "";
        }
        if (institution == null) {
            institution = "";
        }
        if (description == null) {
            description = "";
        }

        String newPassword = Utils.generateUUID().substring(24);
        Account account = new Account(firstName, lastName, initials, email,
                AccountController.encryptPassword(newPassword), institution, description);
        account.setIp("");
        account.setIsSubscribed(1);
        account.setCreationTime(Calendar.getInstance().getTime());
        if (AccountController.save(account) == null)
            throw new ControllerException("Could not save new account");
        return newPassword;
    }

    /**
     * Retrieve all account from the database, sorted by Given (First) Name.
     * 
     * @return Accounts
     * @throws ControllerException
     */
    public static Set<Account> getAllByFirstName() throws ControllerException {
        Set<Account> accounts = null;

        try {
            accounts = AccountManager.getAllByFirstName();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return accounts;
    }

    /**
     * Retrieve {@link Account} by email.
     * 
     * @param email
     *            of the account
     * @return {@link Account}
     * @throws ControllerException
     */
    public static Account getByEmail(String email) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.getByEmail(email);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    /**
     * Store {@link Account} into the database.
     * 
     * @param account
     * @return {@link Account} that has been saved.
     * @throws ControllerException
     */
    public static Account save(Account account) throws ControllerException {
        Account result = null;

        try {
            result = AccountManager.save(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Check in the database if an account is a moderator.
     * 
     * @param account
     * @return True, if the account is a moderator.
     * @throws ControllerException
     */
    public static Boolean isModerator(Account account) throws ControllerException {
        Boolean result = false;

        try {
            result = AccountManager.isModerator(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Check if the given password is valid for the account.
     * 
     * @param account
     * @param password
     * @return True if correct password.
     * @throws ControllerException
     */
    public static Boolean isValidPassword(Account account, String password)
            throws ControllerException {
        if (account == null) {
            throw new ControllerException("Failed to verify password for null Account!");
        }

        Boolean result = false;

        if (account.getPassword().equals(encryptPassword(password))) {
            result = true;
        }

        return result;
    }

    /**
     * Retrieve the {@link Account} by session key.
     * 
     * @param sessionKey
     * @return Account associated with the session key.
     * @throws ControllerException
     */
    public static Account getAccountBySessionKey(String sessionKey) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.getAccountByAuthToken(sessionKey);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    /**
     * Return the encrypted version of the given password, using the salt from the settings file.
     * 
     * @param password
     * @return 40 character encrypted string.
     */
    public static String encryptPassword(String password) {
        return Utils.encryptSHA(JbeirSettings.getSetting("SECRET_KEY") + password);
    }

    /**
     * Return the {@link AccountPreferences} of the given account.
     * 
     * @param account
     * @return accountPreference
     * @throws ControllerException
     */
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

    /**
     * Authenticate a user in the database.
     * <p>
     * Using the {@link IAuthenticationBackend} specified in the settings file, authenticate the
     * user, and return the sessionData
     * 
     * @param login
     * @param password
     * @param ip
     *            IP Address of the user.
     * @return {@link SessionData}
     * @throws InvalidCredentialsException
     * @throws ControllerException
     */
    public static SessionData authenticate(String login, String password, String ip)
            throws InvalidCredentialsException, ControllerException {
        SessionData result = null;
        IAuthenticationBackend authenticationBackend = null;

        try {
            Thread.sleep(2000); // sets 2 seconds delay on login to prevent login/password bruteforce hacking
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

            account.setIp(ip);
            account.setLastLoginTime(Calendar.getInstance().getTime());
            save(account);
            try {
                result = PersistentSessionDataWrapper.getInstance().newSessionData(account);
            } catch (ManagerException e) {
                throw new ControllerException(e);
            }
        }

        return result;
    }

    /**
     * Authenticate a user in the database.
     * <p>
     * Using the {@link IAuthenticationBackend} specified in the settings file, authenticate the
     * user, and return the sessionData
     * 
     * @param login
     * @param password
     * @return {@link SessionData}
     * @throws InvalidCredentialsException
     * @throws ControllerException
     */
    public static SessionData authenticate(String login, String password)
            throws InvalidCredentialsException, ControllerException {
        return authenticate(login, password, "");
    }

    /**
     * See if the given sessionKey is still authenticated with the system.
     * 
     * @param sessionKey
     * @return True if sessionKey is still authenticated (active) to the system.
     * @throws ControllerException
     */
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

    /**
     * Deauthentcate the given sessionKey. The user is logged out from the system.
     * 
     * @param sessionKey
     * @throws ControllerException
     */
    public static void deauthenticate(String sessionKey) throws ControllerException {
        if (isAuthenticated(sessionKey)) {
            try {
                PersistentSessionDataWrapper.getInstance().delete(sessionKey);
            } catch (ManagerException e) {
                throw new ControllerException(e);
            }
        }
    }

    /**
     * Save {@link AccountPreferences} to the database.
     * 
     * @param accountPreferences
     * @throws ControllerException
     */
    public static void saveAccountPreferences(AccountPreferences accountPreferences)
            throws ControllerException {
        try {
            AccountPreferencesManager.save(accountPreferences);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the System account from the database.
     * 
     * @return Account for the system account.
     * @throws ControllerException
     */
    public static Account getSystemAccount() throws ControllerException {
        Account account = null;
        try {
            account = AccountManager.getByEmail("system");
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
        return account;
    }

    public static void resetUserPassword(String email, String url) throws ControllerException {
        Account account = getByEmail(email);

        if (account == null)
            return;

        String newPassword = Utils.generateUUID().substring(24);
        account.setPassword(AccountController.encryptPassword(newPassword));
        AccountController.save(account);
        String subject = "JBEI Registry Password Reminder";
        String body = "A request has been made to reset your password.\n\n";
        body = body + "Your new password is " + newPassword + ".\n\n";
        body = body + "Please go to the following link and change your password.\n\n";
        body = body + url;

        try {
            Emailer.send(account.getEmail(), subject, body);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
}
