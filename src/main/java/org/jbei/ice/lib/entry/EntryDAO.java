package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.Visibility;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * DAO to manipulate {@link Entry} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv,
 */
public class EntryDAO extends HibernateRepository<Entry> {

    @SuppressWarnings("unchecked")
    public HashSet<Long> retrieveStrainsForPlasmid(Plasmid plasmid) throws DAOException {
        Set<PartNumber> plasmidPartNumbers = plasmid.getPartNumbers();
        Session session = currentSession();
        HashSet<Long> strainIds = null;
        try {
            for (PartNumber plasmidPartNumber : plasmidPartNumbers) {
                Query query = session
                        .createQuery("select strain.id from Strain strain where strain.plasmids like :partNumber");
                query.setString("partNumber", "%" + plasmidPartNumber.getPartNumber() + "%");
                strainIds = new HashSet<Long>(query.list());
            }
        } catch (HibernateException e) {
            Logger.error("Could not get strains for plasmid " + e.toString(), e);
            throw new DAOException(e);
        }
        return strainIds;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Long> getAllEntryIds() throws DAOException {
        Session session = currentSession();
        Criteria c = session.createCriteria(Entry.class).setProjection(Projections.id());
        return new LinkedList<Long>(c.list());
    }

    public Set<String> getMatchingSelectionMarkers(String token, int limit) throws DAOException {
        return getMatchingField("selectionMarker.name", "SelectionMarker selectionMarker", token, limit);
    }

    public Set<String> getMatchingOriginOfReplication(String token, int limit) throws DAOException {
        return getMatchingField("plasmid.originOfReplication", "Plasmid plasmid", token, limit);
    }

    public Set<String> getMatchingPromoters(String token, int limit) throws DAOException {
        return getMatchingField("plasmid.promoters", "Plasmid plasmid", token, limit);
    }

    @SuppressWarnings("unchecked")
    protected Set<String> getMatchingField(String field, String object, String token, int limit) throws DAOException {
        Session session = currentSession();
        try {
            token = token.toUpperCase();
            String queryString = "select distinct " + field + " from " + object + " where "
                    + " UPPER(" + field + ") like '%" + token + "%'";
            Query query = session.createQuery(queryString);
            if (limit > 0)
                query.setMaxResults(limit);
            HashSet<String> results = new HashSet<String>(query.list());
            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getMatchingPlasmidNames(String token, int limit) throws DAOException {
        try {
            String qString = "select distinct name.name from Plasmid plasmid inner join plasmid.names as name where " +
                    "name.name like '%" + token + "%' order by name.name asc";
            Query query = currentSession().createQuery(qString);
            if (limit > 0)
                query.setMaxResults(limit);

            HashSet<String> results = new HashSet<String>(query.list());
            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Save {@link FundingSource} object into the database.
     *
     * @param fundingSource funding source to save
     * @return Saved FundingSource object.
     * @throws DAOException
     */
    public FundingSource saveFundingSource(FundingSource fundingSource) throws DAOException {
        Session session = currentSession();
        if (fundingSource.getFundingSource() == null)
            fundingSource.setFundingSource("");

        try {
            session.saveOrUpdate(fundingSource);
            return fundingSource;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    protected FundingSource getExistingFundingSource(FundingSource fundingSource) throws DAOException {
        if (fundingSource == null)
            return null;

        String pI = fundingSource.getPrincipalInvestigator();
        if (pI == null)
            pI = "";
        String source = fundingSource.getFundingSource();
        String queryString = "from " + FundingSource.class.getName()
                + " where fundingSource=:fundingSource AND principalInvestigator=:principalInvestigator";
        Query query = currentSession().createQuery(queryString);
        query.setParameter("fundingSource", source);
        query.setParameter("principalInvestigator", pI);
        List result = query.list();
        if (!result.isEmpty()) {
            if (result.size() > 1)
                Logger.warn("Duplicate funding source found for (" + pI + ", " + source + ")");
            return (FundingSource) result.get(0);
        } else
            return null;
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
     * <p/>
     * If multiple Entries exist with the same part number, this method throws an exception.
     *
     * @param partNumber part number associated with entry
     * @return Entry
     * @throws DAOException
     */
    public Entry getByPartNumber(String partNumber) throws DAOException {
        Entry entry = null;

        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + PartNumber.class.getName()
                                                      + " where partNumber = :partNumber");
            query.setParameter("partNumber", partNumber);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                entry = ((PartNumber) queryResult).getEntry();
            }
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entry by partNumber: " + partNumber, e);
        }

        return entry;
    }

    /**
     * Retrieve an {@link Entry} by it's name.
     *
     * @param name name associated with entry
     * @return Entry.
     * @throws DAOException
     */
    public Entry getByName(String name) throws DAOException {
        Entry entry;
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + Name.class.getName() + " where name = :name");
            query.setParameter("name", name);
            Object queryResult = query.uniqueResult();
            if (queryResult == null) {
                return null;
            }

            entry = ((Name) queryResult).getEntry();
        } catch (HibernateException e) {
            Logger.error("Failed to retrieve entry by name: " + name, e);
            throw new DAOException("Failed to retrieve entry by name: " + name, e);
        }

        return entry;
    }

    /**
     * Retrieve the number of {@link Entry Entries} visible to everyone.
     *
     * @return Number of visible entries.
     * @throws DAOException
     */

    @SuppressWarnings({"unchecked"})
    public LinkedList<Entry> retrieveVisibleEntries(Account account, Set<Group> groups,
            ColumnField sortField, boolean asc, int start, int count) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.setProjection(Projections.property("entry"));

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", groups));
        if (account != null) {
            disjunction.add(Restrictions.eq("account", account));
        }

        criteria.add(disjunction);
        criteria.add(Restrictions.disjunction()
                                 .add(Restrictions.eq("canWrite", true))
                                 .add(Restrictions.eq("canRead", true)));

        Criteria entryC = criteria.createCriteria("entry");
        entryC.add(Restrictions.disjunction()
                               .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                               .add(Restrictions.isNull("visibility")));

        // sort
        if (sortField == null)
            sortField = ColumnField.CREATED;

        String fieldName;
        switch (sortField) {
            case TYPE:
                fieldName = "recordType";
                break;

            case STATUS:
                fieldName = "status";
                break;

            case CREATED:
            default:
                fieldName = "creationTime";
                break;
        }

        entryC.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
        entryC.setFirstResult(start);
        entryC.setMaxResults(count);
        entryC.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new LinkedList<Entry>(entryC.list());
    }

    public long visibleEntryCount(Account account, Set<Group> groups) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class);

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", groups));
        disjunction.add(Restrictions.eq("account", account));

        criteria.add(disjunction);
        criteria.add(Restrictions.disjunction()
                                 .add(Restrictions.eq("canWrite", true))
                                 .add(Restrictions.eq("canRead", true)));

        Criteria entryCriteria = criteria.createCriteria("entry");
        entryCriteria.add(Restrictions.disjunction()
                                      .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                                      .add(Restrictions.isNull("visibility")));

        entryCriteria.setProjection(Projections.countDistinct("id"));
        Number rowCount = (Number) entryCriteria.uniqueResult();
        return rowCount.longValue();
    }

