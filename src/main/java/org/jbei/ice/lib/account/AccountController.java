package org.jbei.ice.lib.account;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.AccountPreferences;
import org.jbei.ice.lib.account.model.AccountType;
import org.jbei.ice.lib.authentication.AuthenticationBackendException;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager.AuthenticationBackendManagerException;
import org.jbei.ice.lib.authentication.IAuthenticationBackend;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.session.PersistentSessionDataWrapper;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.AccountInfo;

/**
 * ABI to manipulate {@link Account} objects.
 * <p/>
 * This class contains methods that wrap {@link AccountDAO} to manipulate {@link Account} objects.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */

public class AccountController {

    private static String SYSTEM_ACCOUNT_EMAIL = "system";
    private final AccountDAO dao; // TODO : setter injection
    private final PreferencesDAO preferencesDAO;

    public AccountController() {
        dao = new AccountDAO();
        preferencesDAO = new PreferencesDAO();
    }

    /**
     * Retrieve account from the database by database id.
     *
     * @param id Database id of account
     * @return Account for the id
     * @throws ControllerException
     */
    public Account get(long id) throws ControllerException {
        try {
            return dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Changes user's password
     *
     * @param email     unique account identifier
     * @param sendEmail whether to notify user of password change
     * @param url       current site url
     * @throws ControllerException if account could not be retrieved using unique identifier
     */
    public void resetPassword(String email, boolean sendEmail, String url)
            throws ControllerException {
        Account account = getByEmail(email);
        if (account == null)
            throw new ControllerException("Could not retrieve account for account id " + email);

        String newPassword = Utils.generateUUID().substring(24);
        String encryptedNewPassword = AccountUtils.encryptPassword(newPassword);
        account.setPassword(encryptedNewPassword);

        save(account);

        if (sendEmail && url != null && !url.isEmpty()) {
            String subject = JbeirSettings.getSetting("PROJECT_NAME") + " Password Reminder";
            String body = "Someone (maybe you) have requested to reset your password.\n\n";
            body = body + "Your new password is " + newPassword + ".\n\n";
            body = body + "Please go to the following link and change your password.\n\n";
            body = body + url;
            Emailer.send(account.getEmail(), subject, body);
        }
    }

    /**
     * Updates account password associated the account email. It encrypts it before associating it
     * with the account
     *
     * @param email    user unique identifier
     * @param password new password
     * @throws ControllerException
     */
    public void updatePassword(String email, String password) throws ControllerException {
        Account account = getByEmail(email);
        if (account == null)
            throw new ControllerException("Could not retrieve account for account id " + email);

        account.setPassword(AccountUtils.encryptPassword(password));
        save(account);
    }

    /**
     * Creates a new account using the parameters passed. A random password is initially generated ,
     * encrypted and
     * assigned to the account
     *
     * @param firstName   account first name
     * @param lastName    account last name
     * @param initials    account initials
     * @param email       unique identifier for account
     * @param institution account institution affiliation
     * @param description account description
     * @return generated password
     * @throws ControllerException in the event email is already assigned to another user or is
     *                             empty
     */
    public String createNewAccount(String firstName, String lastName, String initials,
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
        String encryptedPassword = AccountUtils.encryptPassword(newPassword);
        Account account = new Account(firstName, lastName, initials, email, encryptedPassword,
                                      institution, description);
        account.setIp("");
        account.setIsSubscribed(1);
        account.setCreationTime(Calendar.getInstance().getTime());
        if (save(account) == null)
            throw new ControllerException("Could not save new account");
        return newPassword;
    }

    public Account createAdminAccount(String adminAccountEmail, String adminPassword)
            throws ControllerException {
        if (getByEmail(adminAccountEmail) != null) {
            throw new ControllerException("Account with email \"" + adminAccountEmail + "\" already exists");
        }

        Account adminAccount = new Account();
        adminAccount.setEmail(adminAccountEmail);
        adminAccount.setLastName("Administrator");
        adminAccount.setFirstName("");
        adminAccount.setInitials("");
        adminAccount.setInstitution("");
        adminAccount.setPassword(AccountUtils.encryptPassword(adminPassword));
        adminAccount.setDescription("Administrator Account");
        adminAccount.setIsSubscribed(0);

        adminAccount.setIp("");
        Date currentTime = Calendar.getInstance().getTime();
        adminAccount.setCreationTime(currentTime);
        adminAccount.setModificationTime(currentTime);
        adminAccount.setLastLoginTime(currentTime);
        adminAccount.setType(AccountType.ADMIN);
        return save(adminAccount);
    }

    /**
     * Retrieve {@link Account} by email.
     *
     * @param email of the account
     * @return {@link Account}
     * @throws ControllerException
     */
    public Account getByEmail(String email) throws ControllerException {
        try {
            return dao.getByEmail(email);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Store {@link Account} into the database.
     *
     * @param account
     * @return {@link Account} that has been saved.
     * @throws ControllerException
     */
    public Account save(Account account) throws ControllerException {
        Account result;

        try {
            account.setModificationTime(Calendar.getInstance().getTime());
            result = dao.save(account);
        } catch (DAOException e) {
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
    public Boolean isAdministrator(Account account) throws ControllerException {
        if (account == null)
            return false;

        account = this.get(account.getId());
        return account.getType() == AccountType.ADMIN;
    }

    /**
     * Check if the given password is valid for the account.
     *
     * @param account
     * @param password
     * @return True if correct password.
     * @throws ControllerException
     */
    public Boolean isValidPassword(Account account, String password) throws ControllerException {
        if (account == null) {
            throw new ControllerException("Failed to verify password for null Account!");
        }

        Boolean result = false;

        if (account.getPassword().equals(AccountUtils.encryptPassword(password))) {
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
    public Account getAccountBySessionKey(String sessionKey) throws ControllerException {
        try {
            return dao.getAccountByAuthToken(sessionKey);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Return the {@link AccountPreferences} of the given account.
     *
     * @param account
     * @return accountPreference
     * @throws ControllerException
     */
    public AccountPreferences getAccountPreferences(Account account) throws ControllerException {
        AccountPreferences accountPreferences;

        try {
            accountPreferences = preferencesDAO.getAccountPreferences(account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return accountPreferences;
    }

    /**
     * Authenticate a user in the database.
     * <p/>
     * Using the {@link IAuthenticationBackend} specified in the settings file, authenticate the
     * user, and return the sessionData
     *
     * @param login
     * @param password
     * @param ip       IP Address of the user.
     * @return {@link SessionData}
     * @throws InvalidCredentialsException
     * @throws ControllerException
     */
    public SessionData authenticate(String login, String password, String ip)
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
            AccountPreferences accountPreferences = getAccountPreferences(account);

            if (accountPreferences == null) {
                accountPreferences = new AccountPreferences();
                accountPreferences.setAccount(account);
                saveAccountPreferences(accountPreferences);
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
     * <p/>
     * Using the {@link IAuthenticationBackend} specified in the settings file, authenticate the
     * user, and return the sessionData
     *
     * @param login
     * @param password
     * @return {@link AccountInfo}
     * @throws InvalidCredentialsException
     * @throws ControllerException
     */
    public AccountInfo authenticate(String login, String password)
            throws InvalidCredentialsException, ControllerException, ManagerException {
        SessionData sessionData = authenticate(login, password, "");
        if (sessionData == null)
            return null;

        Account account = sessionData.getAccount();
        AccountInfo info = AccountUtils.accountToInfo(account);
        if (info == null)
            return info;

        boolean isModerator = isAdministrator(account);
        info.setModerator(isModerator);
        info.setSessionId(sessionData.getSessionKey());
        return info;
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
     * De-authenticate the given sessionKey. The user is logged out from the system.
     *
     * @param sessionKey
     * @throws ControllerException
     */
    public static void deauthenticate(String sessionKey) throws ControllerException {
        if (isAuthenticated(sessionKey)) {
            PersistentSessionDataWrapper.getInstance().delete(sessionKey);
        }
    }

    /**
     * Save {@link AccountPreferences} to the database.
     *
     * @param accountPreferences
     * @throws ControllerException
     */
    public void saveAccountPreferences(AccountPreferences accountPreferences)
            throws ControllerException {
        try {
            preferencesDAO.save(accountPreferences);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the System account from the database.
     *
     * @return Account for the system account.
     * @throws ControllerException
     */
    public Account getSystemAccount() throws ControllerException {
        Account account;
        try {
            account = dao.getByEmail(SYSTEM_ACCOUNT_EMAIL);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        return account;
    }

    public void resetUserPassword(String email, String url) throws ControllerException {
        Account account = getByEmail(email);

        if (account == null)
            return;

        String newPassword = Utils.generateUUID().substring(24);
        account.setPassword(AccountUtils.encryptPassword(newPassword));
        save(account);
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

    public Set<Account> getMatchingAccounts(String query, int limit) throws ControllerException {
        try {
            return dao.getMatchingAccounts(query, limit);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Account getAccountByAuthToken(String sessionKey) throws ControllerException {
        try {
            return dao.getAccountByAuthToken(sessionKey);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Account> retrieveAllAccounts() throws ControllerException {
        try {
            Set<Account> results = new HashSet<Account>(dao.getAllAccounts());
            return results;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
