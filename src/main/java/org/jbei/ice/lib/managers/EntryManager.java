package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.NonUniqueResultException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
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

public class EntryManager {
    private static final String STRAIN_ENTRY_TYPE = "strain";
    private static final String PLASMID_ENTRY_TYPE = "plasmid";
    private static final String PART_ENTRY_TYPE = "part";

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
        }

        return result;
    }

    public static Entry get(int id) throws ManagerException {
        Entry entry = null;

        Session session = DAO.getSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " where id = :id");

            query.setParameter("id", id);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                entry = (Entry) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entry by id: " + id, e);
        }

        return entry;
    }

    public static Entry getByRecordId(String recordId) throws ManagerException {
        Entry entry = null;

        Session session = DAO.getSession();
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
        }

        return entry;
    }

    public static Entry getByPartNumber(String partNumber) throws ManagerException {
        Entry entry = null;

        Session session = DAO.getSession();
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
        }

        return entry;
    }

    @SuppressWarnings("unchecked")
    public static int getNumberOfVisibleEntries() throws ManagerException {
        Group everybodyGroup;

        int result = 0;
        Session session = DAO.getSession();

        try {
            everybodyGroup = GroupManager.getEverybodyGroup();

            String queryString = "select id from ReadGroup readGroup where readGroup.group = :group";

            Query query = session.createQuery(queryString);

            query.setParameter("group", everybodyGroup);

            List<Object> results = query.list();

            if (results == null) {
                result = 0;
            } else {
                result = results.size();
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve number of visible entries!", e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Entry> getAllEntries() throws ManagerException {
        ArrayList<Entry> entries = null;

        Session session = DAO.getSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName());

            List list = query.list();

            if (list != null) {
                entries = (ArrayList<Entry>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
        }

        return entries;
    }

    public static ArrayList<Integer> getEntries() throws ManagerException {
        return getEntries(null, false);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Integer> getEntries(String field, boolean ascending)
            throws ManagerException {
        ArrayList<Integer> entries = null;

        Session session = DAO.getSession();
        try {
            String orderSuffix = (field == null) ? ""
                    : (" ORDER BY " + field + " " + (ascending ? "ASC" : "DESC"));

            String queryString = "select id from " + Entry.class.getName() + orderSuffix;

            Query query = session.createQuery(queryString);

            List list = query.list();

            if (list != null) {
                entries = (ArrayList<Integer>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
        }

        return entries;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Integer> getEntriesByOwner(String owner) throws ManagerException {
        ArrayList<Integer> entries = null;

        Session session = DAO.getSession();
        try {
            String queryString = "select id from " + Entry.class.getName()
                    + " where ownerEmail = :ownerEmail";

            Query query = session.createQuery(queryString);

            query.setParameter("ownerEmail", owner);

            List list = query.list();

            if (list != null) {
                entries = (ArrayList<Integer>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries by owner: " + owner, e);
        }

        return entries;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Entry> getEntriesByIdSet(ArrayList<Integer> ids)
            throws ManagerException {
        ArrayList<Entry> entries = null;

        if (ids.size() == 0) {
            return entries;
        }

        String filter = Utils.join(", ", ids);

        Session session = DAO.getSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " WHERE id in ("
                    + filter + ")");

            ArrayList list = (ArrayList) query.list();

            if (list != null) {
                entries = (ArrayList<Entry>) list;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entries!", e);
        }

        return entries;
    }

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

    private static Plasmid createPlasmid(Plasmid newPlasmid) throws ManagerException {
        Plasmid savedPlasmid = null;

        String number = getNextPartNumber();
        PartNumber partNumber = new PartNumber();
        partNumber.setPartNumber(number);
        LinkedHashSet<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
        partNumbers.add(partNumber);
        newPlasmid.setPartNumbers(partNumbers);

        newPlasmid.setRecordId(Utils.generateUUID());
        newPlasmid.setVersionId(newPlasmid.getRecordId());
        newPlasmid.setRecordType(PLASMID_ENTRY_TYPE);
        newPlasmid.setCreationTime(Calendar.getInstance().getTime());

        savedPlasmid = (Plasmid) save(newPlasmid);

        return savedPlasmid;
    }

    private static Strain createStrain(Strain newStrain) throws ManagerException {
        Strain savedStrain = null;

        String number = getNextPartNumber();
        PartNumber partNumber = new PartNumber();
        partNumber.setPartNumber(number);
        LinkedHashSet<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
        partNumbers.add(partNumber);
        newStrain.setPartNumbers(partNumbers);
        newStrain.setRecordId(Utils.generateUUID());
        newStrain.setVersionId(newStrain.getRecordId());
        newStrain.setRecordType(STRAIN_ENTRY_TYPE);
        newStrain.setCreationTime(Calendar.getInstance().getTime());

        savedStrain = (Strain) save(newStrain);

        return savedStrain;
    }

    private static Part createPart(Part newPart) throws ManagerException {
        Part savedPart = null;

        String number = getNextPartNumber();
        PartNumber partNumber = new PartNumber();
        partNumber.setPartNumber(number);
        LinkedHashSet<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
        partNumbers.add(partNumber);
        newPart.setPartNumbers(partNumbers);
        newPart.setRecordId(Utils.generateUUID());
        newPart.setVersionId(newPart.getRecordId());
        newPart.setRecordType(PART_ENTRY_TYPE);
        newPart.setCreationTime(Calendar.getInstance().getTime());

        savedPart = (Part) save(newPart);

        return savedPart;
    }

    /**
     * Updates or Inserts unique funding source and returns the result
     */
    private static FundingSource saveFundingSource(FundingSource fundingSource) throws DAOException {
        FundingSource result;

        Session session = DAO.getSession();
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
            result = duplicateFundingSource;
        }

        if (existingFundingSource == null) {
            result = (FundingSource) DAO.save(fundingSource);
        } else {
            result = existingFundingSource;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static String generateNextPartNumber(String prefix, String delimiter, String suffix)
            throws ManagerException {
        Session session = DAO.getSession();
        try {
            String queryString = "from " + PartNumber.class.getName() + " where partNumber LIKE '"
                    + prefix + "%' ORDER BY partNumber DESC";
            Query query = session.createQuery(queryString);

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

        }
    }

    private static String getNextPartNumber() throws ManagerException {
        return generateNextPartNumber(JbeirSettings.getSetting("PART_NUMBER_PREFIX"), JbeirSettings
                .getSetting("PART_NUMBER_DELIMITER"), JbeirSettings
                .getSetting("PART_NUMBER_DIGITAL_SUFFIX"));
    }
}
