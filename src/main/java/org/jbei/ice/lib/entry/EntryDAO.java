package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.NonUniqueResultException;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.model.ReadGroup;
import org.jbei.ice.lib.permissions.model.ReadUser;
import org.jbei.ice.lib.permissions.model.WriteGroup;
import org.jbei.ice.lib.permissions.model.WriteUser;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.Visibility;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
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
        Session session = newSession();
        HashSet<Long> strainIds = null;
        try {
            session.beginTransaction();
            for (PartNumber plasmidPartNumber : plasmidPartNumbers) {
                Query query = session
                        .createQuery("select strain.id from Strain strain where strain.plasmids like :partNumber");
                query.setString("partNumber", "%" + plasmidPartNumber.getPartNumber() + "%");
                strainIds = new HashSet<Long>(query.list());
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            Logger.error("Could not get strains for plasmid " + e.toString(), e);
            session.getTransaction().rollback();
        } finally {
            closeSession(session);
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

        Session session = newSession();
        try {
            session.getTransaction().begin();
            Query query = session.createQuery("from " + Entry.class.getName()
                                                      + " where recordId = :recordId");
            query.setString("recordId", recordId);
            Object queryResult = query.uniqueResult();
            session.getTransaction().commit();

            if (queryResult != null) {
                entry = (Entry) queryResult;
            }
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            Logger.error(e);
            throw new DAOException("Failed to retrieve entry by recordId: " + recordId, e);
        } finally {
            closeSession(session);
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

        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery("from " + PartNumber.class.getName()
                                                      + " where partNumber = :partNumber");
            query.setParameter("partNumber", partNumber);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                entry = ((PartNumber) queryResult).getEntry();
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            Logger.error(e);
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve entry by partNumber: " + partNumber, e);
        } finally {
            closeSession(session);
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
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery("from " + Name.class.getName()
                                                      + " where name = :name");
            query.setParameter("name", name);
            Object queryResult = query.uniqueResult();
            if (queryResult == null) {
                return null;
            }

            entry = ((Name) queryResult).getEntry();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            Logger.error("Failed to retrieve entry by JBEI name: " + name, e);
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve entry by JBEI name: " + name, e);
        } finally {
            closeSession(session);
        }

        return entry;
    }

    public int getOwnerEntryCount(String ownerEmail, Integer... visibilities) throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
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

            session.getTransaction().commit();
            return result.intValue();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException(
                    "Failed to retrieve entry count by owner \"" + ownerEmail + "\"", e);
        } finally {
            closeSession(session);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<Long> getAllVisibleEntries(Set<Group> groups, Account account)
            throws DAOException {

        Session session = null;
        HashSet<Long> results = new HashSet<Long>();

        try {
            session = newSession();

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

            // check entries (retrieve only ok or submitted drafts)
            Integer[] visibility = new Integer[]{Visibility.OK.getValue(), Visibility.PENDING.getValue()};
            criteria = session.createCriteria(Entry.class)
                              .add(Restrictions.or(Restrictions.isNull("visibility"),
                                                   Restrictions.in("visibility", visibility)))
                              .add(Restrictions.eq("ownerEmail", account.getEmail()))
                              .setProjection(Projections.id());
            results.addAll(criteria.list());
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve number of visible entries!", e);
        } finally {
            closeSession(session);
        }

        return results;
    }

    /**
     * Retrieve all entries in the database.
     *
     * @return ArrayList of Entries.
     * @throws DAOException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ArrayList<Entry> getAllEntries() throws DAOException {

        List results = super.retrieveAll(Entry.class);
        ArrayList<Entry> entries = new ArrayList<Entry>(results);
        return entries;
    }

    public long getAllEntryCount() throws ManagerException {
        Session session = newSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class.getName());
            Long result = (Long) criteria.setProjection(Projections.rowCount())
                                         .uniqueResult();
            return result.longValue();
        } finally {
            closeSession(session);
        }
    }

    /**
     * Retrieve {@link Entry} id's sorted by the given field, with option to sort ascending.
     *
     * @param field     The field to sort on.
     * @param ascending True if ascending
     * @return ArrayList of ids.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Long> getEntries(String field, boolean ascending) throws DAOException {
        ArrayList<Long> entries = null;

        Session session = newSession();
        try {
            String orderSuffix = (field == null) ? ""
                    : (" ORDER BY e." + field + " " + (ascending ? "ASC" : "DESC"));
            String queryString = "select id from " + Entry.class.getName() + " e " + orderSuffix;
            Query query = session.createQuery(queryString);

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                entries = (ArrayList<Long>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entries!", e);
        } finally {
            closeSession(session);
        }

        return entries;
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

        Session session = newSession();
        try {

            session.getTransaction().begin();
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
            session.getTransaction().commit();
        } catch (HibernateException e) {
            Logger.error(e);
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve entries by owner: " + ownerEmail, e);
        } finally {
            closeSession(session);
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

        Session session = newSession();
        try {
            session.getTransaction().begin();
            Query query = session.createQuery("from " + Entry.class.getName() + " WHERE id in ("
                                                      + filter + ")");
            LinkedList<Entry> results = new LinkedList<Entry>(query.list());
            session.getTransaction().commit();
            return results;
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve entries!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public List<Entry> getEntriesByIdSetSortByType(List<Long> ids, boolean ascending)
            throws DAOException {

        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        String filter = Utils.join(", ", ids);
        String orderSuffix = (" ORDER BY record_type " + (ascending ? "ASC" : "DESC"));
        String queryString = "from " + Entry.class.getName() + " WHERE id in (" + filter + ")"
                + orderSuffix;
        return retrieveEntriesByQuery(queryString);
    }

    public List<Entry> getEntriesByIdSetSortByCreated(List<Long> ids, boolean ascending)
            throws DAOException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0)
            return entries;

        String filter = Utils.join(", ", ids);
        String orderSuffix = (" ORDER BY creation_time " + (ascending ? "ASC" : "DESC"));
        String queryString = "from " + Entry.class.getName() + " WHERE id in (" + filter + ")"
                + orderSuffix;
        return retrieveEntriesByQuery(queryString);
    }

    public List<Entry> getEntriesByIdSetSortByStatus(List<Long> ids, boolean ascending)
            throws DAOException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        String filter = Utils.join(", ", ids);
        String orderSuffix = (" ORDER BY status " + (ascending ? "ASC" : "DESC"));
        String queryString = "from " + Entry.class.getName() + " WHERE id in (" + filter + ")"
                + orderSuffix;
        return retrieveEntriesByQuery(queryString);
    }

    public LinkedList<Long> sortList(LinkedList<Long> ids, ColumnField field, boolean asc)
            throws DAOException {
        if (ids == null)
            throw new DAOException("Cannot sort empty list");

        if (ids.isEmpty())
            return ids;

        if (field == null)
            field = ColumnField.CREATED;

        String fieldName;
        switch (field) {

            case TYPE:
                fieldName = "record_type";
                break;

            case STATUS:
                fieldName = "status";
                break;

            case CREATED:
            default:
                fieldName = "creation_time";
                break;
        }

        String filter = Utils.join(", ", ids);
        String orderSuffix = (" ORDER BY " + fieldName + (asc ? " ASC" : " DESC"));
        String queryString = "select id from entries where id in (" + filter + ")" + orderSuffix;
        Session session = null;

        try {
            session = newSession();
            session.beginTransaction();
            SQLQuery query = session.createSQLQuery(queryString);
            @SuppressWarnings("unchecked")
            LinkedList<Long> result = new LinkedList<Long>(query.list());
            session.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            throw new DAOException(e);
        } finally {
            closeSession(session);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Entry> retrieveEntriesByQuery(String queryString) throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            ArrayList<Entry> entries = new ArrayList<Entry>();

            @SuppressWarnings("rawtypes")
            ArrayList list = (ArrayList) query.list();

            if (list != null) {
                entries.addAll(list);
            }
            session.getTransaction().commit();
            return entries;
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve entries!", e);
        } finally {
            closeSession(session);
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
    public Entry saveOrUpdate(Entry entry) throws DAOException {
        if (entry == null) {
            throw new DAOException("Failed to save null entry!");
        }

        // deal with associated objects here instead of making individual forms
        // deal with foreign key checks. Deletion of old values happen through
        // Set.clear() and
        // hibernate cascade delete-orphaned in the model.Entry

        Session session = newSession();
        session.beginTransaction();

        try {
            if (entry.getSelectionMarkers() != null) {
                for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
                    selectionMarker.setEntry(entry);
                }
            }

            if (entry.getLinks() != null) {
                for (Link link : entry.getLinks()) {
                    link.setEntry(entry);
                }
            }

            if (entry.getNames() != null) {
                for (Name name : entry.getNames()) {
                    name.setEntry(entry);
                }
            }

            if (entry.getPartNumbers() != null) {
                for (PartNumber partNumber : entry.getPartNumbers()) {
                    partNumber.setEntry(entry);
                }
            }

            entry.setModificationTime(Calendar.getInstance().getTime());

            if (entry.getEntryFundingSources() != null) {
                // Manual cascade of EntryFundingSource. Guarantees unique FundingSource
                for (EntryFundingSource entryFundingSource : entry.getEntryFundingSources()) {
                    FundingSource saveFundingSource = saveFundingSource(session,
                                                                        entryFundingSource.getFundingSource());
                    entryFundingSource.setFundingSource(saveFundingSource);
                }
            }

            session.saveOrUpdate(entry);
            session.getTransaction().commit();
            return entry;

        } catch (HibernateException he) {
            Logger.error(he);
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Save {@link FundingSource} object into the database.
     *
     * @param fundingSource funding source to save
     * @return Saved FundingSource object.
     * @throws DAOException
     */
    private FundingSource saveFundingSource(Session session, FundingSource fundingSource)
            throws DAOException {
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
        Session session = newSession();
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
            closeSession(session);
        }
    }
}
