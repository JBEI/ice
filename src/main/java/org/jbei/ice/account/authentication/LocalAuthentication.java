package org.jbei.ice.account.authentication;

import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.utils.PasswordUtils;
import org.jbei.ice.utils.UtilityException;

/**
 * Default ICE authentication scheme
 *
 * @author Hector Plahar
 */
public class LocalAuthentication implements IAuthentication {

    public LocalAuthentication() {
    }

    @Override
    public String authenticates(String userId, String password) throws AuthenticationException {
        if (userId == null || password == null)
            throw new AuthenticationException("Invalid username and password");

        AccountModel account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account == null || !isValidPassword(account, password))
            return null;
        return account.getEmail();
    }

    /**
     * Check if the given password is valid for the account. There are multiple checks for backward compatibility
     * reasons
     *
     * @param account  user account whose password is being checked
     * @param password user entered password being checked for validation
     * @return True if entered password matches one of encrypted schemes, false otherwise.
     */
    protected boolean isValidPassword(AccountModel account, String password) {
        if (account == null) {
            return false;
        }

        // first check using the stronger encryption scheme
        try {
            return account.getPassword().equals(PasswordUtils.encryptPassword(password, account.getSalt()));
        } catch (UtilityException e) {
            Logger.error(e);
            return false;
        }
    }
}
