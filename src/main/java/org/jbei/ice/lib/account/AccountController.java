package org.jbei.ice.lib.account;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.authentication.AuthenticationException;
import org.jbei.ice.lib.account.authentication.IAuthentication;
import org.jbei.ice.lib.account.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.account.authentication.UserIdAuthentication;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.AccountPreferences;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.AccountDAO;
import org.jbei.ice.lib.dao.hibernate.AccountPreferencesDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.session.PersistentSessionDataWrapper;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.lang.StringUtils;

/**
 * ABI to manipulate {@link Account} objects.
 * <p/>
 * This class contains methods that wrap {@link org.jbei.ice.lib.dao.hibernate.AccountDAO} to manipulate {@link
 * Account}
 * objects.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */

public class AccountController {

    private static final String ADMIN_ACCOUNT_EMAIL = "Administrator";
    private static final String ADMIN_ACCOUNT_PASSWORD = "Administrator";
    private final AccountDAO dao;
    private final AccountPreferencesDAO accountPreferencesDAO;

    public AccountController() {
        dao = DAOFactory.getAccountDAO();
        accountPreferencesDAO = DAOFactory.getAccountPreferencesDAO();
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
    public void resetPassword(String email, boolean sendEmail, String url) throws ControllerException {
        Account account = getByEmail(email);
        if (account == null)
            throw new ControllerException("Could not retrieve account for account id " + email);

        String newPassword = Utils.generateUUID().substring(24);
        String encryptedNewPassword = AccountUtils.encryptPassword(newPassword, account.getSalt());
        account.setPassword(encryptedNewPassword);

        save(account);
        if (!sendEmail || url == null || url.trim().isEmpty())
            return;

        String projectName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
        String subject = projectName + " Password Reminder";
        String name = account.getFirstName();
        if (StringUtils.isBlank(name)) {
            name = account.getLastName();
            if (StringUtils.isBlank(name))
                name = email;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy 'at' HH:mm aaa, z");

        StringBuilder builder = new StringBuilder();
        builder.append("Dear ").append(name).append(",\n\n")
               .append("The password for your ").append(projectName)
               .append(" account (").append(email).append(") was reset on ")
               .append(dateFormat.format(new Date())).append(". Your new temporary password is\n\n")
               .append(newPassword).append("\n\n")
               .append("Please go to the following link to login and change your password.\n\n").append(url)
               .append("\n\nThank you.");

        Emailer.send(account.getEmail(), subject, builder.toString());
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

        account.setPassword(AccountUtils.encryptPassword(password, account.getSalt()));
        save(account);
    }

    /**
     * validates the account dto to ensure that the fields required (especially by the database)
     * are present
     *
     * @param accountTransfer account dto for validation
     * @throws ControllerException if validation fails
     */
    private void validateRequiredAccountFields(AccountTransfer accountTransfer) throws ControllerException {
        if (accountTransfer.getFirstName() == null || accountTransfer.getFirstName().trim().isEmpty())
            throw new ControllerException("Account first name is required");

        if (accountTransfer.getLastName() == null || accountTransfer.getLastName().trim().isEmpty())
            throw new ControllerException("Account last name is required");

        if (accountTransfer.getEmail() == null || accountTransfer.getEmail().trim().isEmpty()) {
            throw new ControllerException("Cannot create account without user id");
        }
    }

    /**
     * Creates a new account using the parameters passed. A random password is initially generated ,
     * encrypted and assigned to the account
     *
     * @param info      contains information needed to create account
     * @param sendEmail whether to send account information (including password by email)
     * @return generated password
     * @throws ControllerException in the event email is already assigned to another user or is empty
     */
    public String createNewAccount(AccountTransfer info, boolean sendEmail) throws ControllerException {
        // validate fields required by the database
        validateRequiredAccountFields(info);

        String email = info.getEmail().trim();
        if (getByEmail(email) != null) {
            throw new ControllerException("Account with id \"" + email + "\" already exists");
        }

        // generate salt and encrypt password before storing
        String salt = Utils.generateSaltForUserAccount();
        String newPassword = Utils.generateUUID().substring(24);
        String encryptedPassword = AccountUtils.encryptPassword(newPassword, salt);

        Account account = AccountUtils.fromDTO(info);
        account.setPassword(encryptedPassword);
        account.setSalt(salt);
        account.setCreationTime(Calendar.getInstance().getTime());
        save(account);

        if (!sendEmail)
            return newPassword;

        String subject = "Account created successfully";
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Dear ").append(info.getEmail()).append(", ")
                     .append("\n\nThank you for creating a ")
                     .append(Utils.getConfigValue(ConfigurationKey.PROJECT_NAME))
                     .append(" account. \nBy accessing ")
                     .append("this site with the password provided at the bottom ")
                     .append("you agree to the following terms:\n\n");

        String terms = "Biological Parts IP Disclaimer: \n\n"
                + "The JBEI Registry of Biological Parts Software is licensed under a standard BSD\n"
                + "license. Permission or license to use the biological parts registered in\n"
                + "the JBEI Registry of Biological Parts is not included in the BSD license\n"
                + "to use the JBEI Registry Software. Berkeley Lab and JBEI make no representation\n"
                + "that the use of the biological parts registered in the JBEI Registry of\n"
                + "Biological Parts will not infringe any patent or other proprietary right.";

        stringBuilder.append(terms);
        stringBuilder.append("\n\nYour new password is: ")
                     .append(newPassword)
                     .append("\nYour login id is: ")
                     .append(info.getEmail())
                     .append("\n\n");

        String server = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        if (server != null && !server.isEmpty()) {
            stringBuilder.append("Use it to login at ")
                         .append(server)
                         .append(". ");
        }
        stringBuilder.append("\nPlease remember to change your password by going to your profile page.\n\n");
        Emailer.send(info.getEmail(), subject, stringBuilder.toString());
        return newPassword;
    }

    public Account createAdminAccount() throws ControllerException {
        Account adminAccount = getByEmail(ADMIN_ACCOUNT_EMAIL);
        if (adminAccount != null)
            return adminAccount;

        adminAccount = new Account();
        adminAccount.setEmail(ADMIN_ACCOUNT_EMAIL);
        adminAccount.setLastName("Administrator");
        adminAccount.setFirstName("");
        adminAccount.setInitials("");
        adminAccount.setInstitution("");
        adminAccount.setSalt(Utils.generateSaltForUserAccount());
        adminAccount.setPassword(AccountUtils.encryptPassword(ADMIN_ACCOUNT_PASSWORD, adminAccount.getSalt()));
        adminAccount.setDescription("Administrator Account");

        adminAccount.setIp("");
        Date currentTime = Calendar.getInstance().getTime();
        adminAccount.setCreationTime(currentTime);
        adminAccount.setModificationTime(currentTime);
        adminAccount.setLastLoginTime(currentTime);
        adminAccount.setType(AccountType.ADMIN);
        return save(adminAccount);
    }

    /**
     * Retrieve {@link Account} by user id.
     *
     * @param email unique identifier for account, typically email
     * @return {@link Account}
     * @throws ControllerException
     */
    public Account getByEmail(String email) {
        try {
            return dao.getByEmail(email);
        } catch (DAOException de) {
            Logger.debug("Could not retrieve by email " + email);
            return null;
        }
    }

    public long getAccountId(String email) throws ControllerException {
        Account account;
        try {
            account = dao.getByEmail(email);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        if (account == null)
            throw new ControllerException("Could not retrieve account for " + email);
        return account.getId();
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
            if (account.getSalt() == null || account.getSalt().isEmpty())
                account.setSalt(Utils.generateSaltForUserAccount());
            result = dao.create(account);
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

        return account.getPassword().equals(AccountUtils.encryptPassword(password, account.getSalt()));
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
            accountPreferences = accountPreferencesDAO.getAccountPreferences(account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return accountPreferences;
    }

    /**
     * Authenticate a user in the database.
     * <p/>
     * Using the {@link org.jbei.ice.lib.account.authentication.IAuthentication} specified in the settings file,
     * authenticate
     * the
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
        IAuthentication authentication = new UserIdAuthentication();

        try {
            if (!authentication.authenticates(login.trim(), password)) {
                try {
                    Thread.sleep(2000); // sets 2 seconds delay on login to prevent login/password brute force hacking
                } catch (InterruptedException ie) {
                    Logger.warn(ie.getMessage());
                }
                return null;
            }
        } catch (AuthenticationException e2) {
            try {
                Thread.sleep(2000); // sets 2 seconds delay on login to prevent login/password brute force hacking
            } catch (InterruptedException ie) {
                throw new ControllerException(ie);
            }
            throw new ControllerException(e2);
        }

        Account account = dao.getByEmail(login);
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
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        }

        return result;
    }

    /**
     * Authenticate a user in the database.
     * <p/>
     * Using the {@link org.jbei.ice.lib.account.authentication.IAuthentication} specified in the settings file,
     * authenticate
     * the
     * user, and return the sessionData
     *
     * @param login
     * @param password
     * @return {@link AccountTransfer}
     * @throws InvalidCredentialsException
     * @throws ControllerException
     */
    public AccountTransfer authenticate(String login, String password)
            throws InvalidCredentialsException, ControllerException {
        SessionData sessionData = authenticate(login, password, "");
        if (sessionData == null)
            return null;

        Account account = sessionData.getAccount();
        AccountTransfer info = account.toDataTransferObject();
        if (info == null)
            return info;

        info.setLastLogin(account.getLastLoginTime());
        info.setId(account.getId());
        boolean isAdmin = isAdministrator(account);
        info.setAdmin(isAdmin);
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
            SessionData sessionData = PersistentSessionDataWrapper.getInstance().getSessionData(sessionKey);
            if (sessionData != null) {
                result = true;
            }
        } catch (DAOException e) {
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
            accountPreferencesDAO.create(accountPreferences);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public void resetUserPassword(String email, String url) throws ControllerException {
        Account account = getByEmail(email);

        if (account == null)
            return;

        String newPassword = Utils.generateUUID().substring(24);
        account.setPassword(AccountUtils.encryptPassword(newPassword, account.getSalt()));
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

    public Set<Account> getMatchingAccounts(Account account, String query, int limit) throws ControllerException {
        try {
            return dao.getMatchingAccounts(account, query, limit);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

//    public AccountResults retrieveAccounts(Account account, int start, int limit) throws ControllerException {
//        if (!isAdministrator(account)) {
//            Logger.warn(account.getEmail() + " attempting to retrieve all user accounts without admin privileges");
//            return null;
//        }
//
//        try {
//            AccountResults results = new AccountResults();
//            EntryController entryController = new EntryController();
//            LinkedList<Account> accounts = dao.retrieveAccounts(start, limit);
//
//            ArrayList<AccountTransfer> infos = new ArrayList<>();
//            for (Account userAccount : accounts) {
//                AccountTransfer info = new AccountTransfer();
//                long count;
//                try {
//                    count = entryController.getNumberOfOwnerEntries(userAccount, userAccount.getEmail());
//                    info.setUserEntryCount(count);
//                } catch (ControllerException e) {
//                    Logger.error("Error retrieving entry count for user " + userAccount.getEmail());
//                    info.setUserEntryCount(-1);
//                }
//
//                info.setEmail(userAccount.getEmail());
//                info.setAdmin(isAdministrator(userAccount));
//                info.setFirstName(userAccount.getFirstName());
//                info.setLastName(userAccount.getLastName());
//                info.setLastLogin(userAccount.getLastLoginTime());
//                info.setId(userAccount.getId());
//                info.setAccountType(userAccount.getType());
//                infos.add(info);
//            }
//            results.getResults().addAll(infos);
//            int count = dao.retrieveAllNonSystemAccountCount();
//            results.setResultCount(count);
//            return results;
//
//        } catch (DAOException e) {
//            throw new ControllerException(e);
//        }
//    }

    public void removeMemberFromGroup(long id, String email) throws ControllerException {
        Account account = getByEmail(email);
        if (account == null)
            throw new ControllerException("Could not find account " + email);

        Group group = new GroupController().getGroupById(id);
        if (group == null)
            throw new ControllerException("Could not find group " + id);
        account.getGroups().remove(group);
        try {
            dao.update(account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
