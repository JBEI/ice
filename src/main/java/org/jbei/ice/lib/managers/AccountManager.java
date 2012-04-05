package org.jbei.ice.lib.managers;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Moderator;
import org.jbei.ice.lib.models.SessionData;

/**
 * Manager to manipulate {@link Account} objects in the database.
 * 
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 * 
 */
public class AccountManager {

    private static String SYSTEM_ACCOUNT_EMAIL = "system";

    /**
     * Retrieve {@link Account} by id from the database.
     * 
     * @param id
     * @return Account
     * @throws ManagerException
     */
    public static Account get(long id) throws ManagerException {
        Account account = null;

        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("from " + Account.class.getName() + " where id = :id");
            query.setParameter("id", id);

            account = (Account) query.uniqueResult();
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve Account by id: " + String.valueOf(id), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return account;
    }

    /**
     * Retrieve the System {@link Account}.
     * <p>
     * The System account has full privileges, but is not a log in account.
     * 
     * @return System Account
     * @throws ManagerException
     */
    public static Account getSystemAccount() throws ManagerException {
        return getByEmail(SYSTEM_ACCOUNT_EMAIL);
    }

    /**
     * Retrieve all {@link Account}s sorted by the firstName field.
     * 
     * @return Set of {@link Account}s.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static Set<Account> getAllByFirstName() throws ManagerException {
        LinkedHashSet<Account> accounts = new LinkedHashSet<Account>();

        Session session = DAO.newSession();
        try {
            String queryString = "from " + Account.class.getName() + " order by firstName";

            Query query = session.createQuery(queryString);

            accounts.addAll(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve all accounts", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return accounts;
    }

    public static Set<Account> getMatchingAccounts(String token, int limit) throws ManagerException {
        Session session = DAO.newSession();
        try {
            token = token.toUpperCase();
            String queryString = "from " + Account.class.getName()
                    + " where (UPPER(firstName) like '%" + token
                    + "%') OR (UPPER(lastName) like '%" + token + "%')";
            Query query = session.createQuery(queryString);
            query.setFetchSize(limit);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e);
            throw new ManagerException(e);
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
     * @throws ManagerException
     */
    public static Account getByEmail(String email) throws ManagerException {
        Account account = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Account.class.getName()
                    + " where email = :email");

            query.setParameter("email", email);

            Object result = query.uniqueResult();

            if (result != null) {
                account = (Account) result;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve Account by email: " + email);
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
     * @throws ManagerException
     */
    public static Boolean isModerator(Account account) throws ManagerException {
        if (account == null) {
            throw new ManagerException("Failed to determine moderator for null Account!");
        }

        Boolean result = false;

        Session session = DAO.newSession();
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
            throw new ManagerException("Failed to determine moderator for Account: "
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
     * @throws ManagerException
     */
    public static Account save(Account account) throws ManagerException {
        if (account == null) {
            throw new ManagerException("Failed to save null Account!");
        }

        Account result = null;

        try {
            result = (Account) DAO.save(account);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save Account: " + account.getFullName(), e);
        }

        return result;
    }

    /**
     * Delete the given {@link Account} in the database.
     * 
     * @param account
     * @return True if successful.
     * @throws ManagerException
     */
    public static Boolean delete(Account account) throws ManagerException {
        if (account == null) {
            throw new ManagerException("Failed to delete null Account!");
        }

        Boolean result = false;

        try {
            DAO.delete(account);
            result = true;
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete Account: " + account.getFullName(), e);
        }
        return result;
    }

    /**
     * Retrieve the {@link Account} by the authorization token.
     * 
     * @param authToken
     * @return Account.
     * @throws ManagerException
     */
    public static Account getAccountByAuthToken(String authToken) throws ManagerException {
        Account account = null;

        String queryString = "from " + SessionData.class.getName()
                + " sessionData where sessionData.sessionKey = :sessionKey";
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery(queryString);
            query.setString("sessionKey", authToken);
            SessionData sessionData = (SessionData) query.uniqueResult();
            if (sessionData != null) {
                account = sessionData.getAccount();
            }

        } catch (HibernateException e) {
            throw new ManagerException("Failed to get Account by token: " + authToken);
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
     * @throws ManagerException
     */
    public static Moderator saveModerator(Moderator moderator) throws ManagerException {
        if (moderator == null) {
            throw new ManagerException("Failed to save null Moderator");
        }
        Moderator result = null;
        try {
            result = (Moderator) DAO.save(moderator);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save Moderator: "
                    + moderator.getAccount().getEmail(), e);
        }

        return result;
    }
}
