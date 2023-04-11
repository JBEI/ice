package org.jbei.ice.account;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.service.spi.ServiceException;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.dto.AccountResults;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.email.EmailFactory;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.utils.PasswordUtils;
import org.jbei.ice.utils.UtilityException;
import org.jbei.ice.utils.Utils;

import java.util.*;

/**
 * ICE user accounts
 *
 * @author Hector Plahar
 */
public class Accounts {

    private final AccountDAO dao;

    public Accounts() {
        this.dao = DAOFactory.getAccountDAO();
    }

    protected boolean isAdministrator(String userId) {
        AccountModel account = this.dao.getByEmail(userId);
        return account != null && account.getType() == AccountType.ADMIN;
    }

    /**
     * Retrieve {@link AccountModel} by user id.
     *
     * @param email unique identifier for account, typically email
     * @return {@link Account}
     */
    public AccountModel getByEmail(final String email) {
        return dao.getByEmail(email);
    }

    /**
     * Creates a new account using the parameters passed. A random password is initially generated ,
     * encrypted and assigned to the account
     *
     * @param info      contains information needed to create account
     * @param sendEmail whether to send account information (including password by email)
     * @return generated password
     */
    public Account create(final Account info, final boolean sendEmail) throws UtilityException {
        if (StringUtils.isEmpty(info.getLastName()) || StringUtils.isEmpty(info.getEmail()))
            throw new IllegalArgumentException("User id (typically email) and lastname required for account creation");

        final String email = info.getEmail().trim();
        if (getByEmail(email) != null) {
            Logger.error("Account with id \"" + email + "\" already exists");
            return null;
        }

        if (email.endsWith("@qq.com")) {
            Logger.error("Rejecting account registration with email: " + email);
            return null;
        }

        // generate salt and encrypt password before storing
        final String salt = Utils.generateSaltForUserAccount();
        final String newPassword = Utils.generateUUID().substring(24);
        final String encryptedPassword = PasswordUtils.encryptPassword(newPassword, salt);

        AccountModel account = AccountUtils.fromDTO(info);
        account.setPassword(encryptedPassword);
        account.setSalt(salt);
        account.setCreationTime(Calendar.getInstance().getTime());
        account.setModificationTime(account.getCreationTime());
        account = this.dao.create(account);

        info.setId(account.getId());
        info.setPassword(newPassword);

        if (!sendEmail) {
            return info;
        }

        final String subject = "Account created successfully";
        final StringBuilder stringBuilder = new StringBuilder();

        String name = account.getFirstName();
        if (StringUtils.isBlank(name)) {
            name = account.getLastName();
            if (StringUtils.isBlank(name)) {
                name = email;
            }
        }

        stringBuilder.append("Dear ").append(name).append(", ")
            .append("\n\nThank you for creating a ")
            .append(Utils.getConfigValue(ConfigurationKey.PROJECT_NAME))
            .append(" account. \nBy accessing ")
            .append("this site with the password provided at the bottom ")
            .append("you agree to the following terms:\n\n");

        final String terms = "Biological Parts IP Disclaimer: \n\n"
            + "The JBEI Registry of Biological Parts Software is licensed under a standard BSD\n"
            + "license. Permission or license to use the biological parts registered in\n"
            + "the JBEI Registry of Biological Parts is not included in the BSD license\n"
            + "to use the JBEI Registry Software. Berkeley Lab and JBEI make no representation\n"
            + "that the use of the biological parts registered in the JBEI Registry of\n"
            + "Biological Parts will not infringe any patent or other proprietary right.";

        stringBuilder.append(terms);
        stringBuilder.append("\n\nYour new password is: ").append(newPassword)
            .append("\nYour login id is: ").append(info.getEmail()).append("\n\n");

        final String server = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        stringBuilder
            .append("Please remember to change your password by going to your profile page at \n\n")
            .append("https://").append(server).append("/profile/").append(account.getId())
            .append("\n\nThank you.");

        EmailFactory.getEmail().send(info.getEmail(), subject, stringBuilder.toString());
        return info;
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
    public AccountResults getAvailable(String userId, int offset, int limit, boolean asc, String sort, String filter) {
        AccountModel account = dao.getByEmail(userId);
        if (!isAdministrator(userId))
            throw new PermissionException(userId + " does not have the privilege to access all accounts");

        AccountResults results = new AccountResults();
        List<AccountModel> accounts = dao.getAccounts(offset, limit, sort, asc, filter);

        for (AccountModel userAccount : accounts) {
            Account info = userAccount.toDataTransferObject();
            long entryCount = getNumberOfOwnerEntries(account, userAccount.getEmail());
            info.setUserEntryCount(entryCount);
            info.setAdmin(account.getType() == AccountType.ADMIN);
            results.getResults().add(info);
        }

        long count = dao.getAccountsCount(filter);
        results.setResultCount(count);
        return results;
    }

    public List<Account> filterAccount(String userId, String token, int limit) {
        List<AccountModel> results = dao.getMatchingAccounts(token, limit);
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
            account = this.dao.get(Long.decode(userId));
        } else {
            account = this.dao.getByEmail(userId);
        }
        if (account == null)
            return null;
        return account.toDataTransferObject();
    }

    /**
     * Creates the default administrator account or resets the password if the account already exists
     *
     * @throws ServiceException on exception generating password for admin account
     */
    public void createDefaultAdminAccount() throws ServiceException {
        AccountModel accountModel = dao.getByEmail(AccountController.ADMIN_ACCOUNT_EMAIL);
        String newPassword = PasswordUtils.generateRandomToken(48);

        if (accountModel != null) {
            Logger.info("Resetting Administrator account password");
            try {
                accountModel.setPassword(PasswordUtils.encryptPassword(newPassword, accountModel.getSalt()));
            } catch (UtilityException e) {
                throw new ServiceException("Exception encrypting password", e);
            }
            accountModel.setModificationTime(accountModel.getCreationTime());
            dao.update(accountModel);
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

            dao.create(accountModel);
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

    /**
     * @param requester
     * @param userId
     * @param transfer
     * @return updated account object
     */
    public Account updateAccount(final String requester, final long userId, final Account transfer) {
        final AccountModel account = dao.get(userId);
        boolean requesterIsAdmin = isAdministrator(requester);
        if (!account.getEmail().equalsIgnoreCase(requester) && !requesterIsAdmin) {
            return null;
        }

        // if transfer has password then it is a password change
        if (!StringUtils.isEmpty(transfer.getFirstName())) {
            account.setFirstName(transfer.getFirstName());
        }

        if (!StringUtils.isEmpty(transfer.getLastName())) {
            account.setLastName(transfer.getLastName());
        }

        if (!StringUtils.isEmpty(transfer.getDescription())) {
            account.setDescription(transfer.getDescription());
        }

        if (!StringUtils.isEmpty(transfer.getInstitution())) {
            account.setInstitution(transfer.getInstitution());
        }

        if (transfer.getAccountType() != null) {
            if (transfer.getAccountType() != account.getType() && !requesterIsAdmin)
                throw new PermissionException("Only admins can change account type");

            account.setType(transfer.getAccountType());
        }

        Account result = dao.update(account).toDataTransferObject();
        result.setAdmin(isAdministrator(account.getEmail()));
        return result;
    }
}
