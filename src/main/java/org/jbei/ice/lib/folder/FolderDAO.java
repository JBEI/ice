package org.jbei.ice.lib.folder;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.folder.FolderType;

import java.util.*;

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

    public Folder removeFolderEntries(Folder folder, ArrayList<Long> entries) throws DAOException {
        Session session = currentSession();
        try {
            folder = (Folder) session.get(Folder.class, folder.getId());
            Iterator<Entry> it = folder.getContents().iterator();

            while (it.hasNext()) {
                Entry entry = it.next();
                if (entries.contains(entry.getId()))
                    it.remove();
            }

            folder.setModificationTime(new Date(System.currentTimeMillis()));
            session.saveOrUpdate(folder);
            return folder;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
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
    public Long getFolderSize(long id) throws DAOException {
        Session session = currentSession();
        try {
            SQLQuery query = session.createSQLQuery("select count(*) from folder_entry where folder_id = :id ");
            query.setLong("id", id);
            Number result = ((Number) query.uniqueResult());
            return result.longValue();

        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Entry> retrieveFolderContents(long folderId, ColumnField sort, boolean asc, int start, int limit)
            throws DAOException {
        Session session = currentSession();
        try {
            Folder folder = get(folderId);
            if (folder == null)
                throw new DAOException();

            String queryString = " order by";

            switch (sort) {
                default:
                case CREATED:
                    queryString += " id";
                    break;

                case STATUS:
                    queryString += " status";
                    break;

                case TYPE:
                    queryString += " recordType";
                    break;
            }

            queryString += (asc ? " asc" : " desc");
            Query query = session.createFilter(folder.getContents(), queryString);
            List list = query.setFirstResult(start).setMaxResults(limit).list();
            ArrayList<Entry> results = new ArrayList<Entry>(list);
            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Folder addFolderContents(Folder folder, ArrayList<Entry> entrys) throws DAOException {
        Session session = currentSession();
        try {
            folder = (Folder) session.get(Folder.class, folder.getId());
            folder.getContents().addAll(entrys);
            folder.setModificationTime(new Date(System.currentTimeMillis()));
            session.saveOrUpdate(folder);
            return folder;
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve all {@link Folder}s owned by given the {@link Account}.
     *
     * @param account owner account
     * @return List of Folder objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<Folder> getFoldersByOwner(Account account) throws DAOException {
        ArrayList<Folder> folders;
        Session session = currentSession();
        try {
            String queryString = "from " + Folder.class.getName()
                    + " WHERE ownerEmail = :ownerEmail order by creationTime desc";
            Query query = session.createQuery(queryString);

            query.setParameter("ownerEmail", account.getEmail());
            folders = new ArrayList<Folder>(query.list());
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve folders!", e);
        }

        return folders;
    }

    @SuppressWarnings("unchecked")
    public List<Folder> getFoldersByType(FolderType type) throws DAOException {
        ArrayList<Folder> folders;
        Session session = currentSession();
        try {
            String queryString = "from " + Folder.class.getName() + " WHERE type = :type";
            Query query = session.createQuery(queryString);
            query.setParameter("type", type);
            folders = new ArrayList<Folder>(query.list());
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve folders!", e);
        }

        return folders;
    }

    @SuppressWarnings({"unchecked"})
    public List<Folder> getFoldersByEntry(Entry entry) throws DAOException {
        ArrayList<Folder> folders = new ArrayList<>();
        Session session = currentSession();

        try {
            String hql = "select distinct folder from " + Folder.class.getName()
                    + " folder join folder.contents contents where :entry in contents";
            Query query = session.createQuery(hql);
            query.setParameter("entry", entry);
            folders.addAll(query.list());
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve folders!", e);
        }

        return folders;
    }

    @SuppressWarnings("unchecked")
    public Set<Long> getAllFolderIds() throws DAOException {
        try {
            List list = currentSession().createCriteria(Folder.class).setProjection(Projections.property("id")).list();
            return new HashSet<Long>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
