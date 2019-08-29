package org.jbei.ice.lib.account.authentication;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.storage.model.Account;

/**
 * Authentication implementation that only validates the user id
 * with any non empty string as password
 *
 * @author Hector Plahar
 */
public class UserIdAuthentication implements IAuthentication {

    @Override
    public AccountTransfer authenticates(String userId, String password, String ip) {
        AccountController retriever = new AccountController();
        Account account = retriever.getByEmail(userId);
        if (account == null)
            return null;
        return account.toDataTransferObject();
    }
}
