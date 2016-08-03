package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.*;

import java.util.*;

/**
 * DAO to manipulate {@link Entry} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv,
 */
@SuppressWarnings("unchecked")
public class EntryDAO extends HibernateRepository<Entry> {

    public String getEntrySummary(long id) throws DAOException {
        try {
            return (String) currentSession().createCriteria(Entry.class)
                    .add(Restrictions.eq("id", id))
                    .setProjection(Projections.property("shortDescription")).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<String> getMatchingSelectionMarkers(String token, int limit) throws DAOException {
        try {
            return currentSession().createCriteria(SelectionMarker.class)
                    .add(Restrictions.ilike("name", token, MatchMode.ANYWHERE))
                    .setMaxResults(limit)
                    .setProjection(Projections.distinct(Projections.property("name")))
                    .list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<String> getMatchingPlasmidField(AutoCompleteField field, String token, int limit) throws DAOException {
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
            return currentSession().createCriteria(Plasmid.class)
                    .add(Restrictions.ilike(fieldString, token, MatchMode.START))
                    .setMaxResults(limit)
                    .setProjection(Projections.distinct(Projections.property(fieldString)))
                    .list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<String> getMatchingEntryPartNumbers(String token, int limit, Set<String> include) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Entry.class)
                    .add(Restrictions.ilike("partNumber", token, MatchMode.ANYWHERE));
            criteria.setProjection(Projections.distinct(Projections.property("partNumber")));
            if (limit > 0)
                criteria.setMaxResults(limit);
            if (include != null && !include.isEmpty()) {
                criteria.add(Restrictions.in("recordType", include));
            }
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve an {@link Entry} object from the database by id.
     *
     * @param id unique local identifier for entry record (typically synthetic database id)
     * @return Entry entry record associated with id
     * @throws DAOException
     */
    public Entry get(long id) throws DAOException {
        return super.get(Entry.class, id);
    }

    /**
     * Retrieve an {@link Entry} object in the database by recordId field.
     *
     * @param recordId unique global identifier for entry record (typically UUID)
     * @return Entry entry record associated with recordId
     * @throws DAOException
     */
    public Entry getByRecordId(String recordId) throws DAOException {
        Session session = currentSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class).add(Restrictions.eq("recordId", recordId));
            Object object = criteria.uniqueResult();
            if (object != null) {
                return (Entry) object;
            }
            return null;
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entry by recordId: " + recordId, e);
        }
    }

    /**
     * Retrieve an {@link Entry} by it's part number.
     * <p>
     * If multiple Entries exist with the same part number, this method throws an exception.
     *
     * @param partNumber part number associated with entry
     * @return Entry
     * @throws DAOException
     */
    public Entry getByPartNumber(String partNumber) throws DAOException {
        Session session = currentSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class).add(Restrictions.eq("partNumber", partNumber));
            Object object = criteria.uniqueResult();
            if (object != null) {
                return (Entry) object;
            }
            return null;
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entry by partNumber: " + partNumber, e);
        }
    }

