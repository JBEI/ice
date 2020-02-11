package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.*;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * DAO to manipulate {@link Entry} objects in the database.
 *
 * @author Hector Plahar
 */
public class EntryDAO extends HibernateRepository<Entry> {

    /**
     * Retrieve an {@link Entry} object from the database by id.
     *
     * @param id unique local identifier for entry record (typically synthetic database id)
     * @return Entry entry record associated with id
     * @throws DAOException on hibernate exception
     */
    @Override
    public Entry get(long id) {
        return super.get(Entry.class, id);
    }

    public String getEntrySummary(long id) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Entry> from = query.from(Entry.class);
            query.select(from.get("shortDescription")).where(getBuilder().equal(from.get("id"), id));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<String> getMatchingPlasmidField(AutoCompleteField field, String token, int limit) {
        String fieldString;
        switch (field) {
            case ORIGIN_OF_REPLICATION:
            default:
                fieldString = "originOfReplication";
                break;

            case PROMOTERS:
                fieldString = "promoters";
                break;

            case REPLICATES_IN:
                fieldString = "replicatesIn";
                break;
        }

        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Plasmid> from = query.from(Plasmid.class);
            query
                    .select(from.get(fieldString))
                    .where(getBuilder().like(getBuilder().lower(from.get(fieldString)), token.toLowerCase() + "%"));
            return currentSession().createQuery(query).setMaxResults(limit).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<String> getMatchingEntryPartNumbers(String token, int limit, Set<String> include) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Entry> from = query.from(Entry.class);

            ArrayList<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().like(getBuilder().lower(from.get("partNumber")), "%" + token.toLowerCase() + "%"));
            if (include != null && !include.isEmpty()) {
                predicates.add(from.get("recordType").in(include));
            }
            query.select(from.get("partNumber")).distinct(true).where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).setMaxResults(limit).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    private Entry getEntryByField(String field, String fieldValue) {
        try {
            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Entry> from = query.from(Entry.class);
            query.where(getBuilder().equal(from.get(field), fieldValue));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entry by: " + fieldValue, e);
        }
    }

    /**
     * Retrieve an {@link Entry} object in the database by recordId field.
     *
     * @param recordId unique global identifier for entry record (typically UUID)
     * @return Entry entry record associated with recordId
     * @throws DAOException on hibernate exception
     */
    public Entry getByRecordId(String recordId) {
        if (recordId == null)
            return null;
        return getEntryByField("recordId", recordId);
    }

    /**
     * Retrieve an {@link Entry} by it's part number.
     * <p>
     * If multiple Entries exist with the same part number, this method throws an exception.
     *
     * @param partNumber part number associated with entry
     * @return Entry
     * @throws DAOException on hibernate exception
     */
    public Entry getByPartNumber(String partNumber) {
        return getEntryByField("partNumber", partNumber);
    }

    /**
     * Retrieve an {@link Entry} by it's name. Note that name is not a unique field
     * so this could return more than one entry
     *
     * @param name name associated with entry
     * @return Entry.
     * @throws DAOException on hibernate exception
     */
    public List<Entry> getByName(String name) {
        try {
            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Entry> from = query.from(Entry.class);
            query.where(
                    getBuilder().equal(getBuilder().lower(from.get("name")), name.toLowerCase()),
                    getBuilder().equal(from.get("visibility"), Visibility.OK.getValue()));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entry by name: " + name, e);
        }
    }

    /**
     * Retrieve {@link Entry Entries} visible to everyone.
     *
     * @return Number of visible entries.
     * @throws DAOException on hibernate exception
     */
    public List<Long> retrieveVisibleEntries(Account account, Set<Group> groups, ColumnField sortField, boolean asc,
                                             int start, int count, String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            Join<Entry, Permission> entryPermission = from.join("permissions");

            ArrayList<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().equal(from.get("visibility"), Visibility.OK.getValue()));

            String fieldName = EntryAccessorUtilities.columnFieldToString(sortField);

            if (account != null) {
                predicates.add(getBuilder().or(
                        getBuilder().equal(entryPermission.get("account"), account),
                        entryPermission.get("group").in(groups)
                ));
            } else if (!groups.isEmpty()) {
                predicates.add(entryPermission.get("group").in(groups));
            }

            // check filter
            createFilterPredicate(from, filter, predicates);

            query.select(from.get("id")).where(predicates.toArray(new Predicate[0]));
            query.orderBy(asc ? getBuilder().asc(from.get(fieldName)) : getBuilder().desc(from.get(fieldName)));
            return currentSession().createQuery(query).setMaxResults(count).setFirstResult(start).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // todo : or entry is in a folder that is public
    public long visibleEntryCount(Account account, Set<Group> groups, String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);
            Join<Permission, Entry> entry = from.join("entry");

            ArrayList<Predicate> predicates = new ArrayList<>();

            // expect everyone to at least belong to the everyone group so groups should never be empty
            Predicate predicate = getBuilder().or(from.get("group").in(groups));
            if (account != null) {
                predicate.getExpressions().add(getBuilder().equal(from.get("account"), account));
                predicate.getExpressions().add(getBuilder().equal(entry.get("ownerEmail"), account.getEmail()));
            }
            predicates.add(predicate);

            if (filter != null && !filter.trim().isEmpty()) {
                filter = filter.toLowerCase();
                predicates.add(getBuilder().or(
                        getBuilder().like(getBuilder().lower(entry.get("name")), "%" + filter + "%"),
                        getBuilder().like(getBuilder().lower(entry.get("alias")), "%" + filter + "%")
                ));
            }
            predicates.add(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));
            query.select(getBuilder().countDistinct(entry.get("id"))).where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    private void checkAddFilter(List<Predicate> predicates, Root<Entry> root, String filter) {
        if (filter == null || filter.trim().isEmpty())
            return;

        filter = filter.toLowerCase();
        predicates.add(getBuilder().or(
                getBuilder().like(getBuilder().lower(root.get("name")), "%" + filter + "%"),
                getBuilder().like(getBuilder().lower(root.get("alias")), "%" + filter + "%"),
                getBuilder().like(getBuilder().lower(root.get("partNumber")), "%" + filter + "%")
                )
        );
    }

    private void createFilterPredicate(Path<Entry> entry, String filter, List<Predicate> predicates) {
        if (filter != null && !filter.trim().isEmpty()) {
            filter = filter.toLowerCase();
            predicates.add(getBuilder().or(
                    getBuilder().like(getBuilder().lower(entry.get("name")), "%" + filter + "%"),
                    getBuilder().like(getBuilder().lower(entry.get("alias")), "%" + filter + "%"),
                    getBuilder().like(getBuilder().lower(entry.get("partNumber")), "%" + filter + "%")
                    )
            );
        }
    }

    /**
     * Generate CriteriaQuery for shared entries
     *
     * @param requester     account of user making request
     * @param accountGroups groups of user making request
     * @param filter        filter string
     * @param isCount       set to true, if generating query for a count, false if query is to retrieve objects
     * @param sort          entry id retrieve sort order; ignored if <code>isCount</code> is true
     * @param asc           entry id sort asc or desc
     * @return generated criteria query
     */
    private CriteriaQuery<Long> createSharedEntryQuery(Account requester, Set<Group> accountGroups, String filter,
                                                       boolean isCount, ColumnField sort, boolean asc) {
        CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
        Root<Permission> root = query.from(Permission.class);

        Predicate predicate = getBuilder().or(getBuilder().equal(root.get("account"), requester));
        if (!accountGroups.isEmpty()) {
            predicate.getExpressions().add(root.get("group").in(accountGroups));
        }

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(predicate);

        // not a bulk upload permission
        predicates.add(getBuilder().isNotNull(root.get("entry")));

        Join<Permission, Entry> entry = root.join("entry");
        predicates.add(getBuilder().notEqual(entry.get("ownerEmail"), requester.getEmail()));
        predicates.add(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));

        createFilterPredicate(entry, filter, predicates);

        if (isCount) {
            query.select(getBuilder().countDistinct(entry.get("id"))).where(predicates.toArray(new Predicate[0]));
        } else {
            query.select(entry.get("id")).where(predicates.toArray(new Predicate[0])).distinct(true);
            String fieldName = sort == ColumnField.CREATED ? "id" : EntryAccessorUtilities.columnFieldToString(sort);
            query.orderBy(asc ? getBuilder().asc(entry.get(fieldName)) : getBuilder().desc(entry.get(fieldName)));
        }
        return query;
    }

    /**
     * An entry is shared if requester has explicit read or write permissions of belongs
     * to a group that have explicit read or write permissions
     *
     * @param requester     account that entries are shared with
     * @param accountGroups groups that account belongs to
     * @return number of entries that have been shared with user
     */
    public long sharedEntryCount(Account requester, Set<Group> accountGroups, String filter) {
        try {
            CriteriaQuery<Long> query = createSharedEntryQuery(requester, accountGroups, filter, true, null, false);
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // retrieves list of entries based on the paging parameters and the different ways entries can be shared
    public List<Long> sharedWithUserEntries(Account requester, Set<Group> accountGroups, ColumnField sort,
                                            boolean asc, int start, int limit, String filter) {
        try {
            CriteriaQuery<Long> query = createSharedEntryQuery(requester, accountGroups, filter, false, sort, asc);
            return currentSession().createQuery(query).setMaxResults(limit).setFirstResult(start).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves the entries for the specified owner, that the requester has read access to
     *
     * @param requester       account for user making request. This is used to check for access controls
     * @param owner           user id of entries' owner
     * @param requesterGroups groups that the requester is a member of. Used to check access permissions
     * @param sortField       field for sort
     * @param asc             sort order
     * @param start           index to start retrieving records from
     * @param limit           maximum number of entries to retrieve
     * @return list of entries matching specified criteria
     * @throws DAOException on HibernateException
     */
    public List<Long> retrieveUserEntries(Account requester, String owner, Set<Group> requesterGroups,
                                          ColumnField sortField, boolean asc, int start, int limit, String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);
            Join<Permission, Entry> join = from.join("entry");

            query.select(join.get("id"));

            ArrayList<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().or(
                    from.get("group").in(requesterGroups),
                    getBuilder().equal(from.get("account"), requester)
            ));
            predicates.add(getBuilder().equal(join.get("visibility"), Visibility.OK.getValue()));
            predicates.add(getBuilder().equal(join.get("ownerEmail"), owner));

            createFilterPredicate(join, filter, predicates);

            query.where(predicates.toArray(new Predicate[0]));
            String fieldName = sortField == ColumnField.CREATED ? "id" : EntryAccessorUtilities.columnFieldToString(sortField);
            query.orderBy(asc ? getBuilder().asc(join.get(fieldName)) : getBuilder().desc(join.get(fieldName)));
            return currentSession().createQuery(query).setMaxResults(limit).setFirstResult(start).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Long> sharedWithUserEntryIds(Account account, Set<Group> groups) {
        try {
            ArrayList<Predicate> predicates = new ArrayList<>();
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> root = query.from(Permission.class);
            Join<Permission, Entry> entry = root.join("entry");
            query.select(entry.get("id")).distinct(true);

            Predicate predicate = getBuilder().or(getBuilder().equal(root.get("account"), account));
            if (!groups.isEmpty()) {
                predicate.getExpressions().add(root.get("group").in(groups));
            }
            predicates.add(predicate);

            // not a bulk upload permission
            predicates.add(getBuilder().isNotNull(root.get("entry")));
            predicates.add(getBuilder().notEqual(entry.get("ownerEmail"), account.getEmail()));
            predicates.add(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));
            query.where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve {@link Entry} objects of the given list of ids.
     *
     * @param ids list of ids to retrieve
     * @return ArrayList of Entry objects.
     * @throws DAOException
     */
    public List<Entry> getEntriesByIdSet(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Entry> from = query.from(Entry.class);
            query.where(from.get("id").in(ids)).orderBy(getBuilder().desc(from.get("id")));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entries!", e);
        }
    }

    @Override
    public Entry create(Entry entry) {
        try {
            entry = super.create(entry);
            if (entry == null)
                throw new DAOException("Could not save entry");

            // partNumber
            String partNumberPrefix = EntryUtil.getPartNumberPrefix();
            String formatted = String.format("%06d", entry.getId());
            entry.setPartNumber(partNumberPrefix + formatted);
            return update(entry);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public void generateNextStrainNameForEntry(Entry entry, String prefix) {
        try {

            String nextName = prefix + String.format("%06d", entry.getId());
            entry.setName(nextName);
            update(entry);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Long> getByVisibility(String ownerEmail, Visibility visibility, ColumnField field, boolean asc,
                                      int start, int limit, String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            ArrayList<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().equal(from.get("visibility"), visibility.getValue()));

            if (ownerEmail != null) {
                predicates.add(getBuilder().equal(from.get("ownerEmail"), ownerEmail));
            }

            checkAddFilter(predicates, from, filter);
            String fieldName = EntryAccessorUtilities.columnFieldToString(field);
            query.select(from.get("id"));
            query.where(predicates.toArray(new Predicate[0]))
                    .orderBy(asc ? getBuilder().asc(from.get(fieldName)) : getBuilder().desc(from.get(fieldName)));
            return currentSession().createQuery(query).setFirstResult(start).setMaxResults(limit).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public long getByVisibilityCount(String ownerEmail, Visibility visibility, String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (ownerEmail != null)
                predicates.add(getBuilder().equal(from.get("ownerEmail"), ownerEmail));
            predicates.add(getBuilder().equal(from.get("visibility"), visibility.getValue()));
            checkAddFilter(predicates, from, filter);
            query.select(getBuilder().countDistinct(from.get("id")))
                    .where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public long getByVisibilityCount(Visibility visibility) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            query.select(getBuilder().countDistinct(from.get("id")))
                    .where(getBuilder().equal(from.get("visibility"), visibility.getValue()));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SafeVarargs
    private final List<Long> getResultList(int start, int limit, ColumnField sort, boolean asc, String filter,
                                           Map<String, String>... predicatesMap) {
        if (sort == null)
            sort = ColumnField.CREATED;

        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().or(
                    getBuilder().equal(from.get("visibility"), Visibility.OK.getValue()),
                    getBuilder().equal(from.get("visibility"), Visibility.PENDING.getValue())
            ));

            for (Map<String, String> predicate : predicatesMap) {
                for (Map.Entry<String, String> keySet : predicate.entrySet()) {
                    predicates.add(getBuilder().equal(from.get(keySet.getKey()), keySet.getValue()));
                }
            }

            query.select(from.get("id"));
            checkAddFilter(predicates, from, filter);
            query.where(predicates.toArray(new Predicate[0]));
            String fieldName = EntryAccessorUtilities.columnFieldToString(sort);
            query.orderBy(asc ? getBuilder().asc(from.get(fieldName)) : getBuilder().desc(from.get(fieldName)));
            return currentSession().createQuery(query).setMaxResults(limit).setFirstResult(start).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he.getMessage());
        }
    }

    public List<Long> retrieveAllEntryIds(ColumnField sort, boolean asc, int start, int limit, String filter) {
        return getResultList(start, limit, sort, asc, filter);
    }

    /**
     * Retrieves entry ids owned by account with specified email and with visibility of "pending" or "ok"
     *
     * @param ownerEmail email for account whose entries are to be retrieved
     * @param sort       field to sort results on
     * @param asc        sort order
     * @param start      start of retrieve
     * @param limit      maximum number of records to retrieve from
     * @param filter     filter for entries
     * @return list of matching entries
     * @throws DAOException on Hibernate Exception
     */
    public List<Long> retrieveOwnerEntries(String ownerEmail, ColumnField sort, boolean asc, int start,
                                           int limit, String filter) {
        Map<String, String> predicatesMap = new HashMap<>();
        predicatesMap.put("ownerEmail", ownerEmail);
        return getResultList(start, limit, sort, asc, filter, predicatesMap);
    }

    private List<Predicate> getOwnerPredicate(Root<Entry> from, String ownerEmail) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(getBuilder().equal(from.get("ownerEmail"), ownerEmail));
        predicates.add(getBuilder().or(
                getBuilder().equal(from.get("visibility"), Visibility.OK.getValue()),
                getBuilder().equal(from.get("visibility"), Visibility.PENDING.getValue())
        ));
        return predicates;
    }

    /**
     * Retrieves list of entry ids whose owner email column matches the specified ownerEmail parameter,
     * with a visibility of <pre>OK</pre> or <pre>PENDING</pre> and if not null, matches the type
     *
     * @param ownerEmail value of owner email column that desired entries must match
     * @param type       option entry type parameter
     * @return list of ids for all matching entries
     */
    public List<Long> getOwnerEntryIds(String ownerEmail, EntryType type) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            List<Predicate> predicates = getOwnerPredicate(from, ownerEmail);

            if (type != null)
                predicates.add(getBuilder().equal(from.get("recordType"), type.getName()));

            query.select(from.get("id")).where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // todo : replace with hibernate search
    public List<Long> getVisibleEntryIds(boolean admin, Group publicGroup) {
        try {
            if (admin) {
                CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
                Root<Entry> from = query.from(Entry.class);
                query.select(getBuilder().countDistinct(from.get("id")))
                        .where(getBuilder().or(
                                getBuilder().equal(from.get("visibility"), Visibility.OK.getValue()),
                                getBuilder().equal(from.get("visibility"), Visibility.PENDING.getValue())
                        ));
                return currentSession().createQuery(query).list();
            }

            // non admins, check permissions
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);
            Join<Permission, Entry> permissionEntry = from.join("entry");

            query.select(permissionEntry.get("id")).where(
//                    getBuilder().equal(from.get("entry"), permissionEntry)
                    getBuilder().equal(from.get("group"), publicGroup),
                    getBuilder().equal(permissionEntry.get("visibility"), Visibility.OK.getValue())
            );
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * @return number of entries that have visibility of "OK"
     */
    public long getAllEntryCount(String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            ArrayList<Predicate> predicates = new ArrayList<>();
            checkAddFilter(predicates, from, filter);
            predicates.add(getBuilder().or(
                    getBuilder().equal(from.get("visibility"), Visibility.OK.getValue()),
                    getBuilder().equal(from.get("visibility"), Visibility.PENDING.getValue())
            ));
            query.where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // does not check permissions (includes pending entries)
    public long ownerEntryCount(String ownerEmail) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            List<Predicate> predicates = getOwnerPredicate(from, ownerEmail);
            query.select(getBuilder().countDistinct(from.get("id"))).where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // checks permission, does not include pending entries
    public long ownerEntryCount(Account requester, String ownerEmail, Set<Group> accountGroups) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);
            Join<Permission, Entry> entry = from.join("entry");
            Predicate predicate = getBuilder().or(getBuilder().equal(from.get("account"), requester));
            if (!accountGroups.isEmpty())
                predicate.getExpressions().add(from.get("group").in(accountGroups));

            ArrayList<Predicate> predicates = new ArrayList<>();
            predicates.add(predicate);

            predicates.add(getBuilder().or(
                    getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()),
                    getBuilder().isNull(entry.get("visibility"))
            ));
            predicates.add(getBuilder().equal(entry.get("ownerEmail"), ownerEmail));
            query.select(getBuilder().countDistinct(entry.get("id"))).where(predicates.toArray(new Predicate[0]));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // experimental. do not use
    //         delete from bulk upload entry
    public void fullDelete(Entry entry) {
        try {
            String hql = "delete from bulk_upload_entry where entry_id=" + entry.getId();
            if (currentSession().createNativeQuery(hql).executeUpdate() > 0)
                delete(entry);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * links are stored in a join table in the form [entry_id, linked_entry_id] which is used
     * to represent a parent child reln. This method returns the parents in the reln
     */
    public List<Entry> getParents(long entryId) {
        try {
            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Entry> from = query.from(Entry.class);
            Join<Entry, Entry> linked = from.join("linkedEntries");
            query.where(getBuilder().equal(linked.get("id"), entryId));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getDeletedCount(String ownerUserId) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(getBuilder().equal(from.get("ownerEmail"), ownerUserId),
                    getBuilder().equal(from.get("visibility"), Visibility.DELETED.getValue()));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int setEntryVisibility(List<Long> list, Visibility ok) {
        try {
            CriteriaUpdate<Entry> update = getBuilder().createCriteriaUpdate(Entry.class);
            Root<Entry> from = update.from(Entry.class);
            update.set(from.get("visibility"), ok.getValue());
            update.where(from.get("id").in(list));
            return currentSession().createQuery(update).executeUpdate();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<String> getRecordTypes(List<Long> list) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Entry> from = query.from(Entry.class);
            query.select(from.get("recordType")).where(from.get("id").in(list));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Long> filterByUserId(String userId, List<Long> entries) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            query.select(from.get("id"))
                    .where(getBuilder().equal(from.get("ownerEmail"), userId), from.get("id").in(entries));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Entry> getMatching(String name, String alias, EntryType type) {
        try {
            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<Entry> from = query.from(Entry.class);
            query.where(
                    getBuilder().equal(from.get("name"), name),
                    getBuilder().equal(from.get("alias"), alias),
                    getBuilder().equal(from.get("recordType"), type.getName()));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }
}
