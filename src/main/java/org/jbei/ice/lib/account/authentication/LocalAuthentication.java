package org.jbei.ice.lib.account.authentication;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountUtils;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;

/**
 * Backend for authentication using the database. This is the default backend.
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

        Account account;
        AccountController controller = new AccountController();

        try {
            account = controller.getByEmail(userId);
            if (account == null || !isValidPassword(account, password))
                return null;
            return account.getEmail();
        } catch (ControllerException e) {
            throw new AuthenticationException("Exception validating credentials", e);
        }
    }

    /**
     * Check if the given password is valid for the account.
     *
     * @param account
     * @param password
     * @return True if correct password.
     * @throws ControllerException
     */
    protected boolean isValidPassword(Account account, String password) throws ControllerException {
        if (account == null) {
            throw new ControllerException("Failed to verify password for null Account!");
        }
        boolean valid = account.getPassword().equals(AccountUtils.encryptNewUserPassword(password, account.getSalt()));
        if (valid)
            return valid;
        String encrypted = AccountUtils.encryptPassword(password, account.getSalt());
        valid = account.getPassword().equals(encrypted);
        if (valid) {
            // update
            account.setPassword(encrypted);
            try {
                DAOFactory.getAccountDAO().update(account);
            } catch (Exception e) {
                Logger.error(e);
            }
            return true;
        }
        return false;
    }
}
