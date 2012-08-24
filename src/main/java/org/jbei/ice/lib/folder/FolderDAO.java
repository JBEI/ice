package org.jbei.ice.lib.folder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * Manipulate {@link Folder} objects in the database.
 *
 * @author Hector Plahar
 */
class FolderDAO extends HibernateRepository<Folder> {

    /**
     * Retrieves stored folder by identifier
     *
     * @param id unique identifier for folder
     * @return retrieved folder
     * @throws DAOException
     */
    public Folder get(long id) throws DAOException {
        return super.get(Folder.class, id);
    }

    /**
     * deletes stored folder
     *
     * @param folder folder to delete
     * @throws DAOException
     */
    public void delete(Folder folder) throws DAOException {
        super.delete(folder);
    }

    public Folder removeFolderEntries(Folder folder, ArrayList<Long> entries) throws DAOException {

        Session session = newSession();
        try {
            session.getTransaction().begin();
            folder = (Folder) session.get(Folder.class, folder.getId());
            Iterator<Entry> it = folder.getContents().iterator();

            while (it.hasNext()) {
                Entry entry = it.next();
                if (entries.contains(entry.getId()))
                    it.remove();
            }

            folder.setModificationTime(new Date(System.currentTimeMillis()));
            session.saveOrUpdate(folder);
            session.getTransaction().commit();
            return folder;
        } catch (HibernateException he) {
            Logger.error(he);
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            if (session != null)
                session.close();
        }
    }

    /**
     * Retrieves the count of the number of contents in the folder.
     * If the folder contains other folders, the it returns the number of sub-folders
     *
     * @param id unique folder identifier
     * @return number of child contents in the folder
     * @throws DAOException on any exception retrieving the folder or its contents
     */
    public BigInteger getFolderSize(long id) throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
            SQLQuery query = session
                    .createSQLQuery("select count(*) from folder_entry where folder_id = :id ");
            query.setLong("id", id);
            BigInteger result = ((BigInteger) query.uniqueResult());
            session.getTransaction().commit();
            return result;
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Retrieves the entry contents of a folder contents.
     *
     * @param id folder id.
     * @return List of Entry ids.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Long> getFolderContents(long id) throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
            SQLQuery query = session
                    .createSQLQuery("SELECT entry_id FROM folder_entry WHERE folder_id = :id");
            query.setLong("id", id);
            @SuppressWarnings("rawtypes")
            List list = query.list();
            session.getTransaction().commit();
            return (ArrayList<Long>) list;
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public Folder addFolderContents(Folder folder, ArrayList<Entry> entrys) throws DAOException {
        Session session = newSession();
        try {
            session.beginTransaction();
            folder = (Folder) session.get(Folder.class, folder.getId());
            folder.getContents().addAll(entrys);
            folder.setModificationTime(new Date(System.currentTimeMillis()));
            session.saveOrUpdate(folder);
            session.getTransaction().commit();
            return folder;
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException(e);
        } finally {
            if (session.isOpen())
                session.close();
        }
    }

    public Folder save(Folder folder) throws DAOException {
        Session session = newSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(folder);
            session.getTransaction().commit();
            return folder;
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            if (session != null)
                session.close();
        }
    }

    /**
     * Retrieve all {@link Folder}s owned by given the {@link Account}.
     *
     * @param account
     * @return List of Folder objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<Folder> getFoldersByOwner(Account account) throws DAOException {

        ArrayList<Folder> folders = null;
        Session session = newSession();
        try {
            String queryString = "from " + Folder.class.getName()
                    + " WHERE ownerEmail = :ownerEmail order by creationTime desc";
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);

            query.setParameter("ownerEmail", account.getEmail());
            folders = new ArrayList<Folder>(query.list());
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve folders!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return folders;
    }

    @SuppressWarnings({"unchecked"})
    public List<Folder> getFoldersByEntry(Entry entry) throws DAOException {
        ArrayList<Folder> folders = new ArrayList<Folder>();
        Session session = newSession();

        try {
            session.getTransaction().begin();
            String hql = "select distinct folder from " + Folder.class.getName()
                    + " folder join folder.contents contents where :entry in contents";
            Query query = session.createQuery(hql);
            query.setParameter("entry", entry);
            folders.addAll(query.list());
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve folders!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return folders;
    }
}
