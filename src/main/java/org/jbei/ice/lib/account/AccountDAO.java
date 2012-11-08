package org.jbei.ice.lib.account;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.models.SessionData;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

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

    public ArrayList<Account> getAllAccounts() throws DAOException {
        ArrayList<Account> result = new ArrayList<Account>(super.retrieveAll(Account.class));
        return result;
    }

    public Set<Account> getMatchingAccounts(String token, int limit) throws DAOException {
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
        } finally {
            closeSession();
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
        Account account = null;

        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Account.class.getName()
                                                      + " where email = :email");
            query.setParameter("email", email);
            Object result = query.uniqueResult();

            if (result != null) {
                account = (Account) result;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve Account by email: " + email);
        } finally {
            closeSession();
        }

        return account;
    }

    /**
     * Save the given {@link Account} into the database.
     *
     * @param account account object to save
     * @return Saved account.
     * @throws DAOException
     */

    public Account save(Account account) throws DAOException {
        return super.saveOrUpdate(account);
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
        } finally {
            closeSession();
        }

        return account;
    }
}
