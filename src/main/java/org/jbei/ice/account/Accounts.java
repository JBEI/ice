package org.jbei.ice.account;

import org.hibernate.service.spi.ServiceException;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.dto.AccountResults;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.utils.PasswordUtils;
import org.jbei.ice.utils.UtilityException;

import java.util.*;

/**
 * ICE user accounts
 *
 * @author Hector Plahar
 */
public class Accounts {

    private final AccountDAO accountDAO;

    public Accounts() {
        this.accountDAO = DAOFactory.getAccountDAO();
    }

    protected boolean isAdministrator(AccountModel account) {
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
        AccountModel account = accountDAO.getByEmail(userId);
        if (!isAdministrator(account))
            throw new PermissionException(userId + " does not have the privilege to access all accounts");

        AccountResults results = new AccountResults();
        List<AccountModel> accounts = accountDAO.getAccounts(offset, limit, sort, asc, filter);

        for (AccountModel userAccount : accounts) {
            Account info = userAccount.toDataTransferObject();
            long entryCount = getNumberOfOwnerEntries(account, userAccount.getEmail());
            info.setUserEntryCount(entryCount);
            info.setAdmin(isAdministrator(userAccount));
            results.getResults().add(info);
        }

        long count = accountDAO.getAccountsCount(filter);
        results.setResultCount(count);
        return results;
    }

    public List<Account> filterAccount(String userId, String token, int limit) {
        List<AccountModel> results = accountDAO.getMatchingAccounts(token, limit);
        List<Account> accounts = new ArrayList<>();
        for (AccountModel match : results) {
            Account info = new Account();
            info.setId(match.getId());
            info.setEmail(match.getEmail());
            info.setFirstName(match.getFirstName());
            info.setLastName(match.getLastName());
            accounts.add(info);
        }
        return accounts;
    }

    public Account getAccount(String requester, String userId) {
        // todo : who can request the account information?  if in a public group together then can return
        AccountModel account;
        if (userId.matches("\\d+(\\.\\d+)?")) {
            account = this.accountDAO.get(Long.decode(userId));
        } else {
            account = this.accountDAO.getByEmail(userId);
        }
        if (account == null)
            return null;
        return account.toDataTransferObject();
    }

    public void createDefaultAdminAccount() throws ServiceException {
        AccountModel accountModel = accountDAO.getByEmail(AccountController.ADMIN_ACCOUNT_EMAIL);
        String newPassword = PasswordUtils.generateRandomToken(48);

        if (accountModel != null) {
            Logger.info("Resetting Administrator account password");
            try {
                accountModel.setPassword(PasswordUtils.encryptPassword(newPassword, accountModel.getSalt()));
            } catch (UtilityException e) {
                throw new ServiceException("Exception encrypting password", e);
            }
            accountModel.setModificationTime(accountModel.getCreationTime());
            accountDAO.update(accountModel);

        } else {
            Logger.info("Creating Administrator Account");
            accountModel = new AccountModel();
            accountModel.setCreationTime(new Date(System.currentTimeMillis()));
            accountModel.setModificationTime(accountModel.getCreationTime());
            accountModel.setFirstName("Administrator");
            accountModel.setLastName("");
            accountModel.setInitials("");
            accountModel.setInstitution("");
            accountModel.setIp("");
            accountModel.setType(AccountType.ADMIN);
            accountModel.setDescription("Administrator Account");
            accountModel.setEmail(AccountController.ADMIN_ACCOUNT_EMAIL.toLowerCase());
            accountModel.setSalt(PasswordUtils.generateSalt());

            try {
                accountModel.setPassword(PasswordUtils.encryptPassword(newPassword, accountModel.getSalt()));
            } catch (UtilityException ue) {
                throw new ServiceException("Exception encrypting password", ue);
            }

            accountDAO.create(accountModel);
        }

        // add log information for admin password
        Logger.info("NEW ADMIN PASSWORD");
        Logger.info("************************");
        Logger.info(newPassword);
        Logger.info("************************");
    }

    protected long getNumberOfOwnerEntries(AccountModel account, String ownerEmail) {
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        return DAOFactory.getEntryDAO().ownerEntryCount(account, ownerEmail, accountGroups);
    }
}
