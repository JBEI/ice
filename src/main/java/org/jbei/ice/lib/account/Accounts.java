package org.jbei.ice.lib.account;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.dto.AccountResults;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.Account;

import java.util.List;

/**
 * ICE user accounts
 *
 * @author Hector Plahar
 */
public class Accounts {

    private AccountDAO accountDAO;

    public Accounts() {
        this.accountDAO = DAOFactory.getAccountDAO();
    }

    protected boolean isAdministrator(Account account) {
//        Account account = accountDAO.getByEmail(userId);
        return account != null && account.getType() == AccountType.ADMIN;
    }

    /**
     * Retrieves the user records available.
     *
     * @param userId requesting userid
     * @param offset start
     * @param limit  account count upper limit
     * @param asc    sort order
     * @param sort   account sort type
     * @return wrapper around list of retrieved requested records and number available for retrieval
     */
    public AccountResults getAvailableAccounts(String userId, int offset, int limit, boolean asc, String sort,
                                               String filter) {
        Account account = accountDAO.getByEmail(userId);
        if (!isAdministrator(account))
            throw new PermissionException(userId + " does not have the privilege to access all accounts");

        AccountResults results = new AccountResults();
        EntryController entryController = new EntryController();
        List<Account> accounts = accountDAO.getAccounts(offset, limit, sort, asc, filter);

        for (Account userAccount : accounts) {
            AccountTransfer info = userAccount.toDataTransferObject();
            long entryCount = entryController.getNumberOfOwnerEntries(userId, userAccount.getEmail());
            info.setUserEntryCount(entryCount);
            info.setAdmin(isAdministrator(userAccount));
            results.getResults().add(info);
        }

        long count = accountDAO.getAccountsCount(filter);
        results.setResultCount(count);
        return results;
    }
}
