package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.EntryViewFactory;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.Visibility;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

/**
 * ABI to manipulate {@link org.jbei.ice.lib.entry.model.Entry}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class EntryController {

    private EntryDAO dao;
    private PermissionsController permissionsController;
    private AccountController accountController;

    public EntryController() {
        dao = new EntryDAO();
        permissionsController = new PermissionsController();
        accountController = new AccountController();
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps as necessary.
     * Sets the record globally visible and schedule an index rebuild.
     *
     * @param account   account of user creating the record
     * @param entry     entry record being created
     * @param readGroup group that can has read access to entry
     * @return entry that was saved in the database.
     * @throws ControllerException // TODO : visibility should be a parameter
     */
    public Entry createEntry(Account account, Entry entry, Group readGroup) throws ControllerException {
        return createEntry(account, entry, true, readGroup);
    }

    public HashSet<Entry> createStrainWithPlasmid(Account account, Entry strain, Entry plasmid, Group readGroup)
            throws ControllerException {

        HashSet<Entry> results = new HashSet<Entry>();

        plasmid = createEntry(account, plasmid, readGroup);
        results.add(plasmid);

        String plasmidPartNumberString = "[[" + Utils.getConfigValue(ConfigurationKey.WIKILINK_PREFIX) + ":"
                + plasmid.getOnePartNumber().getPartNumber() + "|" + plasmid.getOneName().getName()
                + "]]";
        ((Strain) strain).setPlasmids(plasmidPartNumberString);
        strain = createEntry(account, strain, readGroup);
        results.add(strain);
        return results;
    }

    /**
     * Generate the next part number string using system settings.
     *
     * @return The next part number.
     * @throws ControllerException
     */
    private String getNextPartNumber() throws ControllerException {
        try {
            return dao.generateNextPartNumber(Utils.getConfigValue(ConfigurationKey.PART_NUMBER_PREFIX),
                                              Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DELIMITER),
                                              Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX));
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps.
     * Optionally set the record globally visible or schedule an index rebuild.
     *
     * @param account              account of user creating entry
     * @param entry                entry record being created
     * @param scheduleIndexRebuild Set true to schedule search index rebuild.
     * @param readGroup            group that will have read privileges.set to null if private entry
     * @return entry that was saved in the database.
     * @throws ControllerException
     */
    public Entry createEntry(Account account, Entry entry, boolean scheduleIndexRebuild, Group readGroup)
            throws ControllerException {

        PartNumber partNumber = new PartNumber();
        String nextPart = getNextPartNumber();
        partNumber.setPartNumber(nextPart);
        Set<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
        partNumbers.add(partNumber);
        entry.getPartNumbers().add(partNumber);

        partNumber.setEntry(entry);

        entry.setRecordId(Utils.generateUUID());
        entry.setVersionId(entry.getRecordId());
        entry.setCreationTime(Calendar.getInstance().getTime());
        entry.setModificationTime(entry.getCreationTime());
        entry.setOwner(account.getFullName());
        entry.setOwnerEmail(account.getEmail());

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
            for (PartNumber pNumber : entry.getPartNumbers()) {
                pNumber.setEntry(entry);
            }
        }

        if (entry.getEntryFundingSources() != null) {
            for (EntryFundingSource fundingSource : entry.getEntryFundingSources()) {
                fundingSource.setEntry(entry);
            }
        }

        entry.setModificationTime(Calendar.getInstance().getTime());

        try {
            dao.save(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (readGroup != null) {
            try {
                permissionsController.addReadGroup(account, entry, readGroup);
            } catch (PermissionException pe) {
                Logger.error("Could add group permissions to entry \"" + entry.getId() + "\"", pe);
            }
        }

        if (scheduleIndexRebuild) {
            ApplicationController.scheduleBlastIndexRebuildTask();
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param account account of user performing action
     * @param id      unique local identifier for entry
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry get(Account account, long id) throws ControllerException, PermissionException {
        Entry entry;

        try {
            entry = dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    public ArrayList<PermissionInfo> retrievePermissions(Account account, Entry entry)
            throws ControllerException, PermissionException {

        return permissionsController.retrieveSetEntryPermissions(account, entry);
    }

    /**
     * Retrieve {@link Entry} from the database by recordId (uuid).
     *
     * @param recordId universally unique identifier that was assigned to entry on create
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByRecordId(Account account, String recordId) throws ControllerException,
            PermissionException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by part number.
     * <p/>
     * Throws exception if multiple entries have the same part number.
     *
     * @param partNumber entry part number
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByPartNumber(Account account, String partNumber) throws ControllerException,
            PermissionException {
        Entry entry;

        try {
            entry = dao.getByPartNumber(partNumber);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by name.
     * <p/>
     * Throws exception if multiple entries have the same name.
     *
     * @param name entry name
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByName(Account account, String name) throws ControllerException,
            PermissionException {

        Entry entry;

        try {
            entry = dao.getByName(name);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    public FolderDetails retrieveVisibleEntries(Account account, ColumnField field, boolean asc, int start, int limit)
            throws ControllerException {
        long startTime = System.currentTimeMillis();
        LinkedList<Entry> results;
        FolderDetails details = new FolderDetails();
        try {
//            if (accountController.isAdministrator(account)) {
//                // no filters
//                results = null;
//            } else {
            // retrieve groups for account and filter by permission
            Set<Group> accountGroups = new HashSet<Group>(account.getGroups());
            GroupController controller = new GroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            results = dao.retrieveVisibleEntries(account, accountGroups, field, asc, start, limit);
            for (Entry entry : results) {
                EntryInfo info = EntryViewFactory.createTableViewData(account, entry);
                details.getEntries().add(info);
            }
//            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        Logger.info("Retrieve took " + (System.currentTimeMillis() - startTime) + "ms");
        return details;
    }

    /**
     * Retrieve the number of entries that is visible to a particular user
     *
     * @param account user account
     * @return Number of entries that user with account referenced in the parameter can read.
     * @throws ControllerException
     */
    public long getNumberOfVisibleEntries(Account account) throws ControllerException {
        long numberOfVisibleEntries;

        Set<Group> accountGroups = new HashSet<Group>(account.getGroups());

        // TODO : retrieve all parent groups
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);

        try {
            numberOfVisibleEntries = dao.visibleEntryCount(account, accountGroups);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return numberOfVisibleEntries;
    }

    public long getNumberOfPublicEntries() throws ControllerException {
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        Set<Group> accountGroups = new HashSet<Group>();
        accountGroups.add(everybodyGroup);

        try {
            return dao.getNumberOfVisibleEntries(accountGroups, null);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<Entry> retrieveOwnerEntries(Account account, String ownerEmail,
            ColumnField sort, boolean asc, int start, int limit) throws ControllerException {
        try {
//            if( !accountController.isAdministrator(account) && !account.getEmail().equals(ownerEmail)){
//                Logger.error(account.getEmail());
//                throw new
//            }

            // TODO : should only be able to see entries that user has permission to see

            return dao.retrieveOwnerEntries(ownerEmail, sort, asc, start, limit);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public long getNumberOfOwnerEntries(Account account, String ownerEmail) throws ControllerException {
        try {
            return dao.ownerEntryCount(ownerEmail);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<Long> getEntryIdsByOwner(Account account, String ownerEmail, Visibility... visibilityList)
            throws ControllerException {
        try {
            Integer[] list = new Integer[visibilityList.length];
            int i = 0;
            for (Visibility visibility : visibilityList) {
                list[i] = visibility.getValue();
                i += 1;
            }

            ArrayList<Long> results = dao.getEntriesByOwner(ownerEmail, list);
            if (results == null)
                return results;

            Iterator<Long> resultsIter = results.iterator();

            while (resultsIter.hasNext()) {
                Long next = resultsIter.next();
                try {
                    try {
                        get(account, next);
                    } catch (PermissionException e) {
                        resultsIter.remove();
                        continue;
                    }
                } catch (ControllerException ce) {
                    Logger.error("Error retrieving permission for entry Id " + next);
                }
            }
            return results;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Save the entry into the database. Then schedule index rebuild.
     *
     * @param entry entry object to save
     * @return Saved entry.
     * @throws ControllerException
     */
    public Entry save(Entry entry) throws ControllerException {
        return save(entry, true);
    }

    /**
     * Save the entry into the database. Optionally schedule an index rebuild.
     *
     * @param entry
     * @param scheduleIndexRebuild Set True to schedule index rebuild.
     * @return Entry saved into the database.
     * @throws ControllerException
     */
    public Entry save(Entry entry, boolean scheduleIndexRebuild) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry!");
        }

        Entry savedEntry;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            savedEntry = dao.update(entry);

            if (scheduleIndexRebuild) {
                ApplicationController.scheduleBlastIndexRebuildTask();
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return savedEntry;
    }

    public Entry update(Account account, Entry entry, boolean scheduleIndexRebuild, Group readGroup)
            throws ControllerException, PermissionException {

        if (entry == null) {
            throw new ControllerException("Failed to update null entry!");
        }

        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry!");
        }

        Entry savedEntry;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            savedEntry = dao.update(entry);

            // update read permissions
            // TODO : until the permissions overhaul, no method is expected to call update on entry
            // TODO : and update the groups at the same time. a different mechanism is used
            if (readGroup != null && savedEntry.getVisibility() != Visibility.OK.getValue()) {
                HashSet<Group> groups = new HashSet<Group>();
                groups.add(readGroup);
                permissionsController.setReadGroup(account, entry, groups);
            }

            if (scheduleIndexRebuild) {
                ApplicationController.scheduleBlastIndexRebuildTask();
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return savedEntry;
    }

    /**
     * Delete the entry in the database. Schedule an index rebuild.
     *
     * @param entry
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Entry entry) throws ControllerException, PermissionException {
        delete(account, entry, true);
    }

    /**
     * Delete the entry in the database. Optionally schedule an index rebuild.
     *
     * @param entry
     * @param scheduleIndexRebuild True if index rebuild is scheduled.
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, Entry entry, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry");
        }
        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry");
        }
        String deletionString = "This entry is deleted. It was owned by " + entry.getOwnerEmail()
                + "\n";
        entry.setLongDescription(deletionString + entry.getLongDescription());
        Account sysAccount = accountController.getSystemAccount();
        entry.setOwnerEmail(sysAccount.getEmail());
        // save(entry, true); // Cannot use save, as owner has changed. Must call manager directly

        permissionsController.setReadGroup(sysAccount, entry, new HashSet<Group>());
        permissionsController.setWriteGroup(sysAccount, entry, new HashSet<Group>());
        permissionsController.setReadUser(sysAccount, entry, new HashSet<Account>());
        permissionsController.setWriteUser(sysAccount, entry, new HashSet<Account>());
        entry.setModificationTime(Calendar.getInstance().getTime());

        try {
            dao.update(entry);
        } catch (DAOException e1) {
            throw new ControllerException("Failed to save entry deletion", e1);
        }

        if (scheduleIndexRebuild) {
            ApplicationController.scheduleBlastIndexRebuildTask();
        }
    }

    /**
     * Filter {@link Entry} id's for display.
     * <p/>
     * Given a List of entry id's, keep only id's that user has read access to.
     *
     * @param ids
     * @return List of Entry ids.
     * @throws ControllerException
     */
    List<Long> filterEntriesByPermission(Account account, List<Long> ids) throws ControllerException {
        ArrayList<Long> result = new ArrayList<Long>();
        for (Long id : ids) {
            Entry entry;
            try {
                entry = dao.get(id);
            } catch (DAOException e) {
                Logger.error(e);
                continue;
            }

            if (permissionsController.hasReadPermission(account, entry)) {
                result.add(id);
            }
        }
        return result;
    }

    public long getAllEntryCount() throws ControllerException {
        try {
            return dao.getAllEntryCount();
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public long getOwnerEntryCount(Account account, Visibility... exclude) throws ControllerException {
        try {
            if (exclude.length == 0)
                return dao.getOwnerEntryCount(account.getEmail());

            Integer[] excludeInt = new Integer[exclude.length];
            for (int i = 0; i < exclude.length; i += 1)
                excludeInt[i] = exclude[i].getValue();
            return dao.getOwnerEntryCount(account.getEmail(), excludeInt);

        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public LinkedList<EntryInfo> retrieveEntriesByIdSetSort(Account account,
            LinkedList<Long> entryIds, ColumnField field, boolean asc) throws ControllerException {

        List<Entry> entries;

        try {
            switch (field) {
                case TYPE:
                    entries = dao.getEntriesByIdSetSortByType(entryIds, asc);
                    break;

                case STATUS:
                    entries = dao.getEntriesByIdSetSortByStatus(entryIds, asc);
                    break;

                case CREATED:
                    entries = dao.getEntriesByIdSetSortByCreated(entryIds, asc);
                    break;

                default:
                    entries = dao.getEntriesByIdSet(entryIds);
            }
        } catch (DAOException ce) {
            throw new ControllerException("Could not retrieve entries by sort ", ce);
        }

        if (entries == null)
            return null;
        LinkedList<EntryInfo> results = new LinkedList<EntryInfo>();

        for (Entry entry : entries) {
            EntryInfo view = EntryViewFactory.createTableViewData(account, entry);
            if (view == null)
                continue;

            results.add(view);
        }

        return results;
    }

    public ArrayList<Entry> getEntriesByIdSet(Account account, ArrayList<Long> queryResultIds)
            throws ControllerException {
        List<Long> filtered = this.filterEntriesByPermission(account, queryResultIds);
        try {
            ArrayList<Entry> results = new ArrayList<Entry>(dao.getEntriesByIdSet(filtered));
            return results;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public HashSet<Long> retrieveStrainsForPlasmid(Plasmid plasmid) throws ControllerException {
        try {
            return dao.retrieveStrainsForPlasmid(plasmid);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
