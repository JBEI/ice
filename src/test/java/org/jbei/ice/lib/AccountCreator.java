package org.jbei.ice.lib;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.Account;
import org.junit.Assert;

/**
 * Helper class for creating accounts to be used in tests
 * Needs to wrapped in a transaction
 *
 * @author Hector Plahar
 */
public class AccountCreator {

    public static Account createTestAccount(String testName, boolean admin) throws Exception {
        String email = testName + "@TESTER";
        AccountDAO dao = DAOFactory.getAccountDAO();
        Account account = dao.getByEmail(email);
        if (account != null)
            throw new Exception("duplicate account");

        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setFirstName("TEST_FNAME");
        accountTransfer.setLastName("TEST");
        accountTransfer.setEmail(email);
        accountTransfer = new AccountController().createNewAccount(accountTransfer, false);

        Assert.assertNotNull(accountTransfer.getPassword());
        account = dao.getByEmail(email);
        Assert.assertNotNull(account);

        if (admin) {
            account.setType(AccountType.ADMIN);
            dao.update(account);
        }
        return account;
    }
}
