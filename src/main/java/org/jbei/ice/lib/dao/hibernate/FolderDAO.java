package org.jbei.ice.lib.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.shared.ColumnField;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * Manipulate {@link org.jbei.ice.lib.folder.Folder} objects in the database.
 *
 * @author Hector Plahar
 */
public class FolderDAO extends HibernateRepository<Folder> {

    /**
     * Retrieves stored folder by identifier
     *
     * @param id unique identifier for folder
     * @return retrieved folder
     * @throws DAOException
     */
    public Folder get(long id) {
        return super.get(Folder.class, id);
    }

    public Folder removeFolderEntries(Folder folder, ArrayList<Long> entries) {
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
     * If the folder contains other folders, then it returns the number of sub-folders
     *
     * @param id unique folder identifier
     * @return number of child contents in the folder
     */
    public Long getFolderSize(long id) {
        try {
            Criteria criteria = currentSession().createCriteria(Entry.class);
            criteria.createAlias("folders", "f");
            criteria.add(Restrictions.eq("f.id", id));
            Number number = (Number) criteria.setProjection(Projections.rowCount()).uniqueResult();
            return number.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Entry> retrieveFolderContents(long folderId, ColumnField sort, boolean asc, int start, int limit) {
        Session session = currentSession();

        try {
            Folder folder = get(folderId);
            if (folder == null)
                throw new DAOException("Could not locate folder with id " + folderId);

            String sortString;

            switch (sort) {
                default:
                case CREATED:
                    sortString = "id";
                    break;

                case STATUS:
                    sortString = "status";
                    break;

                case NAME:
                    sortString = "name";
                    break;

                case PART_ID:
                    sortString = "partNumber";
                    break;

                case TYPE:
                    sortString = "recordType";
                    break;
            }

            String ascString = asc ? " asc" : " desc";
            String queryString = "select distinct e from Entry e join e.folders f where f.id = :id "
                    + "order by e." + sortString + ascString;

            Query query = session.createQuery(queryString);
            query.setLong("id", folderId);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            List list = query.list();
            return new ArrayList<Entry>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Folder addFolderContents(Folder folder, ArrayList<Entry> entrys) {
        Session session = currentSession();
        try {
            folder = (Folder) session.get(Folder.class, folder.getId());
            folder.getContents().addAll(entrys);
            folder.setModificationTime(new Date());
            session.saveOrUpdate(folder);
            return folder;
        } catch (HibernateException e) {
            Logger.error(e);
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
    public List<Folder> getFoldersByOwner(Account account) {
        ArrayList<Folder> folders;
        Session session = currentSession();
        try {
            String queryString = "from " + Folder.class.getName()
                    + " WHERE ownerEmail = :ownerEmail order by creationTime desc";
            Query query = session.createQuery(queryString);

            query.setParameter("ownerEmail", account.getEmail());
            folders = new ArrayList<Folder>(query.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve folders!", e);
        }

        return folders;
    }

    @SuppressWarnings("unchecked")
    public List<Folder> getFoldersByType(FolderType type) {
        ArrayList<Folder> folders;
        Session session = currentSession();
        try {
            String queryString = "from " + Folder.class.getName() + " WHERE type = :type";
            Query query = session.createQuery(queryString);
            query.setParameter("type", type);
            folders = new ArrayList<Folder>(query.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve folders!", e);
        }

        return folders;
    }

    @SuppressWarnings({"unchecked"})
    public List<Folder> getFoldersByEntry(Entry entry) {
        ArrayList<Folder> folders = new ArrayList<>();
        Session session = currentSession();

        try {
            String hql = "select distinct folder from " + Folder.class.getName()
                    + " folder join folder.contents contents where :entry in contents";
            Query query = session.createQuery(hql);
            query.setParameter("entry", entry);
            folders.addAll(query.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve folders!", e);
        }

        return folders;
    }
}
