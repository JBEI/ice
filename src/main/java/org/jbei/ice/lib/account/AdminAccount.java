package org.jbei.ice.lib.account;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.Account;

import java.util.Calendar;
import java.util.Date;

/**
 * Represents the default administrator account (a.k.a. the super user)
 */
public class AdminAccount {

    private static final String ADMIN_ACCOUNT_EMAIL = "Administrator";
    private final AccountDAO dao;

    public AdminAccount() {
        dao = DAOFactory.getAccountDAO();
    }

    public void create() {
        Account adminAccount = dao.getByEmail(ADMIN_ACCOUNT_EMAIL);
        if (adminAccount != null)
            return;

        adminAccount = new Account();
        adminAccount.setEmail(ADMIN_ACCOUNT_EMAIL);
        adminAccount.setFirstName("Super");
        adminAccount.setLastName("User");
        adminAccount.setInitials("");
        adminAccount.setInstitution("");
        adminAccount.setIp("");

        TokenHash hash = new TokenHash();
        String salt = hash.generateSalt();
        adminAccount.setSalt(salt);

        String password = setPassword(adminAccount);
        adminAccount.setDescription("Super user account");

        final Date currentTime = Calendar.getInstance().getTime();
        adminAccount.setCreationTime(currentTime);
        adminAccount.setModificationTime(currentTime);
        adminAccount.setLastLoginTime(currentTime);
        adminAccount.setType(AccountType.ADMIN);
        dao.create(adminAccount);

        Logger.info("Administrator authentication token: " + password);
    }

    public void resetPassword() {
        Account adminAccount = dao.getByEmail(ADMIN_ACCOUNT_EMAIL);
        if (adminAccount == null) {
            create();
            return;
        }

        final String password = setPassword(adminAccount);
        dao.update(adminAccount);
        Logger.info("Administrator authentication token: " + password);
    }

    private String setPassword(Account adminAccount) {
        TokenHash hash = new TokenHash();
        String password = hash.generateRandomToken(18);
        // remove padding
        password = password.replaceAll("=", "");
        adminAccount.setPassword(AccountUtils.encryptNewUserPassword(password, adminAccount.getSalt()));
        return password;
    }
}
