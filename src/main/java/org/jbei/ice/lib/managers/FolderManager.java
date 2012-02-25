package org.jbei.ice.lib.managers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;

/**
 * Manipulate {@link Folder} objects in the database.
 * 
 * @author Hector Plahar
 * 
 */
public class FolderManager {

    /**
     * Retrieve {@link Folder} objects from the database by id.
     * 
     * @param id
     * @return Folder object.
     * @throws ManagerException
     */
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

    /**
     * Retrieves the count of the number of contents in the folder.
     * If the folder contains other folders, the it returns the number of sub-folders
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
     * Retrieves the entry contents of a folder contents.
     * 
     * @param id
     *            folder id.
     * @param asc
     *            true if ascending.
     * @return List of Entry ids.
     * @throws ManagerException
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
            for (BigInteger bi : l) {
                results.add(bi.longValue());
            }

            return results;

        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public static boolean removeFolderContents(long folderId, ArrayList<Long> entryIds)
            throws ManagerException {
        Session session = DAO.newSession();
        try {
            Folder folder = get(folderId);
            Iterator<Entry> it = folder.getContents().iterator();

            while (it.hasNext()) {
                Entry entry = it.next();
                if (entryIds.contains(entry.getId()))
                    it.remove();
            }
            update(folder);
            return true;

        } finally {
            if (session.isOpen())
                session.close();
        }
    }

    public static Folder addFolderContents(long folderId, ArrayList<Long> entryIds)
            throws ManagerException {
        Session session = DAO.newSession();
        try {
            Folder folder = get(folderId);
            ArrayList<Entry> entries = EntryManager.getEntriesByIdSet(entryIds);
            if (entries == null)
                return folder;

            folder.getContents().addAll(entries);
            return update(folder);
        } finally {
            if (session.isOpen())
                session.close();
        }
    }

    /**
     * Retrieve all {@link Folder}s owned by given the {@link Account}.
     * 
     * @param account
     * @return List of Folder objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static List<Folder> getFoldersByOwner(Account account) throws ManagerException {

        if (account == null) {
            throw new ManagerException("Requesting information for null account");
        }

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

    /**
     * Save the {@link Folder} object in the database.
     * 
     * @param folder
     * @return Saved Folder object.
     * @throws ManagerException
     */
    public static Folder update(Folder folder) throws ManagerException {
        try {
            folder.setModificationTime(new Date(System.currentTimeMillis()));
            DAO.save(folder);
        } catch (DAOException e) {
            String msg = "Could not save folder: " + folder.getName() + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }

        return folder;
    }

    public static Folder save(Folder folder) throws ManagerException {
        folder.setCreationTime(new Date(System.currentTimeMillis()));
        return update(folder);
    }
}
