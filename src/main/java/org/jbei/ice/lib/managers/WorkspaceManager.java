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

/**
 * Manager to manipulate {@link Workspace} objects.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class WorkspaceManager {
    /**
     * Create a new {@link Workspace} object using the given {@link Account} and {@link Entry}.
     * 
     * @param account
     * @param entry
     * @return Workspace object.
     * @throws ManagerException
     */
    public static Workspace create(Account account, Entry entry) throws ManagerException {
        Workspace result = new Workspace(account, entry);
        try {
            DAO.save(result);
        } catch (DAOException e) {
            throw new ManagerException("Failed to create Workspace!", e);
        }
        return result;
    }

    /**
     * Save the given {@link Workspace} object into the database.
     * 
     * @param workspace
     * @return
     * @throws ManagerException
     */
    public static Workspace addOrUpdate(Workspace workspace) throws ManagerException {
        Workspace result = get(workspace.getAccount(), workspace.getEntry());
        if (result == null) {
            result = workspace;
        }

        result.setInWorkspace(true);

        result = save(result);

        return result;
    }

    /**
     * Determine if the given {@link Account} and {@link Entry} has an associated {@link Workspace}.
     * 
     * @param account
     * @param entry
     * @return True if a Workspace exists.
     */
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

    /**
     * Determine if the given {@link Entry} has an associated {@link Workspace} for the currently
     * active user.
     * <p>
     * TODO Because this method invokes the current session, perhaps it needs to be moved to a
     * controller.
     * 
     * @param entry
     * @return
     * @throws ManagerException
     */
    public static boolean hasEntry(Entry entry) throws ManagerException {
        Account account = IceSession.get().getAccount();

        return hasEntry(account, entry);
    }

    /**
     * Update the number visited statistic for the {@link Workspace} associated with {@link Account}
     * and the {@link Entry}.
     * 
     * @param account
     * @param entry
     * @throws ManagerException
     */
    public synchronized static void setVisited(Account account, Entry entry)
            throws ManagerException {
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

    /**
     * Record that an {@link Entry} object has been visited.
     * 
     * @param entry
     * @throws ManagerException
     */
    public static void setVisited(Entry entry) throws ManagerException {
        Account account = IceSession.get().getAccount();
        setVisited(account, entry);
    }

    /**
     * Retrieve all the {@link} Workspace objects associated with the currently logged in user.
     * <p>
     * TODO Perhaps this method should be in a controller.
     * 
     * @return ArrayList of Workspace objects.
     * @throws ManagerException
     */
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

    /**
     * Retrieve {@link Workspace} objects by the given {@link Account} object, with offset and limit
     * options.
     * 
     * @param account
     * @param offset
     * @param limit
     * @return ArrayList of Workspace objects.
     * @throws ManagerException
     */
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

    /**
     * Retrieve {@link Workspace} objects by the given {@link Account} and {@link Entry} objects.
     * 
     * @param account
     * @param entry
     * @return Workspace object.
     * @throws ManagerException
     */
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

    /**
     * Retrieve the number of {@link Workspace} objects associated with the given {@link Account}.
     * 
     * @param account
     * @return Number.
     * @throws ManagerException
     */
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

    /**
     * Save the given {@link Workspace} object into the database.
     * 
     * @param workspace
     * @return Saved Workspace object.
     * @throws ManagerException
     */
    public static Workspace save(Workspace workspace) throws ManagerException {
        Workspace result = null;

        try {
            Workspace existingWorkspace = get(workspace.getAccount(), workspace.getEntry());
            // prevent duplicate workspace rows
            if (existingWorkspace != null) {
                existingWorkspace.setDateAdded(workspace.getDateAdded());
                existingWorkspace.setDateVisited(workspace.getDateVisited());
                existingWorkspace.setInWorkspace(workspace.isInWorkspace());
                existingWorkspace.setStarred(workspace.isStarred());
                existingWorkspace.setNumberVisited(workspace.getNumberVisited());
                result = (Workspace) DAO.save(existingWorkspace);
            } else {
                result = (Workspace) DAO.save(workspace);
            }
        } catch (DAOException e) {
            throw new ManagerException("Failed to save workspace!", e);
        }

        return result;
    }

    /**
     * Retrieve the {@link Workspace} objects recently viewed by the given {@link Account}, with
     * offset and limit options.
     * 
     * @param account
     * @param offset
     * @param limit
     * @return ArrayList of Workspace objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Workspace> getRecentlyViewedByAccount(Account account, int offset,
            int limit) throws ManagerException {
        if (offset > 50) {
            offset = 50;
        }
        ArrayList<Workspace> result = null;
        Session session = DAO.newSession();
        try {
            String queryString = "from Workspace workspace where account=:account order by workspace.dateVisited desc";
            Query query = session.createQuery(queryString);
            query.setParameter("account", account);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            result = new ArrayList<Workspace>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Could not get recently viewed workspace by account ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    /**
     * Retrieve the number of recently viewed entries from {@link Workspace} object associated with
     * the given {@link Account} object.
     * <p>
     * Maximum value returned is 50.
     * 
     * @param account
     * @return Number, limited to 50.
     * @throws ManagerException
     */
    public static int getRecentlyViewedCount(Account account) throws ManagerException {
        int size = 0;
        Session session = DAO.newSession();
        try {
            String queryString = "from Workspace workspace where account=:account order by workspace.dateAdded desc";
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
        if (size > 50) {
            size = 50;
        }
        return size;
    }
}
