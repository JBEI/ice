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

    public static boolean delete(Folder folder) throws ManagerException {
        if (folder == null)
            throw new ManagerException("Failed to delete null folder!");

        try {
            DAO.delete(folder);
            return true;
        } catch (DAOException e) {
            String msg = "Could not delete folder \"" + folder.getName() + "\" with id "
                    + folder.getId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
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
    @SuppressWarnings("unchecked")
    public static ArrayList<Long> getFolderContents(long id, boolean asc) throws ManagerException {
        Session session = DAO.newSession();
        try {

            //            Criteria c = session.createCriteria(Folder.class).setProjection(
            //                Projections.distinct(Projections.property("entry_id")));
            //            c.
            SQLQuery query = session
                    .createSQLQuery("SELECT entry_id FROM folder_entry WHERE folder_id = :id");
            query.setLong("id", id);
            List list = query.list();
            return (ArrayList<Long>) list;

        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public static Folder removeFolderContents(long folderId, ArrayList<Long> entryIds)
            throws ManagerException {
        Session session = DAO.newSession();
        try {
            session.beginTransaction();
            Query query = session.createQuery("from " + Folder.class.getName() + " where id = :id");
            query.setLong("id", folderId);
            Folder folder = (Folder) query.uniqueResult();
            if (folder == null)
                throw new ManagerException("Cannot retrieve folder with id \"" + folderId + "\"");

            folder.getContents().size();
            boolean isSystemFolder = folder.getOwnerEmail().equals(
                AccountManager.getSystemAccount().getEmail());
            if (isSystemFolder) {
                session.getTransaction().commit();
                throw new ManagerException("Cannot modify non user folder " + folder.getName());
            }

            Iterator<Entry> it = folder.getContents().iterator();

            while (it.hasNext()) {
                Entry entry = it.next();
                if (entryIds.contains(entry.getId()))
                    it.remove();
            }

            folder.setModificationTime(new Date(System.currentTimeMillis()));
            session.saveOrUpdate(folder);
            session.getTransaction().commit();
            return folder;
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            return null;
        } finally {
            if (session.isOpen())
                session.close();
        }
    }

    public static Folder addFolderContents(long folderId, ArrayList<Entry> entrys)
            throws ManagerException {
        Session session = DAO.newSession();
        try {
            session.beginTransaction();
            Query query = session.createQuery("from " + Folder.class.getName() + " where id = :id");
            query.setLong("id", folderId);
            Folder folder = (Folder) query.uniqueResult();
            folder.getContents().size();
            folder.getContents().addAll(entrys);
            folder.setModificationTime(new Date(System.currentTimeMillis()));
            session.saveOrUpdate(folder);
            session.getTransaction().commit();
            return folder;
        } catch (Exception e) {
            session.getTransaction().rollback();
            return null;
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
                    + " WHERE ownerEmail = :ownerEmail order by creationTime desc";
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
