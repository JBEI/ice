package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Folder;

public class FolderManager {

    public static Folder get(long id) throws ManagerException {
        Folder result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Folder.class.getName() + " where id = :id");
            query.setLong("id", id);
            result = (Folder) query.uniqueResult();
            result.getContents().size();
        } catch (Exception e) {
            String msg = "Could not get folder by id: " + id + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

<<<<<<< HEAD
=======
    /**
     * Retrieves the count of the number of contents in the folder
     * if the folder contains other folders, the it returns the number of sub-folders
     * 
     * @param id
     *            unique folder identifier
     * @return number of child contents in the folder
     * @throws ManagerException
     *             on any exception retrieving the folder or its contents
     * 
     */
    public static int getFolderSize(long id) throws ManagerException {
        Session session = DAO.newSession();
        try {
            SQLQuery query = session
                    .createSQLQuery("select count(*) from folder_entry where folder_id = :id ");
            query.setLong("id", id);
            return ((BigInteger) query.uniqueResult()).intValue();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Retrieves the entry contents of a folder starting from <code>first</code> up to a maximum of
     * <code>count</code>
     * 
     * @param id
     *            unique folder identifier
     * @param first
     *            folder content to start from
     * @param count
     *            maximum number of entries to return
     * @return list of entries retrieved
     * @throws ManagerException
     *             on any exception accessing the folder or its contents
     */
    public static ArrayList<Long> getFolderContents(long id, boolean asc) throws ManagerException {
        ArrayList<Long> results = new ArrayList<Long>();
        Session session = DAO.newSession();
        try {

            SQLQuery query = session
                    .createSQLQuery("SELECT entry_id FROM folder_entry WHERE folder_id = :id");
            query.setLong("id", id);

            @SuppressWarnings("unchecked")
            List<BigInteger> l = query.list();
            for (BigInteger bi : l)
                results.add(bi.longValue());

            return results;

        } finally {
            if (session.isOpen())
                session.close();
        }
    }

>>>>>>> 3528420... REGISTRY-585: fix slow loading of large collections
    @SuppressWarnings("unchecked")
    public static List<Folder> getFoldersByOwner(Account account) throws ManagerException {

        if (account == null)
            throw new ManagerException("Requesting information for null account");

        ArrayList<Folder> folders = null;

        Session session = DAO.newSession();
        try {
            String queryString = "from " + Folder.class.getName()
                    + " WHERE ownerEmail = :ownerEmail";
            Query query = session.createQuery(queryString);

            query.setParameter("ownerEmail", account.getEmail());
            folders = new ArrayList<Folder>(query.list());

        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve folders!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return folders;
    }

    public static Folder update(Folder folder) throws ManagerException {
        try {
            DAO.save(folder);
        } catch (DAOException e) {
            String msg = "Could not save folder: " + folder.getName() + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }

        return folder;
    }

    public static Folder save(Folder folder) throws ManagerException {
        return update(folder);
    }
}
