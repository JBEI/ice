package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.common.PageParameters;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.*;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * Manipulate {@link Folder} objects in the database.
 *
 * @author Hector Plahar
 */
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
     * Attempt to retrieve a local folder that references a remote folder.
     *
     * @param remoteFolderId   folder id of the remote folder. This is stored in the <code>description</code> field
     * @param remoteOwnerEmail user id of remote owner of folder
     * @return located folder of type <code>REMOTE</code> if found, null otherwise
     */
    public Folder getRemote(String remoteFolderId, String remoteOwnerEmail) {
        CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
        Root<Folder> from = query.from(Folder.class);
        query.where(
                getBuilder().equal(from.get("type"), FolderType.REMOTE),
                getBuilder().equal(from.get("description"), remoteFolderId),
                getBuilder().equal(from.get("ownerEmail"), remoteOwnerEmail)
        );
        return currentSession().createQuery(query).uniqueResult();
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
        try {
            folder = currentSession().get(Folder.class, folder.getId());
            folder.getContents().removeIf(entry -> entries.contains(entry.getId()));
            folder.setModificationTime(new Date());
            currentSession().update(folder);
            return folder;
        } catch (Exception he) {
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
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Folder> from = query.from(Folder.class);
            Join<Folder, Entry> entry = from.join("contents");

            List<Predicate> predicates = getPredicates(entry, filter);

            if (visibleOnly) {
                predicates.add(entry.get("visibility").in(Arrays.asList(Visibility.OK.getValue(),
                        Visibility.REMOTE.getValue())));
            }

            predicates.add(getBuilder().equal(from.get("id"), id));
            query.select(getBuilder().countDistinct(entry.get("id")));
            query.where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception he) {
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
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Folder> from = query.from(Folder.class);
            Join<Folder, Entry> entry = from.join("contents");

            ArrayList<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().equal(from.get("id"), folderId));
            if (visibleOnly) {
                predicates.add(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));
            }
            if (type != null) {
                predicates.add(getBuilder().equal(entry.get("recordType"), type.getName()));
            }
            query.select(entry.get("id")).where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves list of entries that conforms to the parameters
     *
     * @param folderId       unique identifier for folder whose entries are being retrieved
     * @param pageParameters paging params
     * @param visibleOnly    whether to only include entries with "OK" visibility
     * @return list of found entries
     * @throws DAOException on Exception retrieving
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

            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Folder> from = query.from(Folder.class);
            Join<Folder, Entry> entry = from.join("contents");

            List<Predicate> predicates = getPredicates(entry, pageParameters.getFilter());
            predicates.add(getBuilder().equal(from.get("id"), folderId));

            if (visibleOnly) {
                predicates.add(entry.get("visibility").in(Arrays.asList(Visibility.OK.getValue(),
                        Visibility.REMOTE.getValue())));
            }
            query.select(entry).where(predicates.toArray(new Predicate[0]));
            query.orderBy(pageParameters.isAscending() ? getBuilder().asc(entry.get(sortString)) :
                    getBuilder().desc(entry.get(sortString)));
            return currentSession().createQuery(query).setFirstResult(pageParameters.getOffset())
                    .setMaxResults(pageParameters.getLimit()).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    private List<Predicate> getPredicates(Join<Folder, Entry> entry, String filter) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        if (filter != null && !filter.trim().isEmpty()) {
            filter = filter.toLowerCase();
            predicates.add(getBuilder().or(
                    getBuilder().like(getBuilder().lower(entry.get("name")), "%" + filter + "%"),
                    getBuilder().like(getBuilder().lower(entry.get("alias")), "%" + filter + "%"),
                    getBuilder().like(getBuilder().lower(entry.get("shortDescription")), "%" + filter + "%"),
                    getBuilder().like(getBuilder().lower(entry.get("partNumber")), "%" + filter + "%")
            ));
        }
        return predicates;
    }

    public Folder addFolderContents(Folder folder, List<Entry> entrys) {
        Session session = currentSession();
        try {
            folder = session.get(Folder.class, folder.getId());
            folder.getContents().addAll(entrys);
            folder.setModificationTime(new Date());
            session.saveOrUpdate(folder);
            return folder;
        } catch (Exception e) {
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
    public List<Folder> getFoldersByOwner(Account account) {
        try {
            CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
            Root<Folder> from = query.from(Folder.class);
            query.where(
                    getBuilder().equal(getBuilder().lower(from.get("ownerEmail")), account.getEmail().toLowerCase()),
                    getBuilder().notEqual(from.get("type"), FolderType.REMOTE)
            );
            query.orderBy(getBuilder().desc(from.get("creationTime")));
            return currentSession().createQuery(query).list();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve folders!", e);
        }
    }

    public List<Folder> getFoldersByType(FolderType type) {
        try {
            CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
            Root<Folder> from = query.from(Folder.class);
            query.where(getBuilder().equal(from.get("type"), type));
            return currentSession().createQuery(query).list();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
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
    public List<Folder> getCanEditFolders(Account account, Set<Group> accountGroups) {
        try {
            CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
            Root<Permission> from = query.from(Permission.class);
            Join<Permission, Folder> folder = from.join("folder");

            // where ((account = account or group in groups) and canWrite)) or is owner
            Predicate predicate = getBuilder().and(
                    getBuilder().or(
                            getBuilder().equal(from.get("account"), account),
                            from.get("group").in(accountGroups)
                    ),
                    getBuilder().equal(from.get("canWrite"), true),
                    getBuilder().isNotNull(from.get("folder"))
            );

            query.select(folder).where(predicate);
            return currentSession().createQuery(query).list();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Long> getCanReadFolderIds(Account account, Set<Group> accountGroups) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);
            Join<Permission, Folder> folder = from.join("folder");

            // where ((account = account or group in groups) and canWrite)) or is owner
            Predicate predicate = getBuilder().and(
                    getBuilder().or(
                            getBuilder().equal(from.get("account"), account),
                            from.get("group").in(accountGroups)
                    ),
                    getBuilder().or(
                            getBuilder().equal(from.get("canWrite"), true),
                            getBuilder().equal(from.get("canRead"), true)
                    ),
                    getBuilder().isNotNull(from.get("folder"))
            );

            query.select(folder.get("id")).where(predicate);
            return currentSession().createQuery(query).list();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Folder> filterByName(String token, int limit) {
        try {
            CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
            Root<Folder> from = query.from(Folder.class);
            query.where(getBuilder().like(getBuilder().lower(from.get("name")), "%" + token.toLowerCase() + "%"));
            return currentSession().createQuery(query).setMaxResults(limit).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve the list of ids of entries contained in a folder. Using this method is much faster (especially for
     * larger number of entries in a folder) that iterating through all the entries of a folder
     *
     * @param folder folder whose entries are being retrieved
     * @return list of ids of entries in a folder
     */
    public List<Long> getEntryIds(Folder folder) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Folder> from = query.from(Folder.class);
            Join<Folder, Entry> entry = from.join("contents");
            query.select(entry.get("id")).where(getBuilder().equal(from, folder));
            return currentSession().createQuery(query).list();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Entry> getEntrysByFolderType(FolderType type, ColumnField field, boolean asc, int start, int limit, String filter) {
        try {
            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Folder> from = query.from(Folder.class);
            Join<Folder, Entry> entry = from.join("contents");
            List<Predicate> predicates = getPredicates(entry, filter);
            predicates.add(getBuilder().equal(from.get("type"), type));

            query.select(entry).where(predicates.toArray(new Predicate[0]));
            String sortString = EntryAccessorUtilities.columnFieldToString(field);

            query.orderBy(asc ? getBuilder().asc(entry.get(sortString)) : getBuilder().desc(entry.get(sortString)));
            return currentSession().createQuery(query).setFirstResult(start).setMaxResults(limit).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public long getEntryCountByFolderType(FolderType type, String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Folder> from = query.from(Folder.class);
            Join<Folder, Entry> entry = from.join("contents");
            List<Predicate> predicates = getPredicates(entry, filter);
            predicates.add(getBuilder().equal(from.get("type"), type));

            query.select(getBuilder().countDistinct(entry.get("id")));
            query.where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }
}
