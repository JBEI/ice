package org.jbei.ice.lib.managers;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Moderator;
import org.jbei.ice.lib.models.SessionData;

public class AccountManager {
    public static Account get(int id) throws ManagerException {
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
}
