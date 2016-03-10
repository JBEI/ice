package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.common.PageParameters;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Manipulate {@link Folder} objects in the database.
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
     * Removes, from the list of entries in the specified folder, those whose ids match the ids passed in the
     * parameter
     *
     * @param folder  folder to remove entries from
     * @param entries unique identifiers for list of entries to remove from the folder
     * @return folder whose entries where removed
     */
    public Folder removeFolderEntries(Folder folder, List<Long> entries) {
        Session session = currentSession();
        try {
            folder = session.get(Folder.class, folder.getId());
            Iterator<Entry> it = folder.getContents().iterator();

            while (it.hasNext()) {
                Entry entry = it.next();
                if (entries.contains(entry.getId()))
                    it.remove();
            }

            folder.setModificationTime(new Date());
            session.update(folder);
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
     * @param id          unique folder identifier
     * @param filter      optional filter for entry fields
     * @param visibleOnly if true, counts only entries with visibility "OK"
     * @return number of child contents in the folder
     */
    public Long getFolderSize(long id, String filter, boolean visibleOnly) {
        try {
            Criteria criteria = currentSession().createCriteria(Entry.class);
            if (visibleOnly)
                criteria.add(Restrictions.eq("visibility", Visibility.OK.getValue()));
            criteria.createAlias("folders", "f");
            criteria.add(Restrictions.eq("f.id", id));

            addFilter(criteria, filter);

            Number number = (Number) criteria.setProjection(Projections.countDistinct("id")).uniqueResult();
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
    public List<Long> getFolderContentIds(long folderId, EntryType type, boolean visibleOnly) {
        Criteria criteria = currentSession().createCriteria(Folder.class)
                .add(Restrictions.eq("id", folderId))
                .createAlias("contents", "entry");
        if (visibleOnly)
            criteria.add(Restrictions.eq("entry.visibility", Visibility.OK.getValue()));

        if (type != null) {
            criteria.add(Restrictions.eq("entry.recordType", type.getName()));
        }
        return criteria.setProjection(Projections.property("entry.id")).list();
    }

    /**
     * Retrieves list of entries that conforms to the parameters
     *
     * @param folderId       unique identifier for folder whose entries are being retrieved
     * @param pageParameters paging params
     * @param visibleOnly    whether to only include entries with "OK" visibility
     * @return list of found entries
     * @throws DAOException on HibernateException retrieving
     */
    public List<Entry> retrieveFolderContents(long folderId, PageParameters pageParameters, boolean visibleOnly) {
        try {
            String sortString;
            switch (pageParameters.getSortField()) {
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

            Criteria criteria = currentSession().createCriteria(Entry.class);
            if (visibleOnly)
                criteria.add(Restrictions.eq("visibility", Visibility.OK.getValue()));
            criteria.createAlias("folders", "folder");
            criteria.add(Restrictions.eq("folder.id", folderId));

            addFilter(criteria, pageParameters.getFilter());

            criteria.addOrder(pageParameters.isAscending() ? Order.asc(sortString) : Order.desc(sortString));
            criteria.setMaxResults(pageParameters.getLimit());
            criteria.setFirstResult(pageParameters.getOffset());
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    protected final void addFilter(Criteria criteria, String filterText) {
        if (filterText == null || filterText.trim().isEmpty())
            return;

        criteria.add(Restrictions.disjunction()
                .add(Restrictions.ilike("name", filterText, MatchMode.ANYWHERE))
                .add(Restrictions.ilike("alias", filterText, MatchMode.ANYWHERE))
                .add(Restrictions.ilike("shortDescription", filterText, MatchMode.ANYWHERE))
                .add(Restrictions.ilike("partNumber", filterText, MatchMode.ANYWHERE)));
    }

    public Folder addFolderContents(Folder folder, List<Entry> entrys) {
        Session session = currentSession();
        try {
            folder = session.get(Folder.class, folder.getId());
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
                    .add(Restrictions.eq("ownerEmail", account.getEmail()).ignoreCase())
                    .add(Restrictions.ne("type", FolderType.REMOTE));
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

    /**
     * Retrieves folders that the specified account owns, or has write privileges on based on the permissions
     *
     * @param account       account that is expected to have write privileges on the folders that are returned
     * @param accountGroups groups that account belongs to that is expected to have write privileges
     * @return list of folders that the account or groups that the account belongs to has write privileges on
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

        Disjunction disjunction = Restrictions.or(Restrictions.eq("ownerEmail", account.getEmail()).ignoreCase());
        if (!resultList.isEmpty()) {
            disjunction.add(Restrictions.in("id", resultList));
        }

        Criteria criteria = currentSession().createCriteria(Folder.class).add(disjunction);
        return criteria.list();
    }

    public int setFolderEntryVisibility(long folderId, Visibility ok) {
        Criteria criteria = currentSession().createCriteria(Folder.class)
                .add(Restrictions.eq("id", folderId))
                .createAlias("contents", "entry").setProjection(Projections.property("entry.id"));
        List list = criteria.list();

        // update entries where folder id in
        Query query = currentSession().createQuery("update " + Entry.class.getName()
                + " e set e.visibility=:v where e.id in :ids");
        query.setParameter("v", ok.getValue());
        query.setParameterList("ids", list);
        return query.executeUpdate();
    }

    public List<Folder> filterByName(String token, int limit) {
        try {
            return currentSession().createCriteria(Folder.class)
                    .add(Restrictions.ilike("name", token, MatchMode.ANYWHERE))
                    .setMaxResults(limit)
                    .list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
