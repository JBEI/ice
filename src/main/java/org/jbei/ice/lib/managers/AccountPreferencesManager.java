package org.jbei.ice.lib.managers;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;

public class AccountPreferencesManager {
    public static AccountPreferences getAccountPreferences(Account account) throws ManagerException {
        if (account == null) {
            throw new ManagerException("Failed to get AccountPreferences for null Account!");
        }

        AccountPreferences accountPreferences = null;

        Session session = DAO.newSession();

        try {
            Query query = session.createQuery("from " + AccountPreferences.class.getName()
                    + " where account = :account");
            query.setParameter("account", account);

            accountPreferences = (AccountPreferences) query.uniqueResult();
        } catch (HibernateException e) {
            throw new ManagerException("Failed to get AccountPreferences by Account: "
                    + account.getFullName(), e);
        } finally {
            session.close();
        }

        return accountPreferences;
    }

    public static AccountPreferences save(AccountPreferences accountPreferences)
            throws ManagerException {
        if (accountPreferences == null) {
            throw new ManagerException("Failed to save null AccountPreferences!");
        }

        AccountPreferences result;

        try {
            result = (AccountPreferences) DAO.save(accountPreferences);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save AccountPreferences!", e);
        }

        return result;
    }
}
