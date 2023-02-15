package org.jbei.ice.account;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.account.authentication.*;
import org.jbei.ice.account.authentication.ldap.LdapAuthentication;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.group.GroupType;
import org.jbei.ice.email.EmailFactory;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.utils.PasswordUtils;
import org.jbei.ice.utils.UtilityException;
import org.jbei.ice.utils.Utils;

import javax.naming.ConfigurationException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ABI to manipulate {@link Account} objects.
 * <p>
 * This class contains methods that wrap {@link AccountDAO} to
 * manipulate {@link Account} objects.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */

public class AccountController {

    public static final String ADMIN_ACCOUNT_EMAIL = "Administrator";
    private final AccountDAO dao;
    private final GroupDAO groupDAO;

    /**
     * Default constructor.
     */
    public AccountController() {
        dao = DAOFactory.getAccountDAO();
        groupDAO = DAOFactory.getGroupDAO();
    }

    /**
     * @param requester
     * @param userId
     * @param transfer
     * @return updated account object
     */
    public Account updateAccount(final String requester, final long userId, final Account transfer) {
        final AccountModel account = dao.get(userId);
        boolean requesterIsAdmin = isAdministrator(requester);
        if (!account.getEmail().equalsIgnoreCase(requester) && !requesterIsAdmin) {
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

        if (transfer.getAccountType() != null) {
            if (transfer.getAccountType() != account.getType() && !requesterIsAdmin)
                throw new PermissionException("Only admins can change account type");

            account.setType(transfer.getAccountType());
        }

        Account result = dao.update(account).toDataTransferObject();
        result.setAdmin(isAdministrator(account.getEmail()));
        return result;
    }

    /**
     * Reset a user's password
     *
     * @param targetEmail email address of user account to be changed
     * @return true if the user account is found with email specified in the parameter and password
     * for it is successfully reset, false otherwise
     */
    public boolean resetPassword(final String targetEmail) {
        AccountModel account = getByEmail(targetEmail);
        if (account == null) {
            return false;
        }

        try {
            final String newPassword = Utils.generateUUID().substring(24);
            final String encryptedNewPassword = PasswordUtils.encryptPassword(newPassword, account.getSalt());
            account.setPassword(encryptedNewPassword);

            account = dao.update(account);
            final Account transfer = account.toDataTransferObject();
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

            EmailFactory.getEmail().send(account.getEmail(), subject, builder.toString());
        } catch (final Exception ex) {
            Logger.error(ex);
            return false;
        }
        return true;
    }

    /**
     * Updates the specified user account's password
     *
     * @param userId   email of user making change. If it is not the same as the email associated with the
     *                 <code>id</code>, then this account must have administrator privileges
     * @param id       unique (db) identifier for user whose password is to be changed.
     * @param transfer wrapper around new password
     * @return updated account object
     * @throws PermissionException if the account associated with <code>userId</code> and <code>id</code> are not
     *                             the same but the <code>userId</code> does not have administrative privileges
     */
    public Account updatePassword(String userId, long id, Account transfer) throws PermissionException {
        AccountModel account = dao.get(id);
        if (account == null) {
            throw new IllegalArgumentException("Could not retrieve account by id " + id);
        }

        if (!isAdministrator(userId) && !account.getEmail().equalsIgnoreCase(userId)) {
            throw new PermissionException("User " + userId + " does not have permission to change "
                + transfer.getEmail() + "'s password");
        }

        try {
            account.setPassword(PasswordUtils.encryptPassword(transfer.getPassword(), account.getSalt()));
        } catch (UtilityException e) {
            Logger.error(e);
            return null;
        }
        return dao.update(account).toDataTransferObject();
    }

    /**
     * Creates a new account using the parameters passed. A random password is initially generated ,
     * encrypted and assigned to the account
     *
     * @param info      contains information needed to create account
     * @param sendEmail whether to send account information (including password by email)
     * @return generated password
     */
    public Account createNewAccount(final Account info, final boolean sendEmail) throws UtilityException {
        if (StringUtils.isEmpty(info.getLastName()) || StringUtils.isEmpty(info.getEmail()))
            throw new IllegalArgumentException("Cannot create account without email or lastname");

        final String email = info.getEmail().trim();
        if (getByEmail(email) != null) {
            Logger.error("Account with id \"" + email + "\" already exists");
            return null;
        }

        if (email.endsWith("@qq.com")) {
            Logger.error("Rejecting account registration with email: " + email);
            return null;
        }

        // generate salt and encrypt password before storing
        final String salt = Utils.generateSaltForUserAccount();
        final String newPassword = Utils.generateUUID().substring(24);
        final String encryptedPassword = PasswordUtils.encryptPassword(newPassword, salt);

        AccountModel account = AccountUtils.fromDTO(info);
        account.setPassword(encryptedPassword);
        account.setSalt(salt);
        account.setCreationTime(Calendar.getInstance().getTime());
        account = save(account);

        info.setId(account.getId());
        info.setPassword(newPassword);

        if (!sendEmail) {
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

        EmailFactory.getEmail().send(info.getEmail(), subject, stringBuilder.toString());
        return info;
    }

    /**
     * Retrieve {@link AccountModel} by user id.
     *
     * @param email unique identifier for account, typically email
     * @return {@link Account}
     */
    public AccountModel getByEmail(final String email) {
        return dao.getByEmail(email);
    }

    /**
     * @param email an account identifier (usually email)
     * @return database identifier of account matching account identifier (email)
     * @throws IllegalArgumentException for an invalid account identifier
     */
    public long getAccountId(final String email) {
        final AccountModel account = dao.getByEmail(email);
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
        final String userId = UserSessions.getUserIdBySession(sessionKey);
        if (userId == null) {
            Logger.warn("Could not retrieve user id for session " + sessionKey);
            return null;
        }
        AccountModel account = dao.getByEmail(userId);
        if (account == null)
            return null;

        Account transfer = account.toDataTransferObject();
        transfer.setSessionId(sessionKey);
        transfer.setAdmin(isAdministrator(userId));
        return transfer;
    }

    /**
     * Store {@link Account} into the database.
     *
     * @param account
     * @return {@link Account} that has been saved.
     */
    public AccountModel save(final AccountModel account) {
        account.setModificationTime(Calendar.getInstance().getTime());
        if (account.getSalt() == null || account.getSalt().isEmpty()) {
            account.setSalt(Utils.generateSaltForUserAccount());
        }
        return dao.create(account);
    }

    /**
     * Check in the database if an account is a moderator.
     *
     * @param userId unique account identifier for user
     * @return True, if the account is a moderator.
     */
    public boolean isAdministrator(final String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }

        final AccountModel account = getByEmail(userId);
        return account != null && account.getType() == AccountType.ADMIN;
    }

    private IAuthentication getAuthentication() {
        try {
            String clazz = Utils.getConfigValue(ConfigurationKey.AUTHENTICATION_METHOD);
            if (StringUtils.isEmpty(clazz))
                return new LocalAuthentication();

            switch (AuthType.valueOf(clazz.toUpperCase())) {
                case LDAP:
                    return new LdapAuthentication();

                case OPEN:
                    return new UserIdAuthentication();

                case DEFAULT:
                default:
                    return new LocalAuthentication();
            }
        } catch (Exception e) {
            Logger.error("Exception loading authentication class: ", e);
            Logger.error("Using default authentication");
            return new LocalAuthentication();
        }
    }

    /**
     * Authenticate a user in the database.
     * <p>
     * Using the {@link IAuthentication} specified in the
     * settings file, authenticate the user, and return the sessionData
     *
     * @param login
     * @param password
     * @param ip       IP Address of the user.
     * @return the account identifier (email) on a successful login, otherwise {@code null}
     */
    protected AccountModel authenticate(final String login, final String password, final String ip) {
        final IAuthentication authentication = getAuthentication();
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

        AccountModel account = dao.getByEmail(email);
        if (account == null)
            return null;

        // add to public groups
        List<Group> groups = groupDAO.getGroupsBy(GroupType.PUBLIC, true);
        try {
            if (groups != null) {
                for (Group group : groups) {
                    account.getGroups().add(group);
                }
                dao.update(account);
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        account.setIp(ip);
        account.setLastLoginTime(Calendar.getInstance().getTime());
        account = save(account);
        UserSessions.createNewSessionForUser(account.getEmail());
        return account;
    }

    /**
     * Sets a 2 seconds delay on login authentication failure to prevent login/password brute force hacking
     */
    private void loginFailureCooldown() {
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException ie) {
            Logger.warn(ie.getMessage());
        }
    }

    /**
     * Authenticate a user in the database.
     * <p>
     * Using the {@link IAuthentication} specified in the
     * settings file, authenticate the user, and return the sessionData
     *
     * @param transfer user information containing the email and password to be used for authentication
     *                 If the sessionId field is set, it may or may not be used as the user's session id
     * @return {@link Account}
     */
    public Account authenticate(final Account transfer) throws ConfigurationException {
        if (StringUtils.isEmpty(transfer.getEmail()) || StringUtils.isEmpty(transfer.getPassword())) {
            Logger.error("Invalid login or password");
            throw new IllegalArgumentException("Invalid login credentials");
        }

        final AccountModel account = authenticate(transfer.getEmail(), transfer.getPassword(), "");
        if (account == null) {
            return null;
        }

        String email = account.getEmail();
        final Account info = account.toDataTransferObject();
        info.setLastLogin(account.getLastLoginTime().getTime());
        info.setId(account.getId());
        final boolean isAdmin = isAdministrator(email);
        info.setAdmin(isAdmin);
        info.setSessionId(UserSessions.createSessionForUser(email, transfer.getSessionId()));
        return info;
    }

    /**
     * De-authenticate the given sessionKey. The user is logged out from the system.
     *
     * @param sessionKey unique session identifier
     */
    public void invalidate(final String sessionKey) {
        UserSessions.invalidateSession(sessionKey);
    }

    /**
     * @param id
     * @param email
     */
    public void removeMemberFromGroup(final long id, final String email) {
        final AccountModel account = getByEmail(email);
        if (account == null) {
            throw new IllegalArgumentException("Could not find account " + email);
        }

        final Group group = groupDAO.get(id);
        if (group == null) {
            throw new IllegalArgumentException("Could not find group " + id);
        }
        account.getGroups().remove(group);
        dao.update(account);
    }
}
