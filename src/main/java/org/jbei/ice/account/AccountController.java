package org.jbei.ice.account;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.account.authentication.*;
import org.jbei.ice.account.authentication.ldap.LdapAuthentication;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.group.GroupType;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.utils.Utils;

import javax.naming.ConfigurationException;
import java.util.Calendar;
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
        final boolean isAdmin = new AccountAuthorization().isAdmin(email);
        info.setAdmin(isAdmin);
        info.setSessionId(UserSessions.createSessionForUser(email, transfer.getSessionId()));
        return info;
    }

    /**
     * @param id
     * @param email
     */
    public void removeMemberFromGroup(final long id, final String email) {
        final AccountModel account = this.dao.getByEmail(email);
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
