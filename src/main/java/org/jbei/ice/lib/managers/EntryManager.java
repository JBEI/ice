package org.jbei.ice.lib.managers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.NonUniqueResultException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.ColumnField;

/**
 * Manager to manipulate {@link Entry} objects in the database.
 * 
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 * 
 */
public class EntryManager {
    /**
     * Create a new {@link Entry} object in the database.
     * 
     * @param entry
     * @return Saved Entry object.
     * @throws ManagerException
     */
    public static Entry createEntry(Entry entry) throws ManagerException {
        Entry result = null;

        if (entry == null) {
            result = null;
        } else if (entry instanceof Plasmid) {
            result = createPlasmid((Plasmid) entry);
        } else if (entry instanceof Strain) {
            result = createStrain((Strain) entry);
        } else if (entry instanceof Part) {
            result = createPart((Part) entry);
        } else if (entry instanceof ArabidopsisSeed) {
            result = createArabidopsisSeed((ArabidopsisSeed) entry);
        }

        return result;
    }

    public static long retrieveEntryByType(String type) throws ManagerException {
        Session session = DAO.newSession();
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

    /**
     * Retrieve an {@link Entry} object from the database by id.
     * 
     * @param id
     * @return Entry.
     * @throws ManagerException
     */
    public static Entry get(long id) throws ManagerException {
        Entry entry = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " where id = :id");

            query.setParameter("id", id);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                entry = (Entry) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entry by id: " + id, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entry;
    }

    /**
     * Retrieve an {@link Entry} object in the database by recordId field.
     * 
     * @param recordId
     * @return Entry.
     * @throws ManagerException
     */
    public static Entry getByRecordId(String recordId) throws ManagerException {
        Entry entry = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName()
                    + " where recordId = :recordId");

