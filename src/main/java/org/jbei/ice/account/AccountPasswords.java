package org.jbei.ice.account;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.email.EmailFactory;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.utils.PasswordUtils;
import org.jbei.ice.utils.UtilityException;
import org.jbei.ice.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Hector Plahar
 */
public class AccountPasswords {

    private final AccountDAO dao;

    public AccountPasswords() {
        this.dao = DAOFactory.getAccountDAO();
    }

    /**
     * Reset a user's password
     *
     * @param targetEmail email address of user account to be changed
     * @return true if the user account is found with email specified in the parameter and password
     * for it is successfully reset, false otherwise
     */
    public boolean reset(final String targetEmail) {
        AccountModel account = this.dao.getByEmail(targetEmail);
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

        if (!account.getEmail().equalsIgnoreCase(userId)) {
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
}