    // checks permission (does not include pending entries)
    @SuppressWarnings("unchecked")
    public List<Entry> retrieveUserEntries(Account requestor, String user, Set<Group> groups,
            ColumnField sortField, boolean asc, int start, int limit) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.setProjection(Projections.property("entry"));

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", groups));
        disjunction.add(Restrictions.eq("account", requestor));

        criteria.add(disjunction);
        criteria.add(Restrictions.disjunction()
                                 .add(Restrictions.eq("canWrite", true))
                                 .add(Restrictions.eq("canRead", true)));

        Criteria entryCriteria = criteria.createCriteria("entry");
        entryCriteria.add(Restrictions.disjunction()
                                      .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                                      .add(Restrictions.isNull("visibility")));
        entryCriteria.add(Restrictions.eq("ownerEmail", user));

        // sort
        if (sortField == null)
            sortField = ColumnField.CREATED;

        String fieldName;
        switch (sortField) {
            case TYPE:
                fieldName = "recordType";
                break;

            case STATUS:
                fieldName = "status";
                break;

            case CREATED:
            default:
                fieldName = "creationTime";
                break;
        }

        entryCriteria.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
        entryCriteria.setFirstResult(start);
        entryCriteria.setMaxResults(limit);
        entryCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new LinkedList<Entry>(entryCriteria.list());
    }

    /**
     * @return number of entries that have visibility of "OK" or null (which is a legacy equivalent to "OK")
     * @throws DAOException
     */
    public long getAllEntryCount() throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Entry.class.getName());
        criteria.add(Restrictions.disjunction()
                                 .add(Restrictions.eq("visibility", Visibility.OK.getValue()))
                                 .add(Restrictions.isNull("visibility")));
        Long result = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return result.longValue();
    }

    /**
     * Retrieve {@link Entry} objects of the given list of ids.
     *
     * @param ids list of ids to retrieve
     * @return ArrayList of Entry objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public LinkedList<Entry> getEntriesByIdSet(List<Long> ids) throws DAOException {
        if (ids.size() == 0) {
            return new LinkedList<>();
        }

        String filter = Utils.join(", ", ids);
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " WHERE id in (" + filter + ")");
            LinkedList<Entry> results = new LinkedList<Entry>(query.list());
            return results;
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entries!", e);
        }
    }

    /**
     * Generate the next PartNumber available in the database.
     *
     * @param prefix    Part number prefix. For example, "JBx".
     * @param delimiter Character between the prefix and the part number, For example, "_".
     * @param suffix    Example digits, for example "000000" to represent a six digit part number.
     * @return New part umber string, for example "JBx_000001".
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    String generateNextPartNumber(String prefix, String delimiter, String suffix) throws DAOException {
        Session session = currentSession();
        try {
            String queryString = "from " + PartNumber.class.getName() + " where partNumber LIKE '"
                    + prefix + "%' ORDER BY partNumber DESC";
            Query query = session.createQuery(queryString);
            query.setMaxResults(2);

            ArrayList<PartNumber> tempList = new ArrayList<PartNumber>(query.list());
            PartNumber entryPartNumber = null;
            if (tempList.size() > 0) {
                entryPartNumber = tempList.get(0);
            }

            String nextPartNumber;
            if (entryPartNumber == null) {
                nextPartNumber = prefix + delimiter + suffix;
            } else {
                String[] parts = entryPartNumber.getPartNumber().split(prefix + delimiter);

                if (parts != null && parts.length == 2) {
                    try {
                        int value = Integer.valueOf(parts[1]);
                        value++;
                        nextPartNumber = prefix + delimiter + String.format("%0" + suffix.length() + "d", value);
                    } catch (Exception e) {
                        throw new DAOException("Couldn't parse partNumber", e);
                    }
                } else {
                    throw new DAOException("Couldn't parse partNumber");
                }
            }

            return nextPartNumber;
        } catch (HibernateException e) {
            throw new DAOException("Couldn't retrieve Entry by partNumber", e);
        }
    }

    // does not check permission (includes pending entries)
    @SuppressWarnings("unchecked")
    public List<Entry> retrieveOwnerEntries(String ownerEmail, ColumnField sort, boolean asc, int start, int limit)
            throws DAOException {
        try {
            if (sort == null)
                sort = ColumnField.CREATED;

            String fieldName;
            switch (sort) {
                case TYPE:
                    fieldName = "recordType";
                    break;

                case STATUS:
                    fieldName = "status";
                    break;

                case CREATED:
                default:
                    fieldName = "creationTime";
                    break;
            }

            Session session = currentSession();
            String orderSuffix = (" ORDER BY e." + fieldName + " " + (asc ? "ASC" : "DESC"));
            String queryString = "from " + Entry.class.getName() + " e where owner_email = :oe "
                    + "AND (visibility is null or visibility = " + Visibility.OK.getValue() + " OR visibility = "
                    + Visibility.PENDING.getValue() + ")" + orderSuffix;
            Query query = session.createQuery(queryString);
            query.setParameter("oe", ownerEmail);
            query.setMaxResults(limit);
            query.setFirstResult(start);
            List list = query.list();
            return new LinkedList<Entry>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Entry> retrieveAllEntries(ColumnField sort, boolean asc, int start, int limit)
            throws DAOException {
        try {
            if (sort == null)
                sort = ColumnField.CREATED;

            String fieldName;
            switch (sort) {
                case TYPE:
                    fieldName = "recordType";
                    break;

                case STATUS:
                    fieldName = "status";
                    break;

                case CREATED:
                default:
                    fieldName = "creationTime";
                    break;
            }

            Session session = currentSession();
            String orderSuffix = (" ORDER BY e." + fieldName + " " + (asc ? "ASC" : "DESC"));
            String queryString = "from " + Entry.class.getName() + " e where (visibility is null or visibility = "
                    + Visibility.OK.getValue() + " OR visibility = "
                    + Visibility.PENDING.getValue() + ")" + orderSuffix;
            Query query = session.createQuery(queryString);
            query.setMaxResults(limit);
            query.setFirstResult(start);
            List list = query.list();
            return new LinkedList<Entry>(list);
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
                                     .add(Restrictions.eq("visibility", new Integer(Visibility.OK.getValue())))
                                     .add(Restrictions.eq("visibility", new Integer(Visibility.PENDING.getValue())))
                                     .add(Restrictions.isNull("visibility")));
            criteria.setProjection(Projections.rowCount());
            Number rowCount = (Number) criteria.uniqueResult();
            return rowCount.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    // checks permission does not include pending entries
    public long ownerEntryCount(Account requestor, String ownerEmail, Set<Group> accountGroups) throws DAOException {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Permission.class);
            criteria.setProjection(Projections.property("entry"));

            // expect everyone to at least belong to the everyone group so groups should never be empty
            Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", accountGroups));
            disjunction.add(Restrictions.eq("account", requestor));

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

    public Entry updateEntry(Entry entry) throws DAOException {
        HashSet<EntryFundingSource> sources = null;
        if (entry.getEntryFundingSources() != null) {
            sources = new HashSet<>(entry.getEntryFundingSources());
        }

        if (sources != null) {
            for (EntryFundingSource entryFundingSource : sources) {
                FundingSource newFundingSource = entryFundingSource.getFundingSource();
                FundingSource newFundingSourceExisting = getExistingFundingSource(newFundingSource);
                if (newFundingSourceExisting == null)
                    newFundingSourceExisting = saveFundingSource(newFundingSource);

                entryFundingSource.setFundingSource(newFundingSourceExisting);
                entryFundingSource.setEntry(entry);
                currentSession().saveOrUpdate(entryFundingSource);
            }
        }

        update(entry);
        return entry;
    }

    public Entry saveEntry(Entry entry) throws DAOException {
        HashSet<EntryFundingSource> sources = null;
        if (entry.getEntryFundingSources() != null) {
            sources = new HashSet<>(entry.getEntryFundingSources());
        }

        entry = save(entry);

        if (sources != null) {
            for (EntryFundingSource entryFundingSource : sources) {
                FundingSource fundingSource = entryFundingSource.getFundingSource();
                FundingSource existingFundingSource = getExistingFundingSource(fundingSource);
                if (existingFundingSource == null)
                    existingFundingSource = saveFundingSource(fundingSource);

                entryFundingSource.setFundingSource(existingFundingSource);
                entryFundingSource.setEntry(entry);
                currentSession().saveOrUpdate(entryFundingSource);
            }
        }

        return entry;
    }
}
