package org.jbei.ice.lib.account;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.shared.dto.user.AccountType;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * DAO to manipulate {@link Account} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
class AccountDAO extends HibernateRepository<Account> {

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

    public Set<Account> getMatchingAccounts(Account account, String token, int limit) throws DAOException {
        Session session = currentSession();
        try {
            token = token.toUpperCase();
            String queryString = "from " + Account.class.getName()
                    + " where (UPPER(firstName) like '%" + token
                    + "%') OR (UPPER(lastName) like '%" + token + "%')";
            Query query = session.createQuery(queryString);
            if (limit > 0)
                query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve an {@link Account} by the email field.
     *
     * @param email unique email identifier for account
     * @return Account
     * @throws DAOException
     */
    public Account getByEmail(String email) throws DAOException {
        if (email == null)
            return null;
        Account account = null;
        Session session = currentSession();
        try {
            Query query = session.createQuery(
                    "from " + Account.class.getName() + " account where lower(account.email) = :email");
            query.setParameter("email", email.toLowerCase());
            Object result = query.uniqueResult();

            if (result != null) {
                account = (Account) result;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve Account by email: " + email);
        }
        return account;
    }

    /**
     * Retrieve the {@link Account} by the authorization token.
     *
     * @param authToken token
     * @return Account.
     * @throws DAOException
     */
    public Account getAccountByAuthToken(String authToken) throws DAOException {
        Account account = null;
        String queryString = "from " + SessionData.class.getName()
                + " sessionData where sessionData.sessionKey = :sessionKey";
        Session session = currentSession();
        try {
            Query query = session.createQuery(queryString);
            query.setString("sessionKey", authToken);
            SessionData sessionData = (SessionData) query.uniqueResult();
            if (sessionData != null) {
                account = sessionData.getAccount();
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to get Account by token: " + authToken);
        }
        return account;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Account> retrieveAccounts(int start, int limit) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Account.class).setFirstResult(start).setMaxResults(limit);
        criteria.add(Restrictions.ne("type", AccountType.SYSTEM));
        criteria.addOrder(Order.asc("email"));
        List list = criteria.list();
        return new LinkedList<Account>(list);
    }

    /**
     * @return number of non system accounts
     * @throws DAOException
     */
    public int retrieveAllNonSystemAccountCount() throws DAOException {
        Number number = (Number) currentSession().createCriteria(Account.class)
                .add(Restrictions.ne("type", AccountType.SYSTEM))
                .setProjection(Projections.rowCount()).uniqueResult();
        return number.intValue();
    }
}
