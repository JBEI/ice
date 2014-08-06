package org.jbei.ice.lib.dao.hibernate;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.AccountPreferences;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * DAO to manipulate {@link AccountPreferences} objects in the database.
 *
 * @author Hector Plahar
 */
public class AccountPreferencesDAO extends HibernateRepository<AccountPreferences> {

    /**
     * Retrieve the {@link AccountPreferences} of the given {@link Account}.
     *
     * @param account account whose preferences are being retrieved
     * @return retrieved AccountPreferences
     * @throws DAOException
     */
    public AccountPreferences getAccountPreferences(Account account) throws DAOException {
        AccountPreferences accountPreferences = null;
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + AccountPreferences.class.getName()
                                                      + " where account = :account");
            query.setParameter("account", account);
            accountPreferences = (AccountPreferences) query.uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to get AccountPreferences by Account: " + account.getFullName(), e);
        }

        return accountPreferences;
    }

    @Override
    public AccountPreferences get(long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
