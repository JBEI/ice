package org.jbei.ice.lib.account.authentication;

import org.jbei.ice.lib.account.AccountUtils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;

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

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
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
    protected boolean isValidPassword(Account account, String password) {
        if (account == null) {
            return false;
        }

        // first check using the stronger encryption scheme
        return account.getPassword().equals(AccountUtils.encryptNewUserPassword(password, account.getSalt()));
    }
}
