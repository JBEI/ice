package org.jbei.ice.lib.account;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.dto.AccountResults;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<Account> accounts = accountDAO.getAccounts(offset, limit, sort, asc, filter);

        for (Account userAccount : accounts) {
            AccountTransfer info = userAccount.toDataTransferObject();
            long entryCount = getNumberOfOwnerEntries(account, userAccount.getEmail());
            info.setUserEntryCount(entryCount);
            info.setAdmin(isAdministrator(userAccount));
            results.getResults().add(info);
        }

        long count = accountDAO.getAccountsCount(filter);
        results.setResultCount(count);
        return results;
    }

    public List<AccountTransfer> filterAccount(String userId, String token, int limit) {
        Set<Account> results = accountDAO.getMatchingAccounts(token, limit);
        List<AccountTransfer> accountTransfers = new ArrayList<>();
        for (Account match : results) {
            AccountTransfer info = new AccountTransfer();
            info.setId(match.getId());
            info.setEmail(match.getEmail());
            info.setFirstName(match.getFirstName());
            info.setLastName(match.getLastName());
            accountTransfers.add(info);
        }
        return accountTransfers;
    }

    public AccountTransfer getAccount(String requester, String userId) {
        // todo : who can request the account information?  if in a public group together then can return
        Account account;
        if (userId.matches("\\d+(\\.\\d+)?")) {
            account = this.accountDAO.get(Long.decode(userId));
        } else {
            account = this.accountDAO.getByEmail(userId);
        }
        if (account == null)
            return null;
        return account.toDataTransferObject();
    }

    protected long getNumberOfOwnerEntries(Account account, String ownerEmail) {
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        return DAOFactory.getEntryDAO().ownerEntryCount(account, ownerEmail, accountGroups);
    }
}
