package org.jbei.ice.lib.managers;

import java.util.ArrayList;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.web.IceSession;

public class WorkspaceManager {
    public static Workspace create(Account account, Entry entry) throws ManagerException {
        Workspace result = new Workspace(account, entry);
        try {
            DAO.save(result);
        } catch (DAOException e) {
            throw new ManagerException("Failed to create Workspace!", e);
        }
        return result;
    }

    public static Workspace addOrUpdate(Workspace workspace) throws ManagerException {
        Workspace result = get(workspace.getAccount(), workspace.getEntry());
        if (result == null) {
            result = workspace;
        }

        result.setInWorkspace(true);

        result = save(result);

        return result;
    }

    public static boolean hasEntry(Account account, Entry entry) {
        boolean result = false;

        String queryString = "from Workspace workspace where entry=:entry and account=:account";
        Session session = DAO.newSession();
        Query query = session.createQuery(queryString);
        query.setParameter("entry", entry);
        query.setParameter("account", account);

        try {
            Workspace queryResult = (Workspace) query.uniqueResult();
            if (queryResult != null) {
                if (queryResult.isInWorkspace()) {
                    result = true;
                }
            }
        } catch (Exception e) {
            Logger.error("Could not determine if account's entry is in workspace", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static boolean hasEntry(Entry entry) throws ManagerException {
        Account account = IceSession.get().getAccount();

        return hasEntry(account, entry);
    }

    public static void setVisited(Account account, Entry entry) throws ManagerException {
        try {
            Workspace queryResult = get(account, entry);
            if (queryResult == null) {
                queryResult = create(account, entry);
            }

            long numberVisited = queryResult.getNumberVisited() + 1;
            queryResult.setNumberVisited(numberVisited);
            queryResult.setDateVisited(System.currentTimeMillis());
            DAO.save(queryResult);
        } catch (Exception e) {
            throw new ManagerException("Could not set visited number", e);
        }
    }

    public static void setVisited(Entry entry) throws ManagerException {
        Account account = IceSession.get().getAccount();
        setVisited(account, entry);
    }

    public static ArrayList<Workspace> get() throws ManagerException {
        ArrayList<Workspace> result = null;
        Account account = IceSession.get().getAccount();
        String queryString = "from Workspace workspace where account=:account order by workspace.dateAdded desc";
        Session session = DAO.newSession();
        Query query = session.createQuery(queryString);
        query.setParameter("account", account);
        try {
            @SuppressWarnings("unchecked")
            ArrayList<Workspace> temp = new ArrayList<Workspace>(query.list());
            result = temp;
        } catch (Exception e) {
            if (session.isOpen()) {
                session.close();
            }
            throw new ManagerException("Could not get workspace for account " + account.getEmail(),
                    e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Workspace> getByAccount(Account account, int offset, int limit)
            throws ManagerException {
        ArrayList<Workspace> result = null;
        Session session = DAO.newSession();
        try {
            String queryString = "from Workspace where account=:account and inWorkspace = true";
            Query query = session.createQuery(queryString);
            query.setParameter("account", account);
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            result = new ArrayList<Workspace>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Could not get workspace by account ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static Workspace get(Account account, Entry entry) throws ManagerException {
        String queryString = "from Workspace workspace where entry=:entry and account=:account";
        Session session = DAO.newSession();
        Query query = session.createQuery(queryString);
        query.setParameter("entry", entry);
        query.setParameter("account", account);
        Workspace result = null;
        try {
            result = (Workspace) query.uniqueResult();
        } catch (Exception e) {
            throw new ManagerException("Could not get by account and entry", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    public static int getCountByAccount(Account account) throws ManagerException {
        int size = 0;
        Session session = DAO.newSession();
        try {
            String queryString = "from Workspace workspace where account=:account and inWorkspace = true order by workspace.dateAdded desc";
            Query query = session.createQuery(queryString);
            query.setParameter("account", account);

            size = query.list().size();
        } catch (HibernateException e) {
            throw new ManagerException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return size;
    }

    public static Workspace save(Workspace workspace) throws ManagerException {
        Workspace result = null;

        try {
            result = (Workspace) DAO.save(workspace);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save workspace!", e);
        }

        return result;
    }
}
