package org.jbei.ice.lib.account;

import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

/**
 * Utility class for account management
 * 
 * @author Hector Plahar
 */
class AccountUtils {

    /**
     * Return the encrypted version of the given password, using the salt from the settings file.
     * 
     * @param password
     *            non-empty string
     * @return 40 character encrypted string.
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Cannot encrypt null or empty password");
        return Utils.encryptSHA(JbeirSettings.getSetting("SECRET_KEY") + password);
    }
}
