package org.jbei.ice.lib.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.shared.ColumnField;

import java.util.*;

/**
 * Manipulate {@link org.jbei.ice.lib.folder.Folder} objects in the database.
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class FolderDAO extends HibernateRepository<Folder> {

    /**
     * Retrieves stored folder by locally unique identifier
     *
     * @param id locally unique identifier for folder
     * @return retrieved folder
     * @throws DAOException
     */
    public Folder get(long id) {
        return super.get(Folder.class, id);
    }

    /**
     * Removes entries with the which have the unique identifier in the list of entries
     * from the specified folder, if it is contained in it
     *
     * @param folder  folder to remove entries from
     * @param entries unique identifiers for list of entries to remove from the folder
     * @return folder whose entries where removed
     */
    public Folder removeFolderEntries(Folder folder, List<Long> entries) {
        Session session = currentSession();
        try {
            folder = (Folder) session.get(Folder.class, folder.getId());
            Iterator<Entry> it = folder.getContents().iterator();

            while (it.hasNext()) {
                Entry entry = it.next();
                if (entries.contains(entry.getId()))
                    it.remove();
            }

            folder.setModificationTime(new Date());
            session.saveOrUpdate(folder);
            return folder;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves the count of the number of contents in the folder.
     * Currently, it is assumed that the contents of folders are only entries. The entries
     * that are counted are those that have a visibility of "OK"
     *
     * @param id unique folder identifier
     * @return number of child contents in the folder
     */
    public Long getFolderSize(long id) {
        try {
            Criteria criteria = currentSession().createCriteria(Entry.class);
            criteria.add(Restrictions.eq("visibility", Visibility.OK.getValue()));
            criteria.createAlias("folders", "f");
            criteria.add(Restrictions.eq("f.id", id));
            Number number = (Number) criteria.setProjection(Projections.rowCount()).uniqueResult();
            return number.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves the ids of any entries that are contained in the specified folder; optionally filtered by entry type
     *
     * @param folderId unique folder identifier
     * @param type     optional filter for entries. If null, all entries will be retrieved
     * @return List of entry ids found in the folder with the filter applied if applicable and which have
     * a visibility of "OK"
     */
    public List<Long> getFolderContentIds(long folderId, EntryType type) {
        Criteria criteria = currentSession().createCriteria(Folder.class)
                .add(Restrictions.eq("id", folderId))
                .createAlias("contents", "entry")
                .add(Restrictions.eq("entry.visibility", Visibility.OK.getValue()));

        if (type != null) {
            criteria.add(Restrictions.eq("entry.recordType", type.getName()));
        }
        return criteria.setProjection(Projections.property("entry.id")).list();
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
            return new ArrayList<>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Folder addFolderContents(Folder folder, List<Entry> entrys) {
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
        try {
            Criteria criteria = currentSession().createCriteria(Folder.class)
                    .add(Restrictions.eq("ownerEmail", account.getEmail()));
            criteria.addOrder(Order.desc("creationTime"));
            return criteria.list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve folders!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Folder> getFoldersByType(FolderType type) {
        try {
            return currentSession().createCriteria(Folder.class)
                    .add(Restrictions.eq("type", type))
                    .list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve folders!", e);
        }
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

    /**
     * Retrieves folders that the specified account owns, or has write privileges on based on the permissions
     *
     * @param account
     * @return
     * @throws DAOException
     */
    public List<Folder> getCanEditFolders(Account account, Set<Group> accountGroups) throws DAOException {
        List resultList = currentSession().createCriteria(Permission.class)
                .add(Restrictions.disjunction()
                        .add(Restrictions.eq("account", account))
                        .add(Restrictions.in("group", accountGroups)))
                .add(Restrictions.eq("canWrite", true))
                .add(Restrictions.isNotNull("folder"))
                .setProjection(Projections.property("folder.id"))
                .list();

        Disjunction disjunction = Restrictions.or(Restrictions.eq("ownerEmail", account.getEmail()));
        if (!resultList.isEmpty()) {
            disjunction.add(Restrictions.in("id", resultList));
        }

        Criteria criteria = currentSession().createCriteria(Folder.class).add(disjunction);
        return criteria.list();
    }
}
