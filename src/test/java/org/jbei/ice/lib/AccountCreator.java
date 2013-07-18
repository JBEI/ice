package org.jbei.ice.lib;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.shared.dto.user.User;

import junit.framework.Assert;

/**
 * Helper class for creating accounts to be used in tests
 * Needs to wrapped in a transaction
 *
 * @author Hector Plahar
 */
public class AccountCreator {

    public static Account createTestAccount(String testName, boolean admin) throws Exception {
        String email = testName + "@TESTER";
        AccountController accountController = new AccountController();
        Account account = accountController.getByEmail(email);
        if (account != null)
            throw new Exception("duplicate account");

        User user = new User();
        user.setFirstName("TEST_FNAME");
        user.setLastName("TEST");
        user.setEmail(email);
        String pass = accountController.createNewAccount(user, false);

        Assert.assertNotNull(pass);
        account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        if (admin) {
            account.setType(AccountType.ADMIN);
            accountController.save(account);
        }
        return account;
    }
}
