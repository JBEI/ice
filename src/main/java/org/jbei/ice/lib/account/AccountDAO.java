package org.jbei.ice.lib.account;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Moderator;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DAO to manipulate {@link Account} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
class AccountDAO extends HibernateRepository {

    /**
     * Retrieve {@link Account} by id from the database.
     *
     * @param id
     * @return Account
     * @throws DAOException
     */
    public Account get(long id) throws DAOException {
        return (Account) super.get(Account.class, id);
    }

    public ArrayList<Account> getAllAccounts() {
        Session session = newSession();
        session.beginTransaction();

        @SuppressWarnings("unchecked")
        ArrayList<Account> reports = (ArrayList<Account>) session.createCriteria(Account.class)
                                                                 .list();
        //        Hibernate.initialize(reports);

        session.getTransaction().commit();
        return reports;
    }

    /**
     * Retrieve all {@link Account}s sorted by the firstName field.
     *
     * @return Set of {@link Account}s.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public Set<Account> getAllByFirstName() throws DAOException {
        LinkedHashSet<Account> accounts = new LinkedHashSet<Account>();
        Session session = newSession();

        session.beginTransaction();
        try {
            String queryString = "from " + Account.class.getName() + " order by firstName";
            Query query = session.createQuery(queryString);
            accounts.addAll(query.list());
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve all accounts", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return accounts;
    }

    public Set<Account> getMatchingAccounts(String token, int limit) throws DAOException {
        Session session = newSession();
        try {
            session.beginTransaction();
            token = token.toUpperCase();
            String queryString = "from " + Account.class.getName()
                    + " where (UPPER(firstName) like '%" + token
                    + "%') OR (UPPER(lastName) like '%" + token + "%')";
            Query query = session.createQuery(queryString);
            if (limit > 0)
                query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new DAOException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Retrieve an {@link Account} by the email field.
     *
     * @param email
     * @return Account
     * @throws DAOException
     */
    public Account getByEmail(String email) throws DAOException {
        Account account = null;

        Session session = newSession();
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
            if (session.isOpen()) {
                session.close();
            }
        }

        return account;
    }

    /**
     * Check if the given {@link Account} has moderator privileges.
     *
     * @param account
     * @return True if the {@link Account} is a moderator.
     * @throws DAOException
     */
    public Boolean isModerator(Account account) throws DAOException {
        if (account == null) {
            throw new DAOException("Failed to determine moderator for null Account!");
        }

        Boolean result = false;

        Session session = newSession();
        try {
            String queryString = "from " + Moderator.class.getName()
                    + " moderator where moderator.account = :account";
            Query query = session.createQuery(queryString);
            query.setParameter("account", account);

            Moderator moderator = (Moderator) query.uniqueResult();
            if (moderator != null) {
                result = true;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to determine moderator for Account: "
                                           + account.getFullName());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Save the given {@link Account} into the database.
     *
     * @param account
     * @return Saved account.
     * @throws DAOException
     */
    public Account save(Account account) throws DAOException {
        return (Account) super.saveOrUpdate(account);
    }

    /**
     * Delete the given {@link Account} in the database.
     *
     * @param account
     * @return True if successful.
     * @throws DAOException
     */
    public Boolean delete(Account account) throws DAOException {
        if (account == null) {
            throw new DAOException("Failed to delete null Account!");
        }

        Boolean result = false;
        delete(account);
        result = true;
        return result;
    }

    /**
     * Retrieve the {@link Account} by the authorization token.
     *
     * @param authToken
     * @return Account.
     * @throws DAOException
     */
    public Account getAccountByAuthToken(String authToken) throws DAOException {
        Account account = null;

        String queryString = "from " + SessionData.class.getName()
                + " sessionData where sessionData.sessionKey = :sessionKey";
        Session session = newSession();
        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setString("sessionKey", authToken);
            SessionData sessionData = (SessionData) query.uniqueResult();
            if (sessionData != null) {
                account = sessionData.getAccount();
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to get Account by token: " + authToken);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return account;
    }

    /**
     * Save the given {@link Moderator} object into the database.
     *
     * @param moderator
     * @return Saved Moderator.
     * @throws DAOException TODO
     */
    public Moderator saveModerator(Moderator moderator) throws DAOException {
        if (moderator == null) {
            throw new DAOException("Cannot to save null Moderator");
        }

        return (Moderator) saveOrUpdate(moderator);
    }
}
