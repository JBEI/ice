package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
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
import org.jbei.ice.lib.query.SortField;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

public class EntryManager extends Manager {
    public static final String STRAIN_ENTRY_TYPE = "strain";
    public static final String PLASMID_ENTRY_TYPE = "plasmid";
    public static final String PART_ENTRY_TYPE = "part";

    public EntryManager() {
    }

    public static Plasmid createPlasmid(Plasmid newPlasmid) throws ManagerException {
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
        try {
            savedPlasmid = (Plasmid) save(newPlasmid);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Could not save Plasmid to db: " + e.toString());
        }
        return savedPlasmid;

    }

    public static Strain createStrain(Strain newStrain) throws ManagerException {
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
        try {
            savedStrain = (Strain) save(newStrain);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Could not save Plasmid to db: " + e.toString());
        }
        return savedStrain;

    }

    public static Part createPart(Part newPart) throws ManagerException {
        Part savedStrain = null;
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
        try {
            savedStrain = (Part) save(newPart);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("Could not save Plasmid to db: " + e.toString());
        }
        return savedStrain;
    }

    public static void remove(Entry entry) throws ManagerException {
        try {
            dbDelete(entry);
        } catch (Exception e) {
            throw new ManagerException("Couldn't delete Entry: " + e.toString());
        }
    }

