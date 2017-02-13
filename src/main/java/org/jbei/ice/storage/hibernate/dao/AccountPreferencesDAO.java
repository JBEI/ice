package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.AccountPreferences;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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
    public AccountPreferences getAccountPreferences(Account account) {
        try {
            CriteriaQuery<AccountPreferences> query = getBuilder().createQuery(AccountPreferences.class);
            Root<AccountPreferences> from = query.from(AccountPreferences.class);
            query.where(getBuilder().equal(from.get("account"), account));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to get AccountPreferences by Account: " + account.getFullName(), e);
        }
    }

    @Override
    public AccountPreferences get(long id) {
        return super.get(AccountPreferences.class, id);
    }
}