    /**
     * Retrieve an {@link Entry} by it's name. Note that name is not a unique field
     * so this could return more than one entry
     *
     * @param name name associated with entry
     * @return Entry.
     * @throws DAOException
     */
    public List<Entry> getByName(String name) throws DAOException {
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " where name=:name AND visibility=:v");
            query.setParameter("name", name);
            query.setParameter("v", Visibility.OK.getValue());
            return query.list();
        } catch (HibernateException e) {
            Logger.error("Failed to retrieve entry by name: " + name, e);
            throw new DAOException("Failed to retrieve entry by name: " + name, e);
        }
    }

    /**
     * Retrieve {@link Entry Entries} visible to everyone.
     *
     * @return Number of visible entries.
     * @throws DAOException on hibernate exception
     */
    public Set<Entry> retrieveVisibleEntries(Account account, Set<Group> groups, ColumnField sortField, boolean asc,
                                             int start, int count, String filter) throws DAOException {
        try {
            String fieldName = columnFieldToString(sortField);
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Entry.class)
                    .createAlias("permissions", "p")
                    .add(Restrictions.eq("visibility", Visibility.OK.getValue()));

            if (account != null) {
                detachedCriteria.add(Restrictions
                        .disjunction(Restrictions.in("p.group", groups), Restrictions.eq("p.account", account)));
            } else if (!groups.isEmpty()) {
                detachedCriteria.add(Restrictions.in("p.group", groups));
            }

            detachedCriteria.setProjection(Projections.distinct(Projections.id()));
            detachedCriteria.add(Restrictions.eq("visibility", Visibility.OK.getValue()));

            // check filter
            if (filter != null && !filter.trim().isEmpty()) {
                detachedCriteria.add(Restrictions.disjunction(
                        Restrictions.ilike("name", filter, MatchMode.ANYWHERE),
                        Restrictions.ilike("alias", filter, MatchMode.ANYWHERE),
                        Restrictions.ilike("partNumber", filter, MatchMode.ANYWHERE)
                ));
            }

            Criteria criteria = currentSession().createCriteria(Entry.class);
            criteria.add(Subqueries.propertyIn("id", detachedCriteria));

            criteria.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
            criteria.setFirstResult(start);
            criteria.setMaxResults(count);
            return new LinkedHashSet<>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // todo : or entry is in a folder that is public
    public long visibleEntryCount(Account account, Set<Group> groups, String filter) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.createAlias("entry", "entry");

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", groups));
        if (account != null) {
            disjunction.add(Restrictions.eq("account", account));

            // add entries account owns
            disjunction.add(Restrictions.eq("entry.ownerEmail", account.getEmail()));
        }

        if (filter != null && !filter.trim().isEmpty()) {
            criteria.add(Restrictions.disjunction(
                    Restrictions.ilike("entry.name", filter, MatchMode.ANYWHERE),
                    Restrictions.ilike("entry.alias", filter, MatchMode.ANYWHERE)
            ));
        }

        criteria.add(disjunction);
        criteria.add(Restrictions.eq("entry.visibility", Visibility.OK.getValue()));
        criteria.setProjection(Projections.countDistinct("entry.id"));
        Number rowCount = (Number) criteria.uniqueResult();
        return rowCount.longValue();
    }

    protected Criteria checkAddFilter(Criteria criteria, String filter, String criteriaAlias) {
        if (filter != null && !filter.trim().isEmpty()) {
            String name = (criteriaAlias == null) ? "name" : "name." + criteriaAlias;
            String alias = (criteriaAlias == null) ? "alias" : "alias." + criteriaAlias;
            String partNumber = (criteriaAlias == null) ? "partNumber" : "partNumber." + criteriaAlias;

            criteria.add(Restrictions.disjunction(
                    Restrictions.ilike(name, filter, MatchMode.ANYWHERE),
                    Restrictions.ilike(alias, filter, MatchMode.ANYWHERE),
                    Restrictions.ilike(partNumber, filter, MatchMode.ANYWHERE)
            ));
        }
        return criteria;
    }

    /**
     * Creates and returns a Criteria for the exclusive use of methods that are retrieving
     * entries that are shared with users
     *
     * @param account       account for user the entries are being shared with
     * @param accountGroups groups that account belongs to
     * @return criteria object for {@link Permission}
     */
    private Criteria getSharedWithUserCriteria(Account account, Set<Group> accountGroups) {
        Criteria criteria = currentSession().createCriteria(Permission.class);

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Disjunction disjunction = Restrictions.disjunction();
        if (!accountGroups.isEmpty())
            disjunction.add(Restrictions.in("group", accountGroups));

        // explicit share
        disjunction.add(Restrictions.eq("account", account));

        // not a bulk upload permission
        criteria.add(Restrictions.isNotNull("entry"));

        criteria.createAlias("entry", "entry")
                .add(Restrictions.ne("entry.ownerEmail", account.getEmail()));
        criteria.add(Restrictions.eq("entry.visibility", Visibility.OK.getValue()));
        criteria.add(disjunction);
        return criteria;
    }

    /**
     * An entry is shared if requester has explicit read or write permissions of belongs
     * to a group that have explicit read or write permissions
     *
     * @param requester     account that entries are shared with
     * @param accountGroups groups that account belongs to
     * @return number of entries that have been shared with user
     */
    public long sharedEntryCount(Account requester, Set<Group> accountGroups, String filter) throws DAOException {
        try {
            Criteria criteria = getSharedWithUserCriteria(requester, accountGroups);
            criteria.setProjection(Projections.countDistinct("entry.id"));
            checkAddFilter(criteria, filter, "entry");
            Number rowCount = (Number) criteria.uniqueResult();
            return rowCount.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // retrieves list of entries based on the paging parameters and the different ways entries can be shared
    public List<Entry> sharedWithUserEntries(Account requester, Set<Group> accountGroups, ColumnField sort,
                                             boolean asc, int start, int limit, String filter) throws DAOException {
        try {
            Criteria criteria = getSharedWithUserCriteria(requester, accountGroups);
            criteria.setProjection(Projections.property("entry"));
            String fieldName = sort == ColumnField.CREATED ? "entry.id" : columnFieldToString(sort);
            checkAddFilter(criteria, filter, "entry");
            criteria.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
            return new ArrayList<>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Long> sharedWithUserEntryIds(Account account, Set<Group> groups) {
        try {
            Criteria criteria = getSharedWithUserCriteria(account, groups);
            criteria.setProjection(Projections.distinct(Projections.property("entry.id")));
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves the entries for the specified owner, that the requester has read access to
     *
     * @param requester       account for user making request
     * @param owner           user id of entries' owner
     * @param requesterGroups groups that the requester is a member of. Used to check access permissions
     * @param sortField       field for sort
     * @param asc             sort order
     * @param start           index to start retrieving records from
     * @param limit           maximum number of entries to retrieve
     * @return list of entries matching specified criteria
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<Entry> retrieveUserEntries(Account requester, String owner, Set<Group> requesterGroups,
                                           ColumnField sortField, boolean asc, int start, int limit, String filter)
            throws DAOException {
        Criteria criteria = currentSession().createCriteria(Permission.class);
        criteria.setProjection(Projections.property("entry"));

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", requesterGroups));
        disjunction.add(Restrictions.eq("account", requester));

        criteria.add(disjunction);
        criteria.createAlias("entry", "entry");
        criteria.add(Restrictions.eq("entry.visibility", Visibility.OK.getValue()));
        criteria.add(Restrictions.eq("entry.ownerEmail", owner));

        // sort
        String fieldName = sortField == ColumnField.CREATED ? "entry.id" : "entry." + columnFieldToString(sortField);
        criteria.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
        checkAddFilter(criteria, filter, "entry");
        criteria.setFirstResult(start);
        criteria.setMaxResults(limit);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    /**
     * @return number of entries that have visibility of "OK"
     * @throws DAOException
     */
    public long getAllEntryCount(String filter) throws DAOException {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Entry.class.getName());
            criteria.add(Restrictions.eq("visibility", Visibility.OK.getValue()));
            checkAddFilter(criteria, filter, null);
            criteria.setProjection(Projections.countDistinct("id"));
            Number number = (Number) criteria.uniqueResult();
            return number.longValue();
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
    public List<Entry> getEntriesByIdSet(List<Long> ids) throws DAOException {
        if (ids == null || ids.isEmpty()) {
            return new LinkedList<>();
        }

        try {
            Query query = currentSession().createQuery(
                    "FROM " + Entry.class.getName() + " e WHERE e.id IN (:ids) order by id asc");
            ArrayList<Long> list = new ArrayList<>(ids.size());
            for (Number id : ids) {
                list.add(id.longValue());
            }
            query.setParameterList("ids", list);
            List result = query.list();
            return new LinkedList<>(result);
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entries!", e);
        }
    }

    @Override
    public Entry create(Entry entry) throws DAOException {
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

    public synchronized void generateNextStrainNameForEntry(Entry entry, String prefix) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Entry.class)
                .add(Restrictions.like("name", prefix + "1", MatchMode.START));
        criteria.addOrder(Order.desc("name"));
        criteria.setMaxResults(1);
        List list = criteria.list();
        int next = 0;
        if (!list.isEmpty()) {
            Entry resultEntry = (Entry) list.get(0);
            String name = resultEntry.getName();
            next = Integer.decode(name.split(prefix)[1]);
        }
        next += 1;
        String nextName = prefix + next;
        entry.setName(nextName);
        currentSession().update(entry);
    }

    @SuppressWarnings("unchecked")
    public List<Entry> getByVisibility(String ownerEmail, Visibility visibility, ColumnField field, boolean asc,
                                       int start, int limit, String filter) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Entry.class)
                    .add(Restrictions.eq("visibility", visibility.getValue()));

            if (ownerEmail != null) {
                criteria.add(Restrictions.eq("ownerEmail", ownerEmail));
            }
            checkAddFilter(criteria, filter, null);
            String fieldName = columnFieldToString(field);
            criteria.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(start);
            return new LinkedList<>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public long getByVisibilityCount(String ownerEmail, Visibility visibility, String filter) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Entry.class);
        if (ownerEmail != null)
            criteria = criteria.add(Restrictions.eq("ownerEmail", ownerEmail));
        criteria.add(Restrictions.eq("visibility", visibility.getValue()));
        checkAddFilter(criteria, filter, null);
        return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    public long getByVisibilityCount(Visibility visibility) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Entry.class)
                .add(Restrictions.eq("visibility", visibility.getValue()));
        return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    protected String columnFieldToString(ColumnField field) {
        if (field == null)
            return "creationTime";

        switch (field) {
            case TYPE:
                return "recordType";

            case STATUS:
                return "status";

            case PART_ID:
                return "partNumber";

            case NAME:
                return "name";

            case ALIAS:
                return "alias";

            case SUMMARY:
                return "shortDescription";

            case CREATED:
            default:
                return "creationTime";
        }
    }

    /**
     * Retrieves entries owned by account with specified email and with visibility of "pending" or "ok"
     *
     * @param ownerEmail email for account whose entries are to be retrieved
     * @param sort       field to sort results on
     * @param asc        sort order
     * @param start      start of retrieve
     * @param limit      maximum number of records to retrieve from
     * @param filter     filter for entries
     * @return list of matching entries
     * @throws DAOException
     */
    public List<Entry> retrieveOwnerEntries(String ownerEmail, ColumnField sort, boolean asc, int start,
                                            int limit, String filter) throws DAOException {
        try {
            String fieldName = columnFieldToString(sort);
            Criteria criteria = currentSession().createCriteria(Entry.class)
                    .add(Restrictions.disjunction()
                            .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                            .add(Restrictions.eq("visibility", Visibility.PENDING.getValue())));
            criteria.add(Restrictions.eq("ownerEmail", ownerEmail));
            if (filter != null && filter.trim().length() != 0) {
                criteria.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("name", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("alias", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("partNumber", filter, MatchMode.ANYWHERE)));
            }
            criteria.setMaxResults(limit);
            criteria.setFirstResult(start);
            criteria.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
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
        Criteria criteria = currentSession().createCriteria(Entry.class)
                .add(Restrictions.eq("ownerEmail", ownerEmail));

        criteria.add(Restrictions.disjunction()
                .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                .add(Restrictions.eq("visibility", Visibility.PENDING.getValue())));

        if (type != null)
            criteria.add(Restrictions.eq("recordType", type.getName()));

        return criteria.setProjection(Projections.id()).list();
    }

    public List<Long> getVisibleEntryIds(boolean admin) {
        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Query query;

            if (admin) {
                query = session.createQuery("SELECT e.id FROM Entry e WHERE (visibility IS NULL OR visibility = " +
                        Visibility.OK.getValue() + " OR visibility = " + Visibility.PENDING.getValue() + ")");
            } else {
                query = session.createQuery("SELECT DISTINCT e.id FROM Entry e, Permission p" +
                        " WHERE p.group = :group AND e = p.entry AND e.visibility = :v");
                query.setParameter("group", new GroupController().createOrRetrievePublicGroup());
                query.setParameter("v", Visibility.OK.getValue());
            }

            return query.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Set<Entry> retrieveAllEntries(ColumnField sort, boolean asc, int start, int limit, String filter)
            throws DAOException {
        try {
            if (sort == null)
                sort = ColumnField.CREATED;

            String fieldName = columnFieldToString(sort);
            Criteria criteria = currentSession().createCriteria(Entry.class)
                    .add(Restrictions.eq("visibility", Visibility.OK.getValue()));
            checkAddFilter(criteria, filter, null);
            criteria.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(start);
            return new LinkedHashSet<>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // does not check permissions (includes pending entries)
    public long ownerEntryCount(String ownerEmail) throws DAOException {
        Session session = currentSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class)
                    .add(Restrictions.eq("ownerEmail", ownerEmail));
            criteria.add(Restrictions.disjunction()
                    .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                    .add(Restrictions.eq("visibility", Visibility.PENDING.getValue())));
            criteria.setProjection(Projections.rowCount());
            Number rowCount = (Number) criteria.uniqueResult();
            return rowCount.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // checks permission, does not include pending entries
    public long ownerEntryCount(Account requester, String ownerEmail, Set<Group> accountGroups) throws DAOException {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Permission.class);
            criteria.setProjection(Projections.property("entry"));

            Junction disjunction = Restrictions.disjunction();
            if (!accountGroups.isEmpty())
                disjunction.add(Restrictions.in("group", accountGroups));
            disjunction.add(Restrictions.eq("account", requester));

            criteria.add(disjunction);
            criteria.add(Restrictions.disjunction()
                    .add(Restrictions.eq("canWrite", true))
                    .add(Restrictions.eq("canRead", true)));

            Criteria entryCriteria = criteria.createCriteria("entry");
            entryCriteria.add(Restrictions.disjunction()
                    .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                    .add(Restrictions.isNull("visibility")));
            entryCriteria.add(Restrictions.eq("ownerEmail", ownerEmail));
            criteria.setProjection(Projections.rowCount());
            Number rowCount = (Number) criteria.uniqueResult();
            return rowCount.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // experimental. do not use
    public void fullDelete(Entry entry) throws DAOException {
        // delete from bulk upload entry
        String hql = "delete from bulk_upload_entry where entry_id=" + entry.getId();
        currentSession().createSQLQuery(hql).executeUpdate();
        delete(entry);
    }

    /**
     * links are stored in a join table in the form [entry_id, linked_entry_id] which is used
     * to represent a parent child reln. This method returns the parents in the reln
     */
    public List<Entry> getParents(long entryId) throws DAOException {
        return currentSession().createCriteria(Entry.class)
                .createAlias("linkedEntries", "link")
                .add(Restrictions.eq("link.id", entryId)).list();
    }

    public int getDeletedCount(String ownerUserId) {
        Number itemCount = (Number) currentSession()
                .createCriteria(Entry.class)
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("ownerEmail", ownerUserId))
                .add(Restrictions.eq("visibility", Visibility.DELETED.getValue()))
                .uniqueResult();
        return itemCount.intValue();
    }

    public int setEntryVisibility(List<Long> list, Visibility ok) {
        if (list.isEmpty())
            return 0;

        Query query = currentSession().createQuery("update " + Entry.class.getName()
                + " e set e.visibility=:v where e.id in :ids");
        query.setParameter("v", ok.getValue());
        query.setParameterList("ids", list);
        return query.executeUpdate();
    }

    public List<String> getRecordTypes(List<Long> list) {
        if (list.isEmpty())
            return new ArrayList<>();

        return currentSession().createCriteria(Entry.class)
                .add(Restrictions.in("id", list))
                .setProjection(Projections.distinct(Projections.property("recordType")))
                .list();
    }

    public List<Long> filterByUserId(String userId, List<Long> entries) {
        if (entries.isEmpty())
            return new ArrayList<>();

        return currentSession().createCriteria(Entry.class)
                .add(Restrictions.in("id", entries))
                .add(Restrictions.eq("ownerEmail", userId))
                .setProjection(Projections.distinct(Projections.property("id")))
                .list();
    }
}
