package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.NonUniqueResultException;

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
import org.jbei.ice.lib.permissions.model.ReadGroup;
import org.jbei.ice.lib.permissions.model.ReadUser;
import org.jbei.ice.lib.permissions.model.WriteGroup;
import org.jbei.ice.lib.permissions.model.WriteUser;
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
class EntryDAO extends HibernateRepository<Entry> {

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
        } finally {
            closeSession();
        }
        return strainIds;
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
        Entry entry = null;

        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName()
                                                      + " where recordId = :recordId");
            query.setString("recordId", recordId);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                entry = (Entry) queryResult;
            }
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entry by recordId: " + recordId, e);
        } finally {
            closeSession();
        }

        return entry;
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
        } finally {
            closeSession();
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
        Entry entry = null;
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + Name.class.getName()
                                                      + " where name = :name");
            query.setParameter("name", name);
            Object queryResult = query.uniqueResult();
            if (queryResult == null) {
                return null;
            }

            entry = ((Name) queryResult).getEntry();
        } catch (HibernateException e) {
            Logger.error("Failed to retrieve entry by JBEI name: " + name, e);
            throw new DAOException("Failed to retrieve entry by JBEI name: " + name, e);
        } finally {
            closeSession();
        }

        return entry;
    }

    public int getOwnerEntryCount(String ownerEmail, Integer... visibilities) throws DAOException {
        Session session = currentSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class.getName()).add(
                    Restrictions.eq("ownerEmail", ownerEmail));

            // add no restrictions if no visibilities
            if (visibilities.length > 0) {
                criteria.add(Restrictions.or(
                        Restrictions.not(Restrictions.in("visibility", visibilities)),
                        Restrictions.isNull("visibility")));
            }
            Long result = (Long) criteria.setProjection(Projections.rowCount())
                                         .uniqueResult();

            return result.intValue();
        } catch (HibernateException e) {
            throw new DAOException(
                    "Failed to retrieve entry count by owner \"" + ownerEmail + "\"", e);
        } finally {
            closeSession();
        }
    }

    /**
     * Retrieve the number of {@link Entry Entries} visible to everyone.
     *
     * @return Number of visible entries.
     * @throws DAOException
     */

    @SuppressWarnings({"unchecked"})
    public long getNumberOfVisibleEntries(Set<Group> groups, Account account) throws DAOException {
        return getAllVisibleEntries(groups, account).size();
    }

    @SuppressWarnings({"unchecked"})
    public LinkedList<Entry> retrieveVisibleEntries(Account account, Set<Group> groups,
            ColumnField sortField, boolean asc, int start, int count) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class);

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", groups));

        if (account != null) {
            disjunction.add(Restrictions.eq("account", account));
        }

        criteria.add(disjunction);
        criteria.add(Restrictions.disjunction()
                                 .add(Restrictions.eq("canWrite", true))
                                 .add(Restrictions.eq("canRead", true)));

        criteria.setFirstResult(start);
        criteria.setMaxResults(count);
        criteria.setProjection(Projections.property("entry"));

        Criteria entryC = criteria.createCriteria("entry");
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

        Order sortOrder = asc ? Order.asc(fieldName) : Order.desc(fieldName);
        entryC.addOrder(sortOrder);
        entryC.add(Restrictions.eq("visibility", new Integer(Visibility.OK.getValue())));
        List list = entryC.list();
        return new LinkedList<Entry>(list);
    }

    public long visibleEntryCount(Account account, Set<Group> groups) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class);

        // expect everyone to at least belong to the everyone group so groups should never be empty
        Junction disjunction = Restrictions.disjunction().add(Restrictions.in("group", groups));

        if (account != null) {
            disjunction.add(Restrictions.eq("account", account));
        }

        criteria.add(disjunction);
        criteria.add(Restrictions.disjunction()
                                 .add(Restrictions.eq("canWrite", true))
                                 .add(Restrictions.eq("canRead", true)));

        criteria.setProjection(Projections.rowCount());
        Long rowCount = (Long) criteria.uniqueResult();  // long
        return rowCount;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<Long> getAllVisibleEntries(Set<Group> groups, Account account) throws DAOException {
        HashSet<Long> results = new HashSet<Long>();

        try {
            Session session = currentSession();

            // check read groups
            Criteria criteria = session.createCriteria(ReadGroup.class);
            if (!groups.isEmpty())
                criteria.add(Restrictions.in("group", groups));
            List list = criteria.setProjection(Projections.property("entry.id")).list();
            results.addAll(list);

            // check write groups
            criteria = session.createCriteria(WriteGroup.class);
            if (!groups.isEmpty())
                criteria.add(Restrictions.in("group", groups));
            list = criteria.setProjection(Projections.property("entry.id")).list();
            results.addAll(list);

            if (account != null) {
                // check read user
                criteria = session.createCriteria(ReadUser.class);
                criteria.add(Restrictions.eq("account", account));
                list = criteria.setProjection(Projections.property("entry.id")).list();
                results.addAll(list);

                // check write user
                criteria = session.createCriteria(WriteUser.class);
                criteria.add(Restrictions.eq("account", account));
                list = criteria.setProjection(Projections.property("entry.id")).list();
                results.addAll(list);
            }

            // check entries (retrieve only ok or submitted drafts)
            Integer[] visibility = new Integer[]{Visibility.OK.getValue(), Visibility.PENDING.getValue()};
            criteria = session.createCriteria(Entry.class)
                              .add(Restrictions.or(Restrictions.isNull("visibility"),
                                                   Restrictions.in("visibility", visibility)))
                              .add(Restrictions.eq("ownerEmail", account.getEmail()))
                              .setProjection(Projections.id());
            results.addAll(criteria.list());

            // remove drafts
            criteria = session.createCriteria(Entry.class)
                              .add(Restrictions.eq("visibility", new Integer(Visibility.DRAFT.getValue())))
                              .setProjection(Projections.id());
            List remove = criteria.list();
            results.removeAll(remove);
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve number of visible entries!", e);
        }

        return results;
    }

    public long getAllEntryCount() throws DAOException {
        Session session = currentSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class.getName());
            Long result = (Long) criteria.setProjection(Projections.rowCount())
                                         .uniqueResult();
            return result.longValue();
        } finally {
            closeSession();
        }
    }

    /**
     * Retrieve {@link Entry} ids of the given owner email.
     *
     * @param ownerEmail owner email
     * @return ArrayList of ids.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Long> getEntriesByOwner(String ownerEmail, Integer... visibility)
            throws DAOException {
        ArrayList<Long> entries = null;

        Session session = currentSession();
        try {

            Criteria criteria = session.createCriteria(Entry.class.getName()).add(
                    Restrictions.eq("ownerEmail", ownerEmail));

            // if nothing is selected, include all
            if (visibility == null || visibility.length == 0) {
                visibility = new Integer[]{Visibility.DRAFT.getValue(), Visibility.PENDING.getValue(),
                        Visibility.OK.getValue()
                };
            }
            criteria.add(Restrictions.or(
                    Restrictions.in("visibility", visibility),
                    Restrictions.isNull("visibility")));

            @SuppressWarnings("rawtypes")
            List list = criteria.setProjection(Projections.id()).list();

            if (list != null) {
                entries = (ArrayList<Long>) list;
            }
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve entries by owner: " + ownerEmail, e);
        } finally {
            closeSession();
        }

        return entries;
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
            return new LinkedList<Entry>();
        }

        String filter = Utils.join(", ", ids);

        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " WHERE id in ("
                                                      + filter + ")");
            LinkedList<Entry> results = new LinkedList<Entry>(query.list());
            return results;
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entries!", e);
        } finally {
            closeSession();
        }
    }

    /**
     * Delete an {@link Entry} object in the database.
     *
     * @param entry entry to delete
     * @throws DAOException
     */
    public void delete(Entry entry) throws DAOException {
        super.delete(entry);
    }

    /**
     * Save the {@link Entry} object into the database.
     *
     * @param entry entry to save
     * @return Saved Entry object.
     * @throws DAOException
     */
    public Entry save(Entry entry) throws DAOException {
        if (entry == null) {
            throw new DAOException("Failed to save null entry!");
        }

        // deal with associated objects here instead of making individual forms
        // deal with foreign key checks. Deletion of old values happen through
        // Set.clear() and
        // hibernate cascade delete-orphaned in the model.Entry

        Session session = currentSession();

        try {
            if (entry.getEntryFundingSources() != null) {
                // Manual cascade of EntryFundingSource. Guarantees unique FundingSource
                for (EntryFundingSource entryFundingSource : entry.getEntryFundingSources()) {
                    FundingSource saveFundingSource = saveFundingSource(session,
                                                                        entryFundingSource.getFundingSource());
                    entryFundingSource.setFundingSource(saveFundingSource);
                }
            }

            session.save(entry);
            session.flush();
            return entry;

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Entry update(Entry entry) throws DAOException {
        if (entry == null) {
            throw new DAOException("Failed to save null entry!");
        }

        // deal with associated objects here instead of making individual forms
        // deal with foreign key checks. Deletion of old values happen through
        // Set.clear() and
        // hibernate cascade delete-orphaned in the model.Entry

        Session session = currentSession();

        try {
            if (entry.getEntryFundingSources() != null) {
                // Manual cascade of EntryFundingSource. Guarantees unique FundingSource
                for (EntryFundingSource entryFundingSource : entry.getEntryFundingSources()) {
                    FundingSource saveFundingSource = saveFundingSource(session, entryFundingSource.getFundingSource());
                    entryFundingSource.setFundingSource(saveFundingSource);
                }
            }

            session.update(entry);
            return entry;

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
    private FundingSource saveFundingSource(Session session, FundingSource fundingSource) throws DAOException {
        FundingSource result;

        if (fundingSource.getFundingSource() == null)
            fundingSource.setFundingSource("");

        String queryString = "from " + FundingSource.class.getName()
                + " where fundingSource=:fundingSource AND"
                + " principalInvestigator=:principalInvestigator";
        Query query = session.createQuery(queryString);
        query.setParameter("fundingSource", fundingSource.getFundingSource());
        query.setParameter("principalInvestigator", fundingSource.getPrincipalInvestigator());
        FundingSource existingFundingSource;

        try {
            existingFundingSource = (FundingSource) query.uniqueResult();
        } catch (NonUniqueResultException e) {
            // dirty funding source. There are multiple of these. Clean up.
            FundingSource duplicateFundingSource = (FundingSource) query.list().get(0);
            existingFundingSource = duplicateFundingSource;
        }

        if (existingFundingSource == null) {
            session.saveOrUpdate(fundingSource);
            result = fundingSource;
        } else {
            result = existingFundingSource;
        }

        return result;
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
    String generateNextPartNumber(String prefix, String delimiter, String suffix)
            throws DAOException {
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

                        nextPartNumber = prefix + delimiter
                                + String.format("%0" + suffix.length() + "d", value);
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
        } finally {
            closeSession();
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Entry> retrieveOwnerEntries(String ownerEmail, ColumnField sort, boolean asc, int start, int limit)
            throws DAOException {
        Session session = currentSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class).add(Restrictions.eq("ownerEmail", ownerEmail))
                                       .add(Restrictions.ne("visibility", new Integer(Visibility.DRAFT.getValue())));

            // sort
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

            Order sortOrder = asc ? Order.asc(fieldName) : Order.desc(fieldName);
            criteria.addOrder(sortOrder);
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
            List list = criteria.list();

            return new ArrayList<Entry>(list);

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public long ownerEntryCount(String ownerEmail) throws DAOException {
        Session session = currentSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class).add(Restrictions.eq("ownerEmail", ownerEmail))
                                       .add(Restrictions.eq("visibility", new Integer(Visibility.OK.getValue())));
            criteria.setProjection(Projections.rowCount());
            Long rowCount = (Long) criteria.uniqueResult();  // long
            return rowCount;

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
