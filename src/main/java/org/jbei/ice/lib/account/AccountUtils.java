package org.jbei.ice.lib.account;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.AccountInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for account management
 *
 * @author Hector Plahar
 */
class AccountUtils {

    /**
     * Return the encrypted version of the given password, using the salt from the settings file.
     *
     * @param password non-empty string
     * @return 40 character encrypted string.
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Cannot encrypt null or empty password");
        return Utils.encryptSHA(JbeirSettings.getSetting("SECRET_KEY") + password);
    }

    public static AccountInfo accountToInfo(Account account) {
        if (account == null)
            return null;

        AccountInfo info = new AccountInfo();
        info.setEmail(account.getEmail());
        info.setFirstName(account.getFirstName());
        info.setLastName(account.getLastName());
        info.setInstitution(account.getInstitution());
        info.setDescription(account.getDescription());
        info.setInitials(account.getInitials());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d yyyy");
        Date memberSinceDate = account.getCreationTime();
        if (memberSinceDate != null)
            info.setSince(dateFormat.format(memberSinceDate));

        return info;
    }
}
