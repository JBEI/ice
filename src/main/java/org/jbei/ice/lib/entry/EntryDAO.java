package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.utils.Utils;

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
    public LinkedList<Long> getAllEntryIds() throws DAOException {
        Session session = currentSession();
        Criteria c = session.createCriteria(Entry.class).setProjection(Projections.id());
        return new LinkedList<Long>(c.list());
    }

    public String getEntrySummary(long id) throws DAOException {
        return (String) currentSession().createCriteria(Entry.class)
                .add(Restrictions.eq("id", id))
                .setProjection(Projections.property("shortDescription")).uniqueResult();
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

    public Set<String> getMatchingReplicatesIn(String token, int limit) throws DAOException {
        return getMatchingField("plasmid.replicatesIn", "Plasmid plasmid", token, limit);
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
            return new HashSet<String>(query.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getMatchingPlasmidNames(String token, int limit) throws DAOException {
        try {
            String qString = "select distinct plasmid.name from Plasmid plasmid where plasmid.name " +
                    "like '%" + token + "%'";
            Query query = currentSession().createQuery(qString);
            if (limit > 0)
                query.setMaxResults(limit);

            return new HashSet<String>(query.list());
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
     * Retrieve an {@link Entry} by it's name.The name must be unique to the entry
     *
     * @param name name associated with entry
     * @return Entry.
     * @throws DAOException
     */
    public Entry getByUniqueName(String name) throws DAOException {
        Session session = currentSession();

        try {
            Criteria criteria = session.createCriteria(Entry.class.getName())
                                       .add(Restrictions.eq("name", name))
                                       .add(Restrictions.eq("visibility", Visibility.OK.getValue()));
            List queryResult = criteria.list();
            if (queryResult == null || queryResult.isEmpty()) {
                return null;
            }

            if (queryResult.size() > 1) {
                String msg = "Duplicate entries found for name " + name;
                Logger.error(msg);
                throw new DAOException(msg);
            }

            return (Entry) queryResult.get(0);
        } catch (HibernateException e) {
            Logger.error("Failed to retrieve entry by name: " + name, e);
            throw new DAOException("Failed to retrieve entry by name: " + name, e);
        }
    }

    /**
     * Retrieve the number of {@link Entry Entries} visible to everyone.
     *
     * @return Number of visible entries.
     * @throws DAOException
     */

    @SuppressWarnings({"unchecked"})
    public Set<Entry> retrieveVisibleEntries(Account account, Set<Group> groups, ColumnField sortField, boolean asc,
            int start, int count) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class);

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", groups));
        if (account != null) {
            disjunction.add(Restrictions.eq("account", account));
        }

        criteria.add(Restrictions.isNotNull("entry"));

        criteria.add(disjunction);
        criteria.add(Restrictions.disjunction()
                                 .add(Restrictions.eq("canWrite", true))
                                 .add(Restrictions.eq("canRead", true)));

        Criteria entryC = criteria.createCriteria("entry", "entry");
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

            case PART_ID:
                fieldName = "partNumber";
                break;

            case NAME:
                fieldName = "name";
                break;

            case CREATED:
            default:
                fieldName = "id";
                break;
        }

        entryC.addOrder(asc ? Order.asc(fieldName) : Order.desc(fieldName));
        Set<Long> set = new HashSet<>();
        entryC.setProjection(Projections.property("id"));

        List permissions = criteria.list();
        Iterator iter = permissions.iterator();
        Set<Entry> result = new LinkedHashSet<>();
        while (iter.hasNext()) {
            Number id = (Number) iter.next();
            Entry entry = (Entry) session.get(Entry.class, id.longValue());
            if (set.contains(entry.getId()))
                continue;

            set.add(entry.getId());
            if (set.size() <= start)
                continue;

            result.add(entry);
            if (result.size() == count)
                break;
        }

        return result;
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

            case NAME:
                fieldName = "name";
                break;

            case PART_ID:
                fieldName = "partNumber";
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
        return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
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
        if (ids == null || ids.isEmpty()) {
            return new LinkedList<>();
        }

        String filter = Utils.join(", ", ids);
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " WHERE id in (" + filter + ")");
            return new LinkedList<Entry>(query.list());
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
            String queryString = "from " + Entry.class.getName() + " where partNumber LIKE '"
                    + prefix + "%' ORDER BY partNumber DESC";
            Query query = session.createQuery(queryString);
            query.setMaxResults(2);

            ArrayList<Entry> tempList = new ArrayList<Entry>(query.list());
            Entry entryPartNumber = null;
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

                case PART_ID:
                    fieldName = "partNumber";
                    break;

                case NAME:
                    fieldName = "name";
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
    public Set<Entry> retrieveAllEntries(ColumnField sort, boolean asc, int start, int limit)
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

                case NAME:
                    fieldName = "name";
                    break;

                case PART_ID:
                    fieldName = "partNumber";
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
            return new LinkedHashSet<Entry>(list);
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
                                     .add(Restrictions.eq("visibility", Visibility.PENDING.getValue()))
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

    // experimental. do not use
    public void fullDelete(Entry entry) throws DAOException {
        // delete from sub class (plasmid, strain, seed)
        Class<? extends Entry> clazz;

        if (entry.getRecordType().equalsIgnoreCase(EntryType.PLASMID.toString())) {
            clazz = Plasmid.class;
        } else if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.toString())) {
            clazz = Strain.class;
        } else if (entry.getRecordType().equalsIgnoreCase(EntryType.PART.toString())) {
            clazz = Part.class;
        } else if (entry.getRecordType().equalsIgnoreCase(EntryType.ARABIDOPSIS.toString())) {
            clazz = ArabidopsisSeed.class;
        } else
            throw new DAOException("Unrecognized entry type");
        String hql = "delete from " + clazz.getName() + " where entries_id=:entry";
        currentSession().createQuery(hql).setParameter("entry", entry.getId()).executeUpdate();

        // delete from links
        hql = "delete from " + Link.class.getName() + " where entry=:entry";
        currentSession().createQuery(hql).setParameter("entry", entry).executeUpdate();

        // delete from selection markers
        hql = "delete from " + SelectionMarker.class.getName() + " where entry=:entry";
        currentSession().createQuery(hql).setParameter("entry", entry).executeUpdate();

        // delete from funding source
        hql = "delete from " + EntryFundingSource.class.getName() + " where entry=:entry";
        currentSession().createQuery(hql).setParameter("entry", entry).executeUpdate();

        // delete from permission
        hql = "delete from " + Permission.class.getName() + " where entry=:entry";
        currentSession().createQuery(hql).setParameter("entry", entry).executeUpdate();

        // finally delete actual entry
        delete(entry);
    }

    /**
     * Converts the wiki link method for strain with plasmid to actual relationships between the entries
     *
     * @throws DAOException
     */
    public void upgradeLinks() throws DAOException {
        Session session = currentSession();
        Query query = session.createQuery("from " + Strain.class.getName());
        Iterator iterator = query.iterate();
        int i = 0;
        String wikiLink = Utils.getConfigValue(ConfigurationKey.WIKILINK_PREFIX);

        while (iterator.hasNext()) {
            Strain strain = (Strain) iterator.next();
            if (strain.getPlasmids() == null)
                continue;

            Pattern basicWikiLinkPattern = Pattern.compile("\\[\\[" + wikiLink + ":.*?\\]\\]");

            String plasmid = strain.getPlasmids().trim();
            if (plasmid.isEmpty() || !plasmid.contains(":") || !plasmid.contains("|"))
                continue;

            Matcher basicWikiLinkMatcher = basicWikiLinkPattern.matcher(plasmid);
            while (basicWikiLinkMatcher.find()) {
                String partNumber = basicWikiLinkMatcher.group().trim();
                partNumber = partNumber.split(":")[1];
                Entry entry = getByPartNumber(partNumber.split("\\|")[0]);
                if (entry == null)
                    continue;
                strain.getLinkedEntries().add(entry);
            }

            if (strain.getLinkedEntries().size() == 0)
                continue;

            session.update(strain);
            i += 1;
            if (i % 20 == 0) {
                Logger.info(Long.toString(i));
                session.flush();
                session.clear();
            }
        }
    }

    public void upgradeNamesAndPartNumbers(String partNumberPrefix) throws DAOException {
        Session session = currentSession();
        int i = 0;

        Query query = session.createQuery("from " + PartNumber.class.getName());
        Iterator iterator = query.iterate();
        while (iterator.hasNext()) {
            PartNumber number = (PartNumber) iterator.next();
            Entry entry = number.getEntry();
            if (entry.getPartNumber() == null || entry.getPartNumber().isEmpty()) {
                entry.setPartNumber(number.getPartNumber());
            } else {
                if (number.getPartNumber().startsWith(partNumberPrefix))
                    entry.setPartNumber(number.getPartNumber());
            }
            session.update(entry);
            i += 1;
            if (i % 20 == 0) {
                Logger.info(Long.toString(i));
                session.flush();
                session.clear();
            }
        }

        // upgrade names
        query = session.createQuery("from " + Name.class.getName());
        iterator = query.iterate();
        while (iterator.hasNext()) {
            Name name = (Name) iterator.next();
            Entry entry = name.getEntry();
            if (entry.getName() == null || entry.getName().isEmpty()) {
                entry.setName(name.getName());
            }
            session.update(entry);
            i += 1;
            if (i % 20 == 0) {
                Logger.info(Long.toString(i));
                session.flush();
                session.clear();
            }
        }
    }

    /**
     * links are stored in a join table in the form [entry_id, linked_entry_id] which is defined as
     * <code>
     *
     * @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
     * @JoinTable(name = "entry_entry", joinColumns = {@JoinColumn(name = "entry_id", nullable = false)},
     * inverseJoinColumns = {@JoinColumn(name = "linked_entry_id", nullable = false)})
     * private Set<Entry> linkedEntries = new HashSet<>();
     * </code>
     * Ideally we want another field such as
     * <code>
     * @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
     * @JoinTable(name = "entry_entry", joinColumns = {@JoinColumn(name = "linked_entry_id", nullable = false)},
     * inverseJoinColumns = {@JoinColumn(name = "entry_id", nullable = false)})
     * private Set<Entry> reverseLinkedEntries = new HashSet<>();
     * </code>
     * to keep track of the inverse relationship but dues to laziness I resort to this method which returns
     * entries involved in the inverse relationship with the specified entry
     */
    public LinkedList<Entry> getReverseLinkedEntries(long entryId) throws DAOException {
        String sql = "select entry_id from entry_entry where linked_entry_id=" + entryId;
        List list = currentSession().createSQLQuery(sql).list();
        return getEntriesByIdSet(new ArrayList<Long>(list));
    }
}
