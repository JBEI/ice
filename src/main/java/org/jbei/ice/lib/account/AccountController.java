package org.jbei.ice.lib.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.authentication.AuthenticationException;
import org.jbei.ice.lib.account.authentication.IAuthentication;
import org.jbei.ice.lib.account.authentication.LocalAuthentication;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.AccountPreferences;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.AccountDAO;
import org.jbei.ice.lib.dao.hibernate.AccountPreferencesDAO;
import org.jbei.ice.lib.dto.AccountResults;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;

/**
 * ABI to manipulate {@link Account} objects.
 * <p/>
 * This class contains methods that wrap {@link org.jbei.ice.lib.dao.hibernate.AccountDAO} to
 * manipulate {@link Account} objects.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */

public class AccountController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AccountController.class);
    private static final String ADMIN_ACCOUNT_EMAIL = "Administrator";
    private static final String ADMIN_ACCOUNT_PASSWORD = "Administrator";
    private final AccountDAO dao;
    private final AccountPreferencesDAO accountPreferencesDAO;

    /**
     * Default constructor.
     */
    public AccountController() {
        dao = DAOFactory.getAccountDAO();
        accountPreferencesDAO = DAOFactory.getAccountPreferencesDAO();
    }

    /**
     * @param requester
     * @param userId
     * @param transfer
     * @return updated account object
     */
    public AccountTransfer updateAccount(final String requester, final long userId,
            final AccountTransfer transfer) {
        final Account account = dao.get(userId);
        if (!account.getEmail().equalsIgnoreCase(requester) && !isAdministrator(requester)) {
            return null;
        }

        // if transfer has password then it is a password change
        if (!StringUtils.isEmpty(transfer.getFirstName())) {
            account.setFirstName(transfer.getFirstName());
        }

        if (!StringUtils.isEmpty(transfer.getLastName())) {
            account.setLastName(transfer.getLastName());
        }

        if (!StringUtils.isEmpty(transfer.getDescription())) {
            account.setDescription(transfer.getDescription());
        }

        if (!StringUtils.isEmpty(transfer.getInstitution())) {
            account.setInstitution(transfer.getInstitution());
        }

        return dao.update(account).toDataTransferObject();
    }

    /**
     * Retrieve account from the database by database id.
     *
     * @param id
     *            Database id of account
     * @return Account for the id
     */
    public Account get(final long id) {
        return dao.get(id);
    }

    /**
     * Reset a user's password
     *
     * @param targetEmail
     *            email address of user account to be changed
     * @return true if the user account is found with email specified in the parameter and password
     *         for it is successfully reset, false otherwise
     */
    public boolean resetPassword(final String targetEmail) {
        Account account = getByEmail(targetEmail);
        if (account == null) {
            return false;
        }

        try {
            final String newPassword = Utils.generateUUID().substring(24);
            final String encryptedNewPassword = AccountUtils.encryptNewUserPassword(newPassword,
                    account.getSalt());
            account.setPassword(encryptedNewPassword);

            account = dao.update(account);
            final AccountTransfer transfer = account.toDataTransferObject();
            transfer.setPassword(newPassword);

            final String url = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
            String projectName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
            if (StringUtils.isEmpty(projectName)) {
                projectName = "ICE";
            }
            final String subject = projectName + " Password Reset";
            String name = account.getFirstName();
            if (StringUtils.isBlank(name)) {
                name = account.getLastName();
            }

            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "EEE, MMM d, yyyy 'at' HH:mm aaa, z");

            final StringBuilder builder = new StringBuilder();
            builder.append("Dear ")
                    .append(name)
                    .append(",\n\n")
                    .append("The password for your ")
                    .append(projectName)
                    .append(" account (")
                    .append(targetEmail)
                    .append(") was reset on ")
                    .append(dateFormat.format(new Date()))
                    .append(".\nYour new temporary password is\n\n")
                    .append(newPassword)
                    .append("\n\n")
                    .append("Please go to the following link to login and change your password.\n\n")
                    .append("https://").append(url).append("/profile/").append(account.getId())
                    .append("\n\nThank you.");

            Emailer.send(account.getEmail(), subject, builder.toString());
        } catch (final Exception ex) {
            Logger.error(ex);
            return false;
        }
        return true;
    }

    /**
     * Updates account password associated the account email. It encrypts it before associating it
     * with the account
     *
     * @param userId
     * @param transfer
     * @return updated account object
     */
    public AccountTransfer updatePassword(final String userId, final AccountTransfer transfer) {
        final Account userAccount = getByEmail(transfer.getEmail());
        if (userAccount == null) {
            throw new IllegalArgumentException("Could not retrieve account by id "
                    + transfer.getEmail());
        }

        if (!isAdministrator(userId) && !userAccount.getEmail().equalsIgnoreCase(userId)) {
            return null;
        }

        userAccount.setPassword(AccountUtils.encryptNewUserPassword(transfer.getPassword(),
                userAccount.getSalt()));
        return dao.update(userAccount).toDataTransferObject();
    }

    /**
     * validates the account dto to ensure that the fields required (especially by the database) are
     * present
     *
     * @param accountTransfer
     *            account dto for validation
     */
    private boolean validateRequiredAccountFields(final AccountTransfer accountTransfer) {
        if (accountTransfer.getFirstName() == null
                || accountTransfer.getFirstName().trim().isEmpty()) {
            return false;
        }

        if (accountTransfer.getLastName() == null || accountTransfer.getLastName().trim().isEmpty()) {
            return false;
        }

        if (accountTransfer.getEmail() == null || accountTransfer.getEmail().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Creates a new account using the parameters passed. A random password is initially generated ,
     * encrypted and assigned to the account
     *
     * @param info
     *            contains information needed to create account
     * @param sendEmail
     *            whether to send account information (including password by email)
     * @return generated password
     */
    public AccountTransfer createNewAccount(final AccountTransfer info, final boolean sendEmail) {
        // validate fields required by the database
        validateRequiredAccountFields(info);

        final String email = info.getEmail().trim();
        if (getByEmail(email) != null) {
            Logger.error("Account with id \"" + email + "\" already exists");
            return null;
        }

        // generate salt and encrypt password before storing
        final String salt = Utils.generateSaltForUserAccount();
        final String newPassword = Utils.generateUUID().substring(24);
        final String encryptedPassword = AccountUtils.encryptNewUserPassword(newPassword, salt);

        final Account account = AccountUtils.fromDTO(info);
        account.setPassword(encryptedPassword);
        account.setSalt(salt);
        account.setCreationTime(Calendar.getInstance().getTime());
        save(account);

        if (!sendEmail) {
            info.setPassword(newPassword);
            return info;
        }

        final String subject = "Account created successfully";
        final StringBuilder stringBuilder = new StringBuilder();

        String name = account.getFirstName();
        if (StringUtils.isBlank(name)) {
            name = account.getLastName();
            if (StringUtils.isBlank(name)) {
                name = email;
            }
        }

        stringBuilder.append("Dear ").append(name).append(", ")
                .append("\n\nThank you for creating a ")
                .append(Utils.getConfigValue(ConfigurationKey.PROJECT_NAME))
                .append(" account. \nBy accessing ")
                .append("this site with the password provided at the bottom ")
                .append("you agree to the following terms:\n\n");

        final String terms = "Biological Parts IP Disclaimer: \n\n"
                + "The JBEI Registry of Biological Parts Software is licensed under a standard BSD\n"
                + "license. Permission or license to use the biological parts registered in\n"
                + "the JBEI Registry of Biological Parts is not included in the BSD license\n"
                + "to use the JBEI Registry Software. Berkeley Lab and JBEI make no representation\n"
                + "that the use of the biological parts registered in the JBEI Registry of\n"
                + "Biological Parts will not infringe any patent or other proprietary right.";

        stringBuilder.append(terms);
        stringBuilder.append("\n\nYour new password is: ").append(newPassword)
                .append("\nYour login id is: ").append(info.getEmail()).append("\n\n");

        final String server = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        stringBuilder
                .append("Please remember to change your password by going to your profile page at \n\n")
                .append("https://").append(server).append("/profile/").append(account.getId())
                .append("\n\nThank you.");

        Emailer.send(info.getEmail(), subject, stringBuilder.toString());
        info.setPassword(newPassword);
        return info;
    }

    /**
     * @return new admin account
     */
    public Account createAdminAccount() {
        Account adminAccount = getByEmail(ADMIN_ACCOUNT_EMAIL);
        if (adminAccount != null) {
            return adminAccount;
        }

        adminAccount = new Account();
        adminAccount.setEmail(ADMIN_ACCOUNT_EMAIL);
        adminAccount.setLastName("Administrator");
        adminAccount.setFirstName("");
        adminAccount.setInitials("");
        adminAccount.setInstitution("");
        adminAccount.setSalt(Utils.generateSaltForUserAccount());
        adminAccount.setPassword(AccountUtils.encryptNewUserPassword(ADMIN_ACCOUNT_PASSWORD,
                adminAccount.getSalt()));
        adminAccount.setDescription("Administrator Account");

        adminAccount.setIp("");
        final Date currentTime = Calendar.getInstance().getTime();
        adminAccount.setCreationTime(currentTime);
        adminAccount.setModificationTime(currentTime);
        adminAccount.setLastLoginTime(currentTime);
        adminAccount.setType(AccountType.ADMIN);
        return save(adminAccount);
    }

    /**
     * Retrieve {@link Account} by user id.
     *
     * @param email
     *            unique identifier for account, typically email
     * @return {@link Account}
     */
    public Account getByEmail(final String email) {
        return dao.getByEmail(email);
    }

    /**
     * @param email
     *            an account identifier (usually email)
     * @return database identifier of account matching account identifier (email)
     * @throws IllegalArgumentException
     *             for an invalid account identifier
     */
    public long getAccountId(final String email) {
        final Account account = dao.getByEmail(email);
        if (account == null) {
            throw new IllegalArgumentException("No account found with email " + email);
        }
        return account.getId();
    }

    /**
     * @param sessionKey
     * @return Account object matching a session key, or {@code null}
     */
    public Account getAccountBySessionKey(final String sessionKey) {
        final String userId = SessionHandler.getUserIdBySession(sessionKey);
        if (userId == null) {
            Logger.warn("Could not retrieve user id for session " + sessionKey);
            return null;
        }
        return dao.getByEmail(userId);
    }

    /**
     * Store {@link Account} into the database.
     *
     * @param account
     * @return {@link Account} that has been saved.
     */
    public Account save(final Account account) {
        account.setModificationTime(Calendar.getInstance().getTime());
        if (account.getSalt() == null || account.getSalt().isEmpty()) {
            account.setSalt(Utils.generateSaltForUserAccount());
        }
        return dao.create(account);
    }

    /**
     * @param account
     * @return {@code true} if an administrator account
     */
    public boolean isAdministrator(final Account account) {
        return isAdministrator(account.getEmail());
    }

    /**
     * Check in the database if an account is a moderator.
     *
     * @param userId
     *            unique account identifier for user
     * @return True, if the account is a moderator.
     */
    public boolean isAdministrator(final String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }

        final Account account = getByEmail(userId);
        return account != null && account.getType() == AccountType.ADMIN;
    }

    /**
     * Authenticate a user in the database.
     * <p/>
     * Using the {@link org.jbei.ice.lib.account.authentication.IAuthentication} specified in the
     * settings file, authenticate the user, and return the sessionData
     *
     * @param login
     * @param password
     * @param ip
     *            IP Address of the user.
     * @return the account identifier (email) on a successful login, otherwise {@code null}
     */
    public String authenticate(final String login, final String password, final String ip) {
        final IAuthentication authentication = getAuthenticationBackend();
        String email;

        try {
            email = authentication.authenticates(login.trim(), password);
            if (email == null) {
                loginFailureCooldown();
                return null;
            }
        } catch (final AuthenticationException e2) {
            loginFailureCooldown();
            return null;
        }

        final Account account = dao.getByEmail(email);
        if (account != null) {
            AccountPreferences accountPreferences = accountPreferencesDAO
                    .getAccountPreferences(account);

            if (accountPreferences == null) {
                accountPreferences = new AccountPreferences();
                accountPreferences.setAccount(account);
                saveAccountPreferences(accountPreferences);
            }

            account.setIp(ip);
            account.setLastLoginTime(Calendar.getInstance().getTime());
            save(account);
            SessionHandler.createNewSessionForUser(account.getEmail());
            return email;
        }

        return null;
    }

    private void loginFailureCooldown() {
        // sets 2 seconds delay on login to prevent login/password brute force hacking
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException ie) {
            Logger.warn(ie.getMessage());
        }
    }

    /**
     * Attempts to load the ICE Authentication Backend from database configuration.
     *
     * @return an IAuthentication backend
     */
    public IAuthentication getAuthenticationBackend() {
        final String clazzName = Utils.getConfigValue(ConfigurationKey.AUTHENTICATION_BACKEND);
        try {
            final Class<?> clazz = Class.forName(clazzName);
            if (IAuthentication.class.isAssignableFrom(clazz)) {
                return (IAuthentication) clazz.newInstance();
            }
        } catch (final ClassNotFoundException e) {
            log.error("Failed to load class " + clazzName);
        } catch (final InstantiationException e) {
            log.error("Failed to instantiate class " + clazzName);
        } catch (final IllegalAccessException e) {
            log.error("Inaccessible class " + clazzName);
        }
        return new LocalAuthentication();
    }

    /**
     * Authenticate a user in the database.
     * <p/>
     * Using the {@link org.jbei.ice.lib.account.authentication.IAuthentication} specified in the
     * settings file, authenticate the user, and return the sessionData
     *
     * @param transfer
     *            user information containing the email and password to be used for authentication
     *            If the sessionId field is set, it may or may not be used as the user's session id
     * @return {@link AccountTransfer}
     */
    public AccountTransfer authenticate(final AccountTransfer transfer) {
        final String email = authenticate(transfer.getEmail(), transfer.getPassword(), "");

        if (email == null) {
            return null;
        }

        final Account account = dao.getByEmail(email);
        if (account == null) {
            return null;
        }

        final AccountTransfer info = account.toDataTransferObject();
        info.setLastLogin(account.getLastLoginTime().getTime());
        info.setId(account.getId());
        final boolean isAdmin = isAdministrator(email);
        info.setAdmin(isAdmin);
        info.setSessionId(SessionHandler.createSessionForUser(email, transfer.getSessionId()));
        return info;
    }

    /**
     * See if the given sessionKey is still authenticated with the system.
     *
     * @param sessionKey
     *            unique session identifier
     * @return True if sessionKey is still authenticated (active) to the system.
     */
    public static boolean isAuthenticated(final String sessionKey) {
        return SessionHandler.isValidSession(sessionKey);
    }

    /**
     * De-authenticate the given sessionKey. The user is logged out from the system.
     *
     * @param sessionKey
     *            unique session identifier
     */
    public void invalidate(final String sessionKey) {
        SessionHandler.invalidateSession(sessionKey);
    }

    /**
     * Save {@link AccountPreferences} to the database.
     *
     * @param accountPreferences
     */
    public void saveAccountPreferences(final AccountPreferences accountPreferences) {
        accountPreferencesDAO.create(accountPreferences);
    }

    /**
     * @param userId
     * @param query
     * @param limit
     * @return accounts matching the query
     */
    public List<AccountTransfer> getMatchingAccounts(final String userId, final String query,
            final int limit) {
        // TODO account object is never used?
        getByEmail(userId);
        final Set<Account> matches = dao.getMatchingAccounts(query, limit);
        final ArrayList<AccountTransfer> result = new ArrayList<>();
        for (final Account match : matches) {
            final AccountTransfer info = new AccountTransfer();
            info.setEmail(match.getEmail());
            info.setFirstName(match.getFirstName());
            info.setLastName(match.getLastName());
            result.add(info);
        }
        return result;
    }

    /**
     * @param userId
     * @param start
     * @param limit
     * @param sort
     * @param asc
     * @return window of results to all accounts
     */
    public AccountResults retrieveAccounts(final String userId, final int start, final int limit,
            final String sort, final boolean asc) {
        if (!isAdministrator(userId)) {
            Logger.warn(userId
                    + " attempting to retrieve all user accounts without admin privileges");
            return null;
        }

        final AccountResults results = new AccountResults();
        final EntryController entryController = new EntryController();
        final List<Account> accounts = dao.getAccounts(start, limit, sort, asc);

        final List<AccountTransfer> infos = new ArrayList<>();
        for (final Account userAccount : accounts) {
            final AccountTransfer info = userAccount.toDataTransferObject();
            final long count = entryController.getNumberOfOwnerEntries(userId,
                    userAccount.getEmail());
            info.setUserEntryCount(count);
            info.setAdmin(isAdministrator(userAccount.getEmail()));
            infos.add(info);
        }

        results.getResults().addAll(infos);
        final long count = dao.getAccountsCount();
        results.setResultCount(count);
        return results;
    }

    /**
     * @param id
     * @param email
     * @throws ControllerException
     */
    public void removeMemberFromGroup(final long id, final String email) throws ControllerException {
        final Account account = getByEmail(email);
        if (account == null) {
            throw new ControllerException("Could not find account " + email);
        }

        final Group group = DAOFactory.getGroupDAO().get(id);
        if (group == null) {
            throw new ControllerException("Could not find group " + id);
        }
        account.getGroups().remove(group);
        try {
            dao.update(account);
        } catch (final DAOException e) {
            throw new ControllerException(e);
        }
    }
}
