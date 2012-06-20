package org.jbei.ice.lib.entry;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;
import org.jbei.ice.shared.ColumnField;

import javax.persistence.NonUniqueResultException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * DAO to manipulate {@link Entry} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv,
 */
class EntryDAO extends HibernateRepository {

    public long retrieveEntryByType(String type) throws DAOException {
        Session session = newSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class.getName()).add(
                    Restrictions.eq("recordType", type));
            Integer result = (Integer) criteria.setProjection(Projections.rowCount())
                                               .uniqueResult();
            return result.longValue();
        } finally {
            if (session.isOpen())
                session.close();
        }
    }

    public HashSet<Long> retrieveStrainsForPlasmid(Plasmid plasmid) throws DAOException {

        Set<PartNumber> plasmidPartNumbers = plasmid.getPartNumbers();
        Session session = newSession();
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
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return strainIds;
    }

    /**
     * Retrieve an {@link Entry} object from the database by id.
     *
     * @param id
     * @return Entry.
     * @throws ManagerException
     */
    public Entry get(long id) throws DAOException {
        return (Entry) super.get(Entry.class, id);
    }

    /**
     * Retrieve an {@link Entry} object in the database by recordId field.
     *
     * @param recordId
     * @return Entry.
     * @throws ManagerException
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
            throw new DAOException("Failed to retrieve entry by recordId: " + recordId, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entry;
    }

    /**
     * Retrieve an {@link Entry} by it's part number.
     * <p/>
     * If multiple Entries exist with the same part number, this method throws an exception.
     *
     * @param partNumber
     * @return Entry.
     * @throws ManagerException
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
            throw new DAOException("Failed to retrieve entry by partNumber: " + partNumber, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entry;
    }

    /**
     * Retrieve an {@link Entry} by it's name.
     *
     * @param name
     * @return Entry.
     * @throws ManagerException
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
            throw new DAOException("Failed to retrieve entry by JBEI name: " + name, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entry;
    }

    public int getOwnerEntryCount(String owner) throws ManagerException {
        Session session = newSession();
        try {
            Criteria criteria = session.createCriteria(Entry.class.getName()).add(
                    Restrictions.eq("owner", owner));
            Integer result = (Integer) criteria.setProjection(Projections.rowCount())
                                               .uniqueResult();

            return result.intValue();
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entry count by owner " + owner, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Retrieve the number of {@link Entry Entries} visible to everyone.
     *
     * @return Number of visible entries.
     * @throws ManagerException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static long getNumberOfVisibleEntries(Account account) throws ManagerException {
        Group everybodyGroup = null;

        long result = 0;
        Session session = null;

        try {
            GroupController controller = new GroupController();
            try {
                everybodyGroup = controller.createOrRetrievePublicGroup();
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            session = newSession();
            String queryString = "select id from ReadGroup readGroup where readGroup.group = :group";

            Query query = session.createQuery(queryString);

            query.setParameter("group", everybodyGroup);

            List<Object> results = query.list();

            if (results == null) {
                result = 0;
            } else {
                result = results.size();
            }

            if (account == null)
                return result;

            // get all visible entries
            queryString = "select id from ReadUser readUser where readUser.account = :account";
            query = session.createQuery(queryString);
            query.setParameter("account", account);
            List accountResults = query.list();
            if (accountResults != null) {
                result += accountResults.size();
            }

        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve number of visible entries!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<Long> getAllVisibleEntries(Group everybodyGroup, Account account)
            throws DAOException {

        Session session = null;
        Set<Long> visibleEntries = new HashSet<Long>();

        try {
            session = newSession();
            session.getTransaction().begin();
            String queryString = "select entry_id from permission_read_groups where group_id = "
                    + everybodyGroup.getId();
            Query query = session.createSQLQuery(queryString);
            //            query.setParameter("group", everybodyGroup);
            List results = query.list();
            visibleEntries.addAll(((ArrayList<Long>) results));

            if (account == null) {
                session.getTransaction().commit();
                return visibleEntries;
            }

            // get all visible entries
            queryString = "select entry_id from permission_read_users where account_id = "
                    + account.getId();
            query = session.createSQLQuery(queryString);
            //            query.setParameter("account", account);
            List accountResults = query.list();
            visibleEntries.addAll(((ArrayList<Long>) accountResults));
            session.getTransaction().commit();
            return visibleEntries;

        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve number of visible entries!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
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
            Integer result = (Integer) criteria.setProjection(Projections.rowCount())
                                               .uniqueResult();
            return result.longValue();
        } finally {
            if (session.isOpen())
                session.close();
        }
    }

    /**
     * Retrieve {@link Entry} id's sorted by the given field, with option to sort ascending.
     *
     * @param field     The field to sort on.
     * @param ascending True if ascending
     * @return ArrayList of ids.
     * @throws ManagerException
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
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    /**
     * Retrieve {@link Entry} ids of the given owner email.
     *
     * @param owner
     * @return ArrayList of ids.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Long> getEntriesByOwner(String owner) throws DAOException {
        ArrayList<Long> entries = null;

        Session session = newSession();
        try {
            String queryString = "select id from " + Entry.class.getName()
                    + " where ownerEmail = :ownerEmail";

            Query query = session.createQuery(queryString);
            query.setParameter("ownerEmail", owner);

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                entries = (ArrayList<Long>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entries by owner: " + owner, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    /**
     * Retrieve {@link Entry} ids of the given ownerEmail sorted by field.
     *
     * @param owner
     * @param field
     * @param ascending True if ascending.
     * @return ArrayList of ids.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Long> getEntriesByOwnerSort(String owner, String field,
            boolean ascending) throws ManagerException {
        ArrayList<Long> entries = null;

        Session session = newSession();
        try {
            String orderSuffix = (field == null) ? ""
                    : (" ORDER BY e." + field + " " + (ascending ? "ASC" : "DESC"));

            String queryString = "select id from " + Entry.class.getName()
                    + " e where ownerEmail = :ownerEmail" + orderSuffix;

            Query query = session.createQuery(queryString);

            query.setParameter("ownerEmail", owner);

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                entries = (ArrayList<Long>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries by owner: " + owner, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    /**
     * Retrieve {@link Entry} objects from the database given a list of id's, sorted by the given
     * field.
     *
     * @param ids
     * @param field
     * @param ascending
     * @return List of Entry objects
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public List<Entry> getEntriesByIdSetSort(List<Long> ids, String field, boolean ascending)
            throws DAOException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        String filter = Utils.join(", ", ids);

        Session session = newSession();
        try {
            String orderSuffix = (field == null) ? ""
                    : (" ORDER BY e." + field + " " + (ascending ? "ASC" : "DESC"));

            session.getTransaction().begin();
            Query query = session.createQuery("from " + Entry.class.getName() + " e WHERE id in ("
                                                      + filter + ")" + orderSuffix);

            @SuppressWarnings("rawtypes")
            ArrayList list = (ArrayList) query.list();

            if (list != null) {
                entries.addAll(list);
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Failed to retrieve entries!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    /**
     * Retrieve {@link Entry} ids sorted by name.
     *
     * @param ascending
     * @return List of Entry ids.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public List<Long> getEntriesSortByName(boolean ascending) throws DAOException {
        ArrayList<Long> entries = new ArrayList<Long>();

        Session session = newSession();
        try {

            String queryString = "SELECT entries_id FROM names ORDER BY name "
                    + (ascending ? "ASC" : "DESC");

            session.getTransaction().begin();
            Query query = session.createSQLQuery(queryString);
            List<Integer> list = query.list();

            if (list != null) {
                for (Integer val : list) {
                    entries.add(val.longValue());
                }
            }
            session.getTransaction().commit();
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    /**
     * Retrieve {@link Entry} ids sorted by their {@link PartNumber}.
     *
     * @param ascending
     * @return List of Entry ids.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<Long> getEntriesSortByPartNumber(boolean ascending) throws DAOException {
        ArrayList<Long> entries = new ArrayList<Long>();

        Session session = newSession();
        try {

            String queryString = "SELECT entries_id FROM part_numbers ORDER BY part_number "
                    + (ascending ? "ASC" : "DESC");

            session.getTransaction().begin();
            Query query = session.createSQLQuery(queryString);
            List<Integer> list = query.list();

            if (list != null) {
                for (Integer val : list) {
                    entries.add(val.longValue());
                }
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            Logger.error(e);
            throw new DAOException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    /**
     * Retrieve {@link Entry} objects of the given list of ids.
     *
     * @param ids
     * @return ArrayList of Entry objects.
     * @throws ManagerException
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

    public List<Entry> getEntriesByIdSetSortByName(List<Long> ids, boolean ascending)
            throws DAOException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        //        String filter = Utils.join(", ", ids);
        // TODO : add the filter to filter in the database and not here
        List<Long> sortedEntries = getEntriesSortByName(ascending);
        sortedEntries.retainAll(ids);
        return getEntriesByIdSetSort(sortedEntries, "id", ascending);
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

    public List<Entry> getEntriesByIdSetSortByPartNumber(List<Long> ids, boolean ascending)
            throws DAOException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        //        String filter = Utils.join(", ", ids);
        // TODO : add the filter to filter in the database and not here
        List<Long> sortedEntries = getEntriesSortByPartNumber(ascending);
        sortedEntries.retainAll(ids);
        return getEntriesByIdSetSort(sortedEntries, "id", ascending);
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

        String fieldName = "";
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
            if (session != null)
                session.close();
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
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Delete an {@link Entry} object in the database.
     *
     * @param entry
     * @throws ManagerException
     */
    public void delete(Entry entry) throws DAOException {
        super.delete(entry);
    }

    /**
     * Save the {@link Entry} object into the database.
     *
     * @param entry
     * @return Saved Entry object.
     * @throws ManagerException
     */
    public Entry save(Entry entry) throws ManagerException {
        if (entry == null) {
            throw new ManagerException("Failed to save null entry!");
        }

        Entry savedEntry = null;
        // deal with associated objects here instead of making individual forms
        // deal with foreign key checks. Deletion of old values happen through
        // Set.clear() and
        // hibernate cascade delete-orphaned in the model.Entry

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
                    FundingSource saveFundingSource = saveFundingSource(entryFundingSource
                                                                                .getFundingSource());
                    entryFundingSource.setFundingSource(saveFundingSource);
                }
            }

            savedEntry = (Entry) super.saveOrUpdate(entry);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save entry!", e);
        }

        return savedEntry;
    }

    /**
     * Save {@link FundingSource} object into the database.
     *
     * @param fundingSource
     * @return Saved FundingSource object.
     * @throws DAOException
     */
    private FundingSource saveFundingSource(FundingSource fundingSource) throws DAOException {
        FundingSource result;

        Session session = newSession();
        String queryString = "from " + FundingSource.class.getName()
                + " where fundingSource=:fundingSource AND"
                + " principalInvestigator=:principalInvestigator";
        Query query = session.createQuery(queryString);
        query.setParameter("fundingSource", fundingSource.getFundingSource());
        query.setParameter("principalInvestigator", fundingSource.getPrincipalInvestigator());
        FundingSource existingFundingSource = null;

        try {
            existingFundingSource = (FundingSource) query.uniqueResult();
        } catch (NonUniqueResultException e) {
            // dirty funding source. There are multiple of these. Clean up.
            FundingSource duplicateFundingSource = (FundingSource) query.list().get(0);
            existingFundingSource = duplicateFundingSource;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        if (existingFundingSource == null) {
            result = (FundingSource) super.saveOrUpdate(fundingSource);
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
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    String generateNextPartNumber(String prefix, String delimiter, String suffix)
            throws ManagerException {
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

            String nextPartNumber = null;
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
                        throw new ManagerException("Couldn't parse partNumber", e);
                    }
                } else {
                    throw new ManagerException("Couldn't parse partNumber");
                }
            }

            return nextPartNumber;
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve Entry by partNumber", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

}
