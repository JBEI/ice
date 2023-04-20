package org.jbei.ice;

import org.jbei.ice.account.Account;
import org.jbei.ice.account.AccountType;
import org.jbei.ice.account.Accounts;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.junit.Assert;

/**
 * Helper class for creating accounts to be used in tests
 * Needs to wrapped in a transaction
 *
 * @author Hector Plahar
 */
public class AccountCreator {

    public static AccountModel createTestAccount(String testName, boolean admin) throws Exception {
        String email = testName + "@TESTER";
        AccountDAO dao = DAOFactory.getAccountDAO();
        AccountModel accountModel = dao.getByEmail(email);
        if (accountModel != null)
            throw new Exception("duplicate account");

        Account account = new Account();
        account.setFirstName("TEST_FNAME");
        account.setLastName("TEST");
        account.setEmail(email);
        account = new Accounts().create(account, false);

        Assert.assertNotNull(account.getPassword());
        accountModel = dao.getByEmail(email);
        Assert.assertNotNull(account);

        if (admin) {
            accountModel.setType(AccountType.ADMIN);
            dao.update(accountModel);
        }
        return accountModel;
    }
}