    public static Entry get(int id) throws ManagerException {
        Session session = getSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName() + " where id = :id");
            query.setParameter("id", id);
            Entry entry = (Entry) query.uniqueResult();
            return entry;

        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve Entry by id: " + String.valueOf(id), e);
        } finally {

        }
    }

    public static Entry getByRecordId(String recordId) throws ManagerException {
        Session session = getSession();
        try {
            Query query = session.createQuery("from " + Entry.class.getName()
                    + " where recordId = :recordId");
            query.setParameter("recordId", recordId);

            Entry entry = (Entry) query.uniqueResult();

            return entry;
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve Entry by recordId: " + recordId, e);
        } finally {

        }
    }

    public static Entry getByPartNumber(String partNumber) throws ManagerException {
        Session session = getSession();
        try {
            Query query = session.createQuery("from " + PartNumber.class.getName()
                    + " where partNumber = :partNumber");
            query.setParameter("partNumber", partNumber);

            PartNumber entryPartNumber = (PartNumber) query.uniqueResult();

            Entry entry;
            if (entryPartNumber == null) {
                entry = null;
            } else {
                entry = entryPartNumber.getEntry();
            }

            return entry;
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve Entry by partNumber: " + partNumber, e);
        } finally {

        }
    }

    public static Entry getByName(String name) throws ManagerException {
        Session session = getSession();
        try {
            Query query = session.createQuery("from " + Name.class.getName()
                    + " where name = :name");
            query.setParameter("name", name);

            Name entryName = (Name) query.uniqueResult();

            if (entryName == null) {
                return null;
            }

            return entryName.getEntry();
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve Entry by name: " + name, e);
        } finally {

        }
    }

    public static Set<Entry> getByFilter(ArrayList<String[]> data, int offset, int limit) {
        LinkedHashSet<Entry> result = org.jbei.ice.lib.query.Query.getInstance().query(data,
                offset, limit);

        return result;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<Entry> getByAccount(Account account, int offset, int limit) {
        String queryString = "from Entry where ownerEmail = :ownerEmail";
        Session session = getSession();
        Query query = session.createQuery(queryString);

        query.setParameter("ownerEmail", account.getEmail());
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();
        try {
            result = new LinkedHashSet<Entry>(query.list());
        } catch (HibernateException e) {
            Logger.error("Couldn't retrieve Entry by name: " + e.toString());
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<Entry> getByAccount(Account account, int offset, int limit,
            SortField[] sortFields) {

        String sortQuerySuffix = "";

        if (sortFields != null && sortFields.length > 0) {
            sortQuerySuffix = Utils.join(", ", Arrays.asList(sortFields));
        }

        String queryString = "from Entry where ownerEmail = :ownerEmail"
                + (!sortQuerySuffix.isEmpty() ? (" ORDER BY " + sortQuerySuffix) : "");
        Session session = getSession();
        Query query = session.createQuery(queryString);

        query.setParameter("ownerEmail", account.getEmail());
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        LinkedHashSet<Entry> result = null;
        try {
            result = new LinkedHashSet<Entry>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get entries by account " + e.toString());
        } finally {

        }
        return result;
    }

    public static int getByAccountCount(Account account) {
        if (account == null) {
            return 0;
        }

        String queryString = "from Entry where ownerEmail = :ownerEmail";
        Session session = getSession();
        Query query = session.createQuery(queryString);

        query.setParameter("ownerEmail", account.getEmail());
        int result = 0;
        try {
            result = query.list().size();
        } catch (HibernateException e) {
            Logger.error("Could not get number of entries by  account " + e.toString());
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Set<Entry> getAll() {
        String queryString = "from Entry";
        Session session = getSession();
        Query query = session.createQuery(queryString);
        LinkedHashSet<Entry> result = null;
        try {
            new LinkedHashSet<Entry>(query.list());
        } catch (HibernateException e) {
            String msg = "could not get all entries: " + e.toString();
            Logger.error(msg);
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Set<Entry> getAllVisible() {
        Set<Entry> result = null;
        Group everybodyGroup = null;
        Session session = getSession();
        try {
            everybodyGroup = GroupManager.getEverybodyGroup();
            String queryString = "select entry from Entry entry, ReadGroup readGroup where readGroup.group = :group and readGroup.entry = entry";
            Query query = session.createQuery(queryString);
            query.setParameter("group", everybodyGroup);
            result = new LinkedHashSet<Entry>(query.list());
        } catch (ManagerException e) {
            Logger.error("getAllVisible: " + e.toString());
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Set<Entry> getAll(int offset, int limit, SortField[] sortFields) {
        String sortQuerySuffix = "";

        if (sortFields != null && sortFields.length > 0) {
            sortQuerySuffix = Utils.join(", ", Arrays.asList(sortFields));
        }

        String queryString = "from Entry"
                + (!sortQuerySuffix.isEmpty() ? (" ORDER BY " + sortQuerySuffix) : "");
        Session session = getSession();
        Query query = session.createQuery(queryString);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        LinkedHashSet<Entry> result = null;
        try {
            new LinkedHashSet<Entry>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get all entries by limit " + e.toString());
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Set<Entry> getAllVisible(int offset, int limit, SortField[] sortFields) {
        String sortQuerySuffix = "";
        Group everybodyGroup;
        LinkedHashSet<Entry> result = null;
        Session session = getSession();
        try {
            everybodyGroup = GroupManager.getEverybodyGroup();
            if (sortFields != null && sortFields.length > 0) {
                sortQuerySuffix = Utils.join(", ", Arrays.asList(sortFields));
            }
            String queryString = "select entry from Entry entry, ReadGroup readGroup where readGroup.group = :group and readGroup.entry = entry"
                    + (!sortQuerySuffix.isEmpty() ? (" ORDER BY " + "entry." + sortQuerySuffix)
                            : "");
            Query query = session.createQuery(queryString);
            query.setParameter("group", everybodyGroup);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            result = new LinkedHashSet<Entry>(query.list());

        } catch (ManagerException e) {
            Logger.error("getAllVisible: " + e.toString());
        } catch (Exception e) {
            Logger.error("getAllVisible: " + e.toString());
        } finally {

        }

        return result;
    }

    public static int getNumberOfEntries() {
        String queryString = "select id from Entry";
        Session session = getSession();
        Query query = session.createQuery(queryString);
        int result = 0;
        try {
            result = query.list().size();
        } catch (HibernateException e) {
            Logger.error("Could not get number of entries " + e.toString());
        } finally {

        }
        return result;
    }

    public static int getNumberOfVisibleEntries() {
        Group everybodyGroup;
        int result = 0;
        Session session = getSession();
        try {
            everybodyGroup = GroupManager.getEverybodyGroup();
            String queryString = "select id from ReadGroup readGroup where readGroup.group = :group";
            Query query = session.createQuery(queryString);
            query.setParameter("group", everybodyGroup);
            result = query.list().size();

        } catch (ManagerException e) {
            Logger.error("getNumberOfVisibleEntries: " + e.toString());
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String generateNextPartNumber(String prefix, String delimiter, String suffix)
            throws ManagerException {
        Session session = getSession();
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

    /**
     * Updates or Inserts unique funding source and returns the result
     */

    private static FundingSource saveFundingSource(FundingSource fundingSource)
            throws ManagerException {
        FundingSource result;
        Session session = getSession();
        try {
            String queryString = "from " + FundingSource.class.getName()
                    + " where fundingSource=:fundingSource AND"
                    + " principalInvestigator=:principalInvestigator";
            Query query = session.createQuery(queryString);
            query.setParameter("fundingSource", fundingSource.getFundingSource());
            query.setParameter("principalInvestigator", fundingSource.getPrincipalInvestigator());
            FundingSource existingFundingSource = null;
            try {
                existingFundingSource = (FundingSource) query.uniqueResult();
            } catch (org.hibernate.NonUniqueResultException e1) {
                // dirty funding source. There are multiple of these. Clean up.
                String msg = "Cleaning unp messy funding sources. Try normalizing them";
                Logger.warn(msg);
                FundingSource duplicateFundingSource = (FundingSource) query.list().get(0);
                result = duplicateFundingSource;
            }
            if (existingFundingSource == null) {
                result = (FundingSource) dbSave(fundingSource);
            } else {
                result = existingFundingSource;
            }

        } catch (Exception e) {
            String msg = "Could not save unique funding source";
            throw new ManagerException(msg, e);
        } finally {

        }
        return result;
    }

    public static Entry save(Entry entry) throws ManagerException {
        Entry result = null;
        // deal with associated objects here instead of making individual forms
        // deal with foreign key checks. Deletion of old values happen through
        // Set.clear() and
        // hibernate cascade delete-orphaned in the model.Entry

        for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
            selectionMarker.setEntry(entry);
        }
        for (Link link : entry.getLinks()) {
            link.setEntry(entry);
        }
        for (Name name : entry.getNames()) {
            name.setEntry(entry);
        }
        for (PartNumber partNumber : entry.getPartNumbers()) {
            partNumber.setEntry(entry);
        }

        entry.setModificationTime(Calendar.getInstance().getTime());

        // Manual cascade of EntryFundingSource. Guarantees unique FundingSource
        for (EntryFundingSource entryFundingSource : entry.getEntryFundingSources()) {
            FundingSource saveFundingSource = saveFundingSource(entryFundingSource
                    .getFundingSource());
            entryFundingSource.setFundingSource(saveFundingSource);
        }

        result = (Entry) dbSave(entry);

        return result;
    }

    public static void main(String[] args) {

        System.out.println("" + getNumberOfVisibleEntries());
    }

}
