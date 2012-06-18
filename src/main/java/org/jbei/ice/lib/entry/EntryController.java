package org.jbei.ice.lib.entry;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.*;
import org.jbei.ice.lib.permissions.PermissionDAO;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.server.EntryViewFactory;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ABI to manipulate {@link Entry}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class EntryController {

    private final EntryDAO dao;
    private final EntryPermissionVerifier verifier;

    public EntryController() {
        verifier = new EntryPermissionVerifier();
        dao = new EntryDAO();
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps as necessary.
     * Sets the record globally visible and schedule an index rebuild.
     *
     * @param entry
     * @return entry that was saved in the database.
     * @throws ControllerException
     */
    public Entry createEntry(Entry entry) throws ControllerException {
        return createEntry(entry, true, true);
    }

    public HashSet<Entry> createStrainWithPlasmid(Strain strain, Plasmid plasmid) throws ControllerException {
        checkNotNull(strain);
        checkNotNull(plasmid);

        HashSet<Entry> results = new HashSet<Entry>();

        plasmid = (Plasmid) createEntry(plasmid);
        results.add(plasmid);

        String plasmidPartNumberString = "[["
                + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":"
                + plasmid.getOnePartNumber().getPartNumber() + "|"
                + plasmid.getOneName().getName() + "]]";
        strain.setPlasmids(plasmidPartNumberString);
        strain = (Strain) createEntry(strain);
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
        } catch (ManagerException e) {
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
     * @param entry
     * @param scheduleIndexRebuild Set true to schedule search index rebuild.
     * @param makePublic           Set true to make record globally visible.
     * @return entry that was saved in the database.
     * @throws ControllerException
     */
    public Entry createEntry(Entry entry, boolean scheduleIndexRebuild, boolean makePublic)
            throws ControllerException {
        Entry createdEntry = null;
        GroupController controller = new GroupController();

        try {
            String nextPart = getNextPartNumber();
            createdEntry = EntryFactory.createEntry(nextPart, entry);
            if (makePublic) {
                PermissionDAO.addReadGroup(createdEntry, controller.createOrRetrievePublicGroup());
            }

            if (scheduleIndexRebuild) {
                ApplicationController.scheduleBlastIndexRebuildJob();
                ApplicationController.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return createdEntry;
    }

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param id
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry get(Account account, long id) throws ControllerException, PermissionException {
        Entry entry = null;

        try {
            entry = dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by recordId (uuid).
     *
     * @param recordId
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByRecordId(Account account, String recordId) throws ControllerException,
            PermissionException {
        Entry entry = null;

        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !hasReadPermission(account, entry)) {
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
        Entry entry = null;

        try {
            entry = dao.getByPartNumber(partNumber);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !hasReadPermission(account, entry)) {
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

        Entry entry = null;

        try {
            entry = dao.getByName(name);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    /**
     * Checks if the user has read permission to the given {@link Entry}.
     *
     * @param entry
     * @return True if user has read permission.
     * @throws ControllerException
     */
    public boolean hasReadPermission(Account account, Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to check read permissions for null entry!");
        }

        return verifier.hasReadPermissions(entry, account);
    }

    /**
     * Checks if the user has write permission to the given {@link Entry}
     *
     * @param entry
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Account account, Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to check write permissions for null entry!");
        }
        return verifier.hasWritePermissions(entry, account);
    }

    /**
     * Checks if the given entry has a {@link Sequence} associated with it.
     *
     * @param entry
     * @return True if sequence is associated.
     * @throws ControllerException
     */
    public boolean hasSequence(Entry entry) throws ControllerException {
        try {
            return SequenceManager.hasSequence(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Checks if the given entry has {@link Sample}s associated with it.
     *
     * @param entry
     * @return True if there are associated samples.
     * @throws ControllerException
     */
    public boolean hasSamples(Entry entry) throws ControllerException {
        try {
            return SampleManager.hasSample(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Checks if the given entry has {@link Attachment}s associated with it.
     *
     * @param entry
     * @return True if there are associated attachments.
     * @throws ControllerException
     */
    public boolean hasAttachments(Entry entry) throws ControllerException {
        try {
            return AttachmentManager.hasAttachment(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Long> getAllVisibleEntryIDs(Account account) throws ControllerException {
        try {
            GroupController controller = new GroupController();
            Group publicGroup = controller.createOrRetrievePublicGroup();
            return dao.getAllVisibleEntries(publicGroup, account);
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
        long numberOfVisibleEntries = 0;

        try {
            numberOfVisibleEntries = EntryDAO.getNumberOfVisibleEntries(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return numberOfVisibleEntries;
    }

    public ArrayList<Long> getEntryIdsByOwner(String ownerEmail) throws ControllerException {
        try {
            return dao.getEntriesByOwner(ownerEmail);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Save the entry into the database. Then schedule index rebuild.
     *
     * @param entry
     * @return Saved entry.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry save(Account account, Entry entry) throws ControllerException, PermissionException {
        return save(account, entry, true);
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
    public Entry save(Account account, Entry entry, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry!");
        }

        if (!hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry!");
        }

        Entry savedEntry = null;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());

            savedEntry = EntryDAO.save(entry);

            if (scheduleIndexRebuild) {
                ApplicationController.scheduleSearchIndexRebuildJob();
                ApplicationController.scheduleBlastIndexRebuildJob();
            }
        } catch (ManagerException e) {
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
        if (!hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry");
        }
        String deletionString = "This entry is deleted. It was owned by " + entry.getOwnerEmail()
                + "\n";
        entry.setLongDescription(deletionString + entry.getLongDescription());
        entry.setOwnerEmail(PopulateInitialDatabase.systemAccountEmail);
        // save(entry, true); // Cannot use save, as owner has changed. Must call manager directly
        try {
            PermissionDAO.setReadGroup(entry, new HashSet<Group>());
            PermissionDAO.setWriteGroup(entry, new HashSet<Group>());
            PermissionDAO.setReadUser(entry, new HashSet<Account>());
            PermissionDAO.setWriteUser(entry, new HashSet<Account>());
        } catch (ManagerException e) {
            throw new ControllerException("Failed to change permissions for deleted entry.", e);
        }
        entry.setModificationTime(Calendar.getInstance().getTime());

        try {
            EntryDAO.save(entry);
        } catch (ManagerException e1) {
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
            if (verifier.hasReadPermissionsById(id, account)) {
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

    public long getOwnerEntryCount(Account account) throws ControllerException {
        try {
            return dao.getOwnerEntryCount(account.getEmail());
        } catch (ManagerException e) {
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

    public int getOwnerEntryCountBy(String email) {
        return 0;
    }

    public LinkedList<EntryInfo> retrieveEntriesByIdSetSort(LinkedList<Long> entryIds,
                                                            ColumnField field, boolean asc) throws ControllerException {

        List<Entry> entries;

        try {

            switch (field) {
                case TYPE:
                    entries = dao.getEntriesByIdSetSortByType(entryIds, asc);
                    break;

                case PART_ID:
                    entries = dao.getEntriesByIdSetSortByPartNumber(entryIds, asc);
                    break;

                case STATUS:
                    entries = dao.getEntriesByIdSetSortByStatus(entryIds, asc);
                    break;

                case NAME:
                    entries = dao.getEntriesByIdSetSortByName(entryIds, asc);
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
            EntryInfo view = EntryViewFactory.createTableViewData(entry);
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

    public long retrieveEntryByType(Account account, String type) throws ControllerException {
        try {
            return dao.retrieveEntryByType(type);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }
}