            query.setParameter("recordId", recordId);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                entry = (Entry) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entry by recordId: " + recordId, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entry;
    }

    /**
     * Retrieve an {@link Entry} by it's part number.
     * <p>
     * If multiple Entries exist with the same part number, this method throws an exception.
     * 
     * @param partNumber
     * @return Entry.
     * @throws ManagerException
     */
    public static Entry getByPartNumber(String partNumber) throws ManagerException {
        Entry entry = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + PartNumber.class.getName()
                    + " where partNumber = :partNumber");

            query.setParameter("partNumber", partNumber);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                entry = ((PartNumber) queryResult).getEntry();
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entry by partNumber: " + partNumber, e);
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
    public static Entry getByName(String name) throws ManagerException {
        Entry entry = null;
        Session session = DAO.newSession();

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
            throw new ManagerException("Failed to retrieve entry by JBEI name: " + name, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entry;
    }

    /**
     * Retrieve the number of {@link Entry Entries} visible to everyone.
     * 
     * @return Number of visible entries.
     * @throws ManagerException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
            session = DAO.newSession();
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Set<Long> getAllVisibleEntries(Account account) throws ManagerException {

        Group everybodyGroup = null;
        Session session = null;
        Set<Long> visibleEntries = new HashSet<Long>();

        try {
            GroupController controller = new GroupController();
            try {
                everybodyGroup = controller.createOrRetrievePublicGroup();
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            session = DAO.newSession();
            String queryString = "select entry_id from permission_read_groups where group_id = "
                    + everybodyGroup.getId();
            Query query = session.createSQLQuery(queryString);
            //            query.setParameter("group", everybodyGroup);
            List results = query.list();
            visibleEntries.addAll(((ArrayList<Long>) results));

            if (account == null)
                return visibleEntries;

            // get all visible entries
            queryString = "select entry_id from permission_read_users where account_id = "
                    + account.getId();
            query = session.createSQLQuery(queryString);
            //            query.setParameter("account", account);
            List accountResults = query.list();
            visibleEntries.addAll(((ArrayList<Long>) accountResults));

            return visibleEntries;

        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve number of visible entries!", e);
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
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Entry> getAllEntries() throws ManagerException {
        ArrayList<Entry> entries = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName());

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                entries = (ArrayList<Entry>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    public static long getAllEntryCount() throws ManagerException {
        Session session = DAO.newSession();
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
     * @param field
     *            The field to sort on.
     * @param ascending
     *            True if ascending
     * @return ArrayList of ids.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Long> getEntries(String field, boolean ascending)
            throws ManagerException {
        ArrayList<Long> entries = null;

        Session session = DAO.newSession();
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
            throw new ManagerException("Failed to retrieve entries!", e);
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
    public static ArrayList<Long> getEntriesByOwner(String owner) throws ManagerException {
        ArrayList<Long> entries = null;

        Session session = DAO.newSession();
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
            throw new ManagerException("Failed to retrieve entries by owner: " + owner, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return entries;
    }

    public static int getEntryCountBy(String owner) throws ManagerException {
        Session session = DAO.newSession();
        try {
            SQLQuery query = session
                    .createSQLQuery("SELECT COUNT(id) FROM entries WHERE owner_email = :owner ");
            query.setString("owner", owner);
            return ((BigInteger) query.uniqueResult()).intValue();
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entry count by owner " + owner, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Retrieve {@link Entry} ids of the given ownerEmail sorted by field.
     * 
     * @param owner
     * @param field
     * @param ascending
     *            True if ascending.
     * @return ArrayList of ids.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Long> getEntriesByOwnerSort(String owner, String field,
            boolean ascending) throws ManagerException {
        ArrayList<Long> entries = null;

        Session session = DAO.newSession();
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
    public static List<Entry> getEntriesByIdSetSort(List<Long> ids, String field, boolean ascending)
            throws ManagerException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        String filter = Utils.join(", ", ids);

        Session session = DAO.newSession();
        try {
            String orderSuffix = (field == null) ? ""
                    : (" ORDER BY e." + field + " " + (ascending ? "ASC" : "DESC"));

            Query query = session.createQuery("from " + Entry.class.getName() + " e WHERE id in ("
                    + filter + ")" + orderSuffix);

            @SuppressWarnings("rawtypes")
            ArrayList list = (ArrayList) query.list();

            if (list != null) {
                entries.addAll(list);
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
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
    public static List<Long> getEntriesSortByName(boolean ascending) throws ManagerException {
        ArrayList<Long> entries = new ArrayList<Long>();

        Session session = DAO.newSession();
        try {

            String queryString = "SELECT entries_id FROM names ORDER BY name "
                    + (ascending ? "ASC" : "DESC");

            Query query = session.createSQLQuery(queryString);

            List<Integer> list = query.list();

            if (list != null) {
                for (Integer val : list) {
                    entries.add(val.longValue());
                }
            }
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
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getEntriesSortByPartNumber(boolean ascending) throws ManagerException {
        ArrayList<Long> entries = new ArrayList<Long>();

        Session session = DAO.newSession();
        try {

            String queryString = "SELECT entries_id FROM part_numbers ORDER BY part_number "
                    + (ascending ? "ASC" : "DESC");

            Query query = session.createSQLQuery(queryString);

            List<Integer> list = query.list();

            if (list != null) {
                for (Integer val : list) {
                    entries.add(val.longValue());
                }
            }
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
    public static LinkedList<Entry> getEntriesByIdSet(List<Long> ids) throws ManagerException {
        if (ids.size() == 0) {
            return new LinkedList<Entry>();
        }

        String filter = Utils.join(", ", ids);

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " WHERE id in ("
                    + filter + ")");

            return new LinkedList<Entry>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public static List<Entry> getEntriesByIdSetSortByType(List<Long> ids, boolean ascending)
            throws ManagerException {

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

    public static List<Entry> getEntriesByIdSetSortByName(List<Long> ids, boolean ascending)
            throws ManagerException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        //        String filter = Utils.join(", ", ids);
        // TODO : add the filter to filter in the database and not here
        List<Long> sortedEntries = EntryManager.getEntriesSortByName(ascending);
        sortedEntries.retainAll(ids);
        return EntryManager.getEntriesByIdSetSort(sortedEntries, "id", ascending);
    }

    public static List<Entry> getEntriesByIdSetSortByCreated(List<Long> ids, boolean ascending)
            throws ManagerException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0)
            return entries;

        String filter = Utils.join(", ", ids);
        String orderSuffix = (" ORDER BY creation_time " + (ascending ? "ASC" : "DESC"));
        String queryString = "from " + Entry.class.getName() + " WHERE id in (" + filter + ")"
                + orderSuffix;
        return retrieveEntriesByQuery(queryString);
    }

    public static List<Entry> getEntriesByIdSetSortByPartNumber(List<Long> ids, boolean ascending)
            throws ManagerException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (ids.size() == 0) {
            return entries;
        }

        //        String filter = Utils.join(", ", ids);
        // TODO : add the filter to filter in the database and not here
        List<Long> sortedEntries = EntryManager.getEntriesSortByPartNumber(ascending);
        sortedEntries.retainAll(ids);
        return EntryManager.getEntriesByIdSetSort(sortedEntries, "id", ascending);
    }

    public static List<Entry> getEntriesByIdSetSortByStatus(List<Long> ids, boolean ascending)
            throws ManagerException {
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

    public static LinkedList<Long> sortList(LinkedList<Long> ids, ColumnField field, boolean asc)
            throws ManagerException {
        if (ids == null)
            throw new ManagerException("Cannot sort empty list");

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
            session = DAO.newSession();
            session.beginTransaction();
            SQLQuery query = session.createSQLQuery(queryString);
            @SuppressWarnings("unchecked")
            LinkedList<Long> result = new LinkedList<Long>(query.list());
            session.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            throw new ManagerException(e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    @SuppressWarnings("unchecked")
    protected static List<Entry> retrieveEntriesByQuery(String queryString) throws ManagerException {
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery(queryString);
            ArrayList<Entry> entries = new ArrayList<Entry>();

            @SuppressWarnings("rawtypes")
            ArrayList list = (ArrayList) query.list();

            if (list != null) {
                entries.addAll(list);
            }
            return entries;
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
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
    public static void delete(Entry entry) throws ManagerException {
        if (entry == null) {
            throw new ManagerException("Failed to delete null entry!");
        }

        try {
            DAO.delete(entry);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete entry!", e);
        }
    }

    /**
     * Save the {@link Entry} object into the database.
     * 
     * @param entry
     * @return Saved Entry object.
     * @throws ManagerException
     */
    public static Entry save(Entry entry) throws ManagerException {
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

            savedEntry = (Entry) DAO.save(entry);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save entry!", e);
        }

        return savedEntry;
    }

    /**
     * Create a new {@link Plasmid} object in the database.
     * 
     * @param newPlasmid
     * @return Saved Plasmid object.
     * @throws ManagerException
     */
    private static Plasmid createPlasmid(Plasmid newPlasmid) throws ManagerException {
        Plasmid savedPlasmid = null;

        newPlasmid = (Plasmid) createGenericEntry(newPlasmid);
        newPlasmid.setRecordType(Entry.PLASMID_ENTRY_TYPE);

        savedPlasmid = (Plasmid) save(newPlasmid);

        return savedPlasmid;
    }

    /**
     * create a new {@link Strain} object in the database.
     * 
     * @param newStrain
     * @return saved Strain object.
     * @throws ManagerException
     */
    private static Strain createStrain(Strain newStrain) throws ManagerException {
        Strain savedStrain = null;

        newStrain = (Strain) createGenericEntry(newStrain);
        newStrain.setRecordType(Entry.STRAIN_ENTRY_TYPE);

        savedStrain = (Strain) save(newStrain);

        return savedStrain;
    }

    /**
     * Create a new {@link Part} object in the database.
     * 
     * @param newPart
     * @return Saved Part object.
     * @throws ManagerException
     */
    private static Part createPart(Part newPart) throws ManagerException {
        Part savedPart = null;

        newPart = (Part) createGenericEntry(newPart);
        newPart.setRecordType(Entry.PART_ENTRY_TYPE);

        savedPart = (Part) save(newPart);

        return savedPart;
    }

    /**
     * Create a new {@link ArabidopsisSeed} object in the database.
     * 
     * @param newArabidopsisSeed
     * @return Saved ArabidopsisSeed object.
     * @throws ManagerException
     */
    private static ArabidopsisSeed createArabidopsisSeed(ArabidopsisSeed newArabidopsisSeed)
            throws ManagerException {
        ArabidopsisSeed savedArabidopsisSeed = null;

        newArabidopsisSeed = (ArabidopsisSeed) createGenericEntry(newArabidopsisSeed);
        newArabidopsisSeed.setRecordType(Entry.ARABIDOPSIS_SEED_ENTRY_TYPE);

        savedArabidopsisSeed = (ArabidopsisSeed) save(newArabidopsisSeed);

        return savedArabidopsisSeed;
    }

    /**
     * Create an {@link Entry} object in the database.
     * <p>
     * Call to this method assigning the next {@link PartNumber}, and a random recordId, sets the
     * creationTime or the modificationTime. It does not set the recordType, which are handled by
     * the appropriate create methods for each recordType.
     * 
     * @param newEntry
     * @return Saved Entry object.
     * @throws ManagerException
     */
    private static Entry createGenericEntry(Entry newEntry) throws ManagerException {
        String number = getNextPartNumber();
        PartNumber partNumber = new PartNumber();
        partNumber.setPartNumber(number);
        Set<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
        partNumbers.add(partNumber);
        newEntry.getPartNumbers().add(partNumber);
        if (newEntry.getRecordId() == null || "".equals(newEntry.getRecordId())) {
            newEntry.setRecordId(Utils.generateUUID());
        }
        if (newEntry.getVersionId() == null || "".equals(newEntry.getVersionId())) {
            newEntry.setVersionId(newEntry.getRecordId());
        }
        if (newEntry.getCreationTime() == null) {
            newEntry.setCreationTime(Calendar.getInstance().getTime());
        } else {
            newEntry.setModificationTime(Calendar.getInstance().getTime());
        }

        return newEntry;
    }

    /**
     * Save {@link FundingSource} object into the database.
     * 
     * @param fundingSource
     * @return Saved FundingSource object.
     * @throws DAOException
     */
    private static FundingSource saveFundingSource(FundingSource fundingSource) throws DAOException {
        FundingSource result;

        Session session = DAO.newSession();
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
            result = (FundingSource) DAO.save(fundingSource);
        } else {
            result = existingFundingSource;
        }

        return result;
    }

    /**
     * Generate the next PartNumber available in the database.
     * 
     * @param prefix
     *            Part number prefix. For example, "JBx".
     * @param delimiter
     *            Character between the prefix and the part number, For example, "_".
     * @param suffix
     *            Example digits, for example "000000" to represent a six digit part number.
     * @return New part umber string, for example "JBx_000001".
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    private static String generateNextPartNumber(String prefix, String delimiter, String suffix)
            throws ManagerException {
        Session session = DAO.newSession();
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

    /**
     * Generate the next part number string using system settings.
     * 
     * @return The next part number.
     * @throws ManagerException
     */
    private static String getNextPartNumber() throws ManagerException {
        return generateNextPartNumber(JbeirSettings.getSetting("PART_NUMBER_PREFIX"),
            JbeirSettings.getSetting("PART_NUMBER_DELIMITER"),
            JbeirSettings.getSetting("PART_NUMBER_DIGITAL_SUFFIX"));
    }

}
