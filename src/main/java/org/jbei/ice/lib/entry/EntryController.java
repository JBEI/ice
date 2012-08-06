package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.server.EntryViewFactory;
import org.jbei.ice.shared.ColumnField;
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
    private AttachmentController attachmentController;

    public EntryController() {
        dao = new EntryDAO();
        permissionsController = new PermissionsController();
        attachmentController = new AttachmentController();
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

        String plasmidPartNumberString = "[[" + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":"
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
            return dao.generateNextPartNumber(JbeirSettings.getSetting("PART_NUMBER_PREFIX"),
                                              JbeirSettings.getSetting("PART_NUMBER_DELIMITER"),
                                              JbeirSettings.getSetting("PART_NUMBER_DIGITAL_SUFFIX"));
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps as necessary.
     * Optionally set the record globally visible or schedule an index rebuild.
     *
     * @param account              account of user creating entry
     * @param entry                entry record being created
     * @param scheduleIndexRebuild Set true to schedule search index rebuild.
     * @return entry that was saved in the database.
     * @throws ControllerException
     */
    public Entry createEntry(Account account, Entry entry, boolean scheduleIndexRebuild, Group readGroup)
            throws ControllerException {
        Entry createdEntry;

        String nextPart = getNextPartNumber();
        createdEntry = EntryFactory.createEntry(account, nextPart, entry);

        try {
            dao.saveOrUpdate(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (readGroup != null) {
            try {
                permissionsController.addReadGroup(account, createdEntry, readGroup);
            } catch (PermissionException pe) {
                Logger.error("Could not make entry " + createdEntry.getId() + " public ", pe);
            }
        }

        if (scheduleIndexRebuild) {
            ApplicationController.scheduleBlastIndexRebuildJob();
            ApplicationController.scheduleSearchIndexRebuildJob();
        }

        return createdEntry;
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

        ArrayList<PermissionInfo> permissionInfos = new ArrayList<PermissionInfo>();

        Set<Account> readAccounts = permissionsController.getReadUser(account, entry);
        for (Account readAccount : readAccounts) {
            permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.READ_ACCOUNT,
                                                   readAccount.getId(), readAccount.getFullName()));
        }

        Set<Account> writeAccounts = permissionsController.getWriteUser(account, entry);
        for (Account writeAccount : writeAccounts) {
            permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.WRITE_ACCOUNT,
                                                   writeAccount.getId(), writeAccount.getFullName()));
        }

        Set<Group> readGroups = permissionsController.getReadGroup(account, entry);
        for (Group group : readGroups) {
            permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.READ_GROUP, group
                    .getId(), group.getLabel()));
        }

        Set<Group> writeGroups = permissionsController.getWriteGroup(account, entry);
        for (Group group : writeGroups) {
            permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.WRITE_GROUP, group
                    .getId(), group.getLabel()));
        }

        return permissionInfos;
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
     * @param partNumber
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
     * @param name
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

    /**
     * Checks if the given entry has {@link org.jbei.ice.lib.entry.attachment.Attachment}s
     * associated with it.
     *
     * @param entry
     * @return True if there are associated attachments.
     * @throws ControllerException
     */
    public boolean hasAttachments(Account account, Entry entry) throws ControllerException {
        return attachmentController.hasAttachment(account, entry);
    }

    public Set<Long> getAllVisibleEntryIDs(Account account) throws ControllerException {

        Set<Group> accountGroups = new HashSet<Group>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);

        try {
            return dao.getAllVisibleEntries(accountGroups, account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<Long> getAllEntryIDs() throws ControllerException {
        try {
            return dao.getEntries("creationTime", true);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the number of publicly visible entries (Entries visible to the Everybody group).
     *
     * @param account
     * @return Number of entries.
     * @throws ControllerException
     */
    public long getNumberOfVisibleEntries(Account account) throws ControllerException {
        long numberOfVisibleEntries;

        try {
            numberOfVisibleEntries = dao.getNumberOfVisibleEntries(account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return numberOfVisibleEntries;
    }

    public ArrayList<Long> getEntryIdsByOwner(String ownerEmail, Visibility... visibilityList)
            throws ControllerException {
        try {
            Integer[] list = new Integer[visibilityList.length];
            int i = 0;
            for (Visibility visibility : visibilityList)
                list[i] = visibility.getValue();

            return dao.getEntriesByOwner(ownerEmail, list);
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
     * @throws PermissionException
     */
    public Entry save(Entry entry, boolean scheduleIndexRebuild) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry!");
        }

        Entry savedEntry = null;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            savedEntry = dao.saveOrUpdate(entry);

            if (scheduleIndexRebuild) {
                ApplicationController.scheduleSearchIndexRebuildJob();
                ApplicationController.scheduleBlastIndexRebuildJob();
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return savedEntry;
    }

    public Entry update(Account account, Entry entry) throws ControllerException,
            PermissionException {

        if (entry == null) {
            throw new ControllerException("Failed to update null entry!");
        }

        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry!");
        }

        Entry savedEntry;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            savedEntry = dao.saveOrUpdate(entry);

//            ApplicationController.scheduleSearchIndexRebuildJob();
//            ApplicationController.scheduleBlastIndexRebuildJob();
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
    public void delete(Account account, Entry entry) throws ControllerException,
            PermissionException {
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
        entry.setOwnerEmail(PopulateInitialDatabase.systemAccountEmail);
        // save(entry, true); // Cannot use save, as owner has changed. Must call manager directly

        permissionsController.setReadGroup(account, entry, new HashSet<Group>());
        permissionsController.setWriteGroup(account, entry, new HashSet<Group>());
        permissionsController.setReadUser(account, entry, new HashSet<Account>());
        permissionsController.setWriteUser(account, entry, new HashSet<Account>());
        entry.setModificationTime(Calendar.getInstance().getTime());

        try {
            dao.saveOrUpdate(entry);
        } catch (DAOException e1) {
            throw new ControllerException("Failed to save entry deletion", e1);
        }

        if (scheduleIndexRebuild) {
            ApplicationController.scheduleSearchIndexRebuildJob();
            ApplicationController.scheduleBlastIndexRebuildJob();
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
    List<Long> filterEntriesByPermission(Account account, List<Long> ids)
            throws ControllerException {
        ArrayList<Long> result = new ArrayList<Long>();
        for (Long id : ids) {
            Entry entry = null;
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
        } catch (ManagerException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public long getOwnerEntryCount(Account account, Visibility... exclude)
            throws ControllerException {
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

    public ArrayList<Entry> getAllEntries() throws ControllerException {
        try {
            return dao.getAllEntries();
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

    public LinkedList<Long> sortList(LinkedList<Long> ids, ColumnField field, boolean asc)
            throws ControllerException {
        try {

            return dao.sortList(ids, field, asc);
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

    public void setDAO(EntryDAO dao) {
        this.dao = dao;
    }

    public void setPermissionsController(PermissionsController permissionsController) {
        this.permissionsController = permissionsController;
    }
}
