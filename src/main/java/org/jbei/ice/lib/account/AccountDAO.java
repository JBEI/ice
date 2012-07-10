package org.jbei.ice.lib.account;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.AccountType;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Moderator;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

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

    @SuppressWarnings("unchecked")
    public ArrayList<Account> getAllAccounts() throws DAOException {

        ArrayList<Account> reports;
        Session session = newSession();

        try {
            session.beginTransaction();
            reports = (ArrayList<Account>) session.createCriteria(Account.class).list();
            session.getTransaction().commit();
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }

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
            closeSession(session);
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
            closeSession(session);
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
            closeSession(session);
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
            closeSession(session);
        }

        return account;
    }

    @SuppressWarnings("deprecation")
    public void updateModeratorAccounts() throws DAOException {
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery("from " + Moderator.class.getName());
            @SuppressWarnings("unchecked")
            List<Moderator> results = new ArrayList<Moderator>(query.list());
            for (Moderator moderator : results) {
                Account account = moderator.getAccount();
                account.setType(AccountType.ADMIN);
                session.update(account);
            }
            session.getTransaction().commit();
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

    }
}
