package org.jbei.ice.lib.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO to manipulate {@link Account} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class AccountDAO extends HibernateRepository<Account> {

    /**
     * Retrieve {@link Account} by id from the database.
     *
     * @param id unique local identifier for object
     * @return Account
     * @throws DAOException
     */
    public Account get(long id) throws DAOException {
        return super.get(Account.class, id);
    }

    /**
     * Retrieves accounts whose firstName, lastName, or email fields match the specified token up to the specified limit
     *
     * @param token filter for the account fields
     * @param limit maximum number of matching accounts to return; 0 to return all
     * @return list of matching accounts
     */
    @SuppressWarnings("unchecked")
    public Set<Account> getMatchingAccounts(String token, int limit) {
        try {
            Criteria criteria = currentSession().createCriteria(Account.class)
                    .add(Restrictions.disjunction()
                            .add(Restrictions.ilike("firstName", token, MatchMode.ANYWHERE))
                            .add(Restrictions.ilike("lastName", token, MatchMode.ANYWHERE))
                            .add(Restrictions.ilike("email", token, MatchMode.ANYWHERE)));

            if (limit > 0)
                criteria.setMaxResults(limit);
            return new HashSet<>(criteria.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve an {@link Account} by the email field.
     *
     * @param email unique email identifier for account
     * @return Account record referenced by email or null if email is null
     */
    public Account getByEmail(String email) {
        if (email == null)
            return null;

        try {
            return (Account) currentSession().createCriteria(Account.class)
                    .add(Restrictions.eq("email", email).ignoreCase())
                    .uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve Account by email: " + email, e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAccounts(int offset, int limit, String sort, boolean asc) {
        return super.getList(Account.class, offset, limit, sort, asc);
    }

    public long getAccountsCount() {
        try {
            Number itemCount = (Number) currentSession().createCriteria(Account.class.getName())
                    .setProjection(Projections.countDistinct("id")).uniqueResult();
            return itemCount.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
