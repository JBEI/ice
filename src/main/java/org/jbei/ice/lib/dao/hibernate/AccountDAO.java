package org.jbei.ice.lib.dao.hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;

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

    @SuppressWarnings("unchecked")
    public Set<Account> getMatchingAccounts(String token, int limit) {
        Session session = currentSession();
        try {
            token = token.toUpperCase();
            String queryString = "from " + Account.class.getName()
                    + " where (UPPER(firstName) like '%" + token
                    + "%') OR (UPPER(lastName) like '%" + token + "%')";
            Query query = session.createQuery(queryString);
            if (limit > 0)
                query.setMaxResults(limit);

            return new HashSet<Account>(query.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve an {@link Account} by the email field.
     *
     * @param email unique email identifier for account
     * @return Account
     */
    public Account getByEmail(String email) {
        Account account = null;
        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Account.class.getName() + " where LOWER(email) = :email");
            query.setParameter("email", email.toLowerCase());
            Object result = query.uniqueResult();

            if (result != null) {
                account = (Account) result;
            }
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve Account by email: " + email, e);
        }
        return account;
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
