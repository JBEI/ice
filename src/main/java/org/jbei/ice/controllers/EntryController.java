package org.jbei.ice.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;

/**
 * ABI to manipulate {@link Entry}s.
 * 
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 * 
 */
public class EntryController extends Controller {
    public EntryController(Account account) {
        super(account, new EntryPermissionVerifier());
    }

    /**
     * Create an entry in the database.
     * <p>
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

    /**
     * Create an entry in the database.
     * <p>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps as necessary.
     * Optionally set the record globally visible or schedule an index rebuild.
     * 
     * @param entry
     * @param scheduleIndexRebuild
     *            Set true to schedule search index rebuild.
     * @param doAddReadGroup
     *            Set true to make record globally visible.
     * @return entry that was saved in the database.
     * @throws ControllerException
     */
    public Entry createEntry(Entry entry, boolean scheduleIndexRebuild, boolean doAddReadGroup)
            throws ControllerException {
        Entry createdEntry = null;

        try {
            createdEntry = EntryManager.createEntry(entry);

            if (doAddReadGroup) {
                PermissionManager.addReadGroup(createdEntry, GroupManager.getEverybodyGroup());
            }

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleBlastIndexRebuildJob();
                ApplicationContoller.scheduleSearchIndexRebuildJob();
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
    public Entry get(long id) throws ControllerException, PermissionException {
        Entry entry = null;

        try {
            entry = EntryManager.get(id);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (!hasReadPermission(entry)) {
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
    public Entry getByRecordId(String recordId) throws ControllerException, PermissionException {
        Entry entry = null;

        try {
            entry = EntryManager.getByRecordId(recordId);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (entry == null) {
            return null;
        }

        if (!hasReadPermission(entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by part number.
     * <p>
     * Throws exception if multiple entries have the same part number.
     * 
     * @param partNumber
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByPartNumber(String partNumber) throws ControllerException, PermissionException {
        Entry entry = null;

        try {
            entry = EntryManager.getByPartNumber(partNumber);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !hasReadPermission(entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by name.
     * <p>
     * Throws exception if multiple entries have the same name.
     * 
     * @param name
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByName(String name) throws ControllerException, PermissionException {

        Entry entry = null;

        try {
            entry = EntryManager.getByName(name);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (entry != null && !hasReadPermission(entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    /**
     * Retrieve {@link Entry} from the database by an identifier.
     * <p>
     * The identifier can be the database id, record id, or by part number. Will throw an exception
     * if multiple entries match the query identifier.
     * 
     * @param identifier
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByIdentifier(String identifier) throws ControllerException, PermissionException {
        long entryId = 0;

        Entry entry = null;

        try {
            entryId = Long.parseLong(identifier);

            entry = EntryManager.get(entryId);
        } catch (NumberFormatException e) {
            entry = null;
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (entry != null) {
            if (!hasReadPermission(entry)) {
                throw new PermissionException("No read permissions for entry!");
            }

            return entry;
        }

        // Not a number. Perhaps it's a part number?
        try {
            entry = EntryManager.getByPartNumber(identifier);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (entry != null) {
            if (!hasReadPermission(entry)) {
                throw new PermissionException("No read permissions for entry!");
            }

            return entry;
        }

        // Not a number. Perhaps it's a recordId?
        try {
            entry = EntryManager.getByRecordId(identifier);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        if (entry != null) {
            if (!hasReadPermission(entry)) {
                throw new PermissionException("No read permissions for entry!");
            }

            return entry;
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
    public boolean hasReadPermission(Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to check read permissions for null entry!");
        }

        return getEntryPermissionVerifier().hasReadPermissions(entry, getAccount());
    }

    /**
     * Checks if the user has write permission to the given {@link Entry}
     * 
     * @param entry
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to check write permissions for null entry!");
        }

        return getEntryPermissionVerifier().hasWritePermissions(entry, getAccount());
    }

    /**
     * Checks if the user has read permission to the {@link Entry} with the given database id.
     * 
     * @param entryId
     * @return True if user has read permission.
     * @throws ControllerException
     */
    public boolean hasReadPermissionById(long entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasReadPermissionsById(entryId, getAccount());
    }

    /**
     * Checks if the user has write permission to the {@link Entry} with the given database id.
     * 
     * @param entryId
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermissionById(long entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasWritePermissionsById(entryId, getAccount());
    }

    /**
     * Checks if the user has read permission to the {@link Entry} with the given record id.
     * 
     * @param entryId
     * @return True if user has read permission.
     * @throws ControllerException
     */
    public boolean hasReadPermissionByRecordId(String entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasReadPermissionsByRecordId(entryId, getAccount());
    }

    /**
     * Checks if the user has write permission to the {@link Entry} with the given record id.
     * 
     * @param entryId
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermissionByRecordId(String entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasWritePermissionsByRecordId(entryId, getAccount());
    }

    /**
     * Checks if the given entry has a {@link Sequence} associated with it.
     * 
     * @param entry
     * @return True if sequence is associated.
     * @throws ControllerException
     */
    public boolean hasSequence(Entry entry) throws ControllerException {
        boolean result = false;

        Sequence sequence = null;
        try {
            sequence = SequenceManager.getByEntry(entry);

            result = (sequence != null);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Checks if the given entry has {@link Sample}s associated with it.
     * 
     * @param entry
     * @return True if there are associated samples.
     * @throws ControllerException
     */
    public boolean hasSamples(Entry entry) throws ControllerException {
        boolean result = false;

        try {
            ArrayList<Sample> samples = SampleManager.getSamplesByEntry(entry);

            result = (samples == null) ? false : samples.size() > 0;
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Checks if the given entry has {@link Attachment}s associated with it.
     * 
     * @param entry
     * @return True if there are associated attachments.
     * @throws ControllerException
     */
    public boolean hasAttachments(Entry entry) throws ControllerException {
        boolean result = false;

        try {
            ArrayList<Attachment> attachments = AttachmentManager.getByEntry(entry);

            result = (attachments == null) ? false : attachments.size() > 0;
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Retrieve all entries from the database. Use with caution, as query can be slow.
     * 
     * @return List of all {@link Entry entries}.
     * @throws ControllerException
     */
    public List<Entry> getEntries() throws ControllerException {
        return getEntries(0, -1, null, true);
    }

    /**
     * Retrieve entries from the database sorted by the field, offset and limited.
     * 
     * @param offset
     * @param limit
     * @param field
     *            Field to sort on
     * @param ascending
     *            True if ascending
     * @return List of entries.
     * @throws ControllerException
     */
    public List<Entry> getEntries(int offset, int limit, String field, boolean ascending)
            throws ControllerException {
        List<Entry> entries = null;

        try {
            ArrayList<Long> entryIds = EntryManager.getEntries(field, ascending);
            ArrayList<Long> filteredEntries = filterEntriesByPermissionAndOffsetLimit(entryIds,
                offset, limit);
            entries = EntryManager.getEntriesByIdSetSort(filteredEntries, field, ascending);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return entries;
    }

    /**
     * Retrieve from the database entries owned the owner's email.
     * 
     * @param owner
     * @param offset
     * @param limit
     * @param field
     *            Field to sort on.
     * @param ascending
     *            True if ascending.
     * @return List of entries.
     * @throws ControllerException
     */
    public List<Entry> getEntriesByOwner(String owner, int offset, int limit, String field,
            boolean ascending) throws ControllerException {
        List<Entry> entries = null;

        try {
            ArrayList<Long> ownerEntries = EntryManager.getEntriesByOwnerSort(owner, field,
                ascending);
            ArrayList<Long> filteredEntries = filterEntriesByPermissionAndOffsetLimit(ownerEntries,
                offset, limit);

            entries = EntryManager.getEntriesByIdSetSort(filteredEntries, field, ascending);

        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return entries;
    }

    /**
     * Retrieve entries from the database with the specified database id's.
     * 
     * @param ids
     *            List of id's.
     * @return ArrayList of entries.
     * @throws ControllerException
     */
    public ArrayList<Entry> getEntriesByIdSet(List<Long> ids) throws ControllerException {
        ArrayList<Entry> entries = null;
        try {
            ids = filterEntriesByPermission(ids);
            entries = EntryManager.getEntriesByIdSet(ids);

        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
        return entries;

    }

    /**
     * Retrieve the number of publicly visible entries (Entries visible to the Everybody group).
     * 
     * @return Number of entries.
     * @throws ControllerException
     */
    public long getNumberOfVisibleEntries() throws ControllerException {
        long numberOfVisibleEntries = 0;

        try {
            numberOfVisibleEntries = EntryManager.getNumberOfVisibleEntries();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return numberOfVisibleEntries;
    }

    /**
     * Retrieve the number of entries owned by the owner email.
     * 
     * @param owner
     *            Email of the account.
     * @return Number of entries.
     * @throws ControllerException
     */
    public long getNumberOfEntriesByOwner(String owner) throws ControllerException {

        try {
            return EntryManager.getEntriesByOwner(owner).size();
        } catch (ManagerException e) {
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
    public Entry save(Entry entry) throws ControllerException, PermissionException {
        return save(entry, true);
    }

    /**
     * Save the entry into the database. Optionally schedule an index rebuild.
     * 
     * @param entry
     * @param scheduleIndexRebuild
     *            Set True to schedule index rebuild.
     * @return Entry saved into the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry save(Entry entry, boolean scheduleIndexRebuild) throws ControllerException,
            PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry!");
        }

        if (!hasWritePermission(entry)) {
            throw new PermissionException("No write permission for entry!");
        }

        Entry savedEntry = null;

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());

            savedEntry = EntryManager.save(entry);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleSearchIndexRebuildJob();
                ApplicationContoller.scheduleBlastIndexRebuildJob();
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
    public void delete(Entry entry) throws ControllerException, PermissionException {
        delete(entry, true);
    }

    /**
     * Delete the entry in the database. Optionally schedule an index rebuild.
     * 
     * @param entry
     * @param scheduleIndexRebuild
     *            True if index rebuild is scheduled.
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Entry entry, boolean scheduleIndexRebuild) throws ControllerException,
            PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry");
        }
        if (!hasWritePermission(entry)) {
            throw new PermissionException("No write permission for entry");
        }
        String deletionString = "This entry is deleted. It was owned by " + entry.getOwnerEmail()
                + "\n";
        entry.setLongDescription(deletionString + entry.getLongDescription());
        entry.setOwnerEmail(PopulateInitialDatabase.systemAccountEmail);
        // save(entry, true); // Cannot use save, as owner has changed. Must call manager directly
        try {
            PermissionManager.setReadGroup(entry, new HashSet<Group>());
            PermissionManager.setWriteGroup(entry, new HashSet<Group>());
            PermissionManager.setReadUser(entry, new HashSet<Account>());
            PermissionManager.setWriteUser(entry, new HashSet<Account>());
        } catch (ManagerException e) {
            throw new ControllerException("Failed to change permissions for deleted entry.", e);
        }
        entry.setModificationTime(Calendar.getInstance().getTime());

        try {
            EntryManager.save(entry);
        } catch (ManagerException e1) {
            throw new ControllerException("Failed to save entry deletion", e1);
        }

        if (scheduleIndexRebuild) {
            ApplicationContoller.scheduleSearchIndexRebuildJob();
            ApplicationContoller.scheduleBlastIndexRebuildJob();
        }

    }

    /**
     * Retrieve the {@link EntryPermissionVerifier}.R
     * 
     * @return entryPermissionVerifier.
     */
    protected EntryPermissionVerifier getEntryPermissionVerifier() {
        return (EntryPermissionVerifier) getPermissionVerifier();
    }

    /**
     * Filter {@link Entry} id's for display.
     * <p>
     * Given an ArrayList of entry database id's, keep only id's that the user has read access to,
     * and offset and limited for paging.
     * 
     * @param ids
     * @param offset
     * @param limit
     * @return ArrayList of entry id's.
     * @throws ControllerException
     */
    protected ArrayList<Long> filterEntriesByPermissionAndOffsetLimit(ArrayList<Long> ids,
            long offset, long limit) throws ControllerException {
        ArrayList<Long> entryIds = new ArrayList<Long>();

        long skip = 0;
        long counter = 0;
        for (Long entryId : ids) {
            if (hasReadPermissionById(entryId)) {
                if (offset > skip) {
                    skip++;

                    continue;
                }

                if (limit == -1) {
                    entryIds.add(entryId);
                } else {
                    if (counter < limit) {
                        entryIds.add(entryId);

                        counter++;
                    } else {
                        break;
                    }
                }
            }
        }

        return entryIds;
    }

    /**
     * Filter {@link Entry} id's for display.
     * <p>
     * Given a List of entry id's, keep only id's that user has read access to.
     * 
     * @param ids
     * @return List of Entry ids.
     * @throws ControllerException
     */
    public List<Long> filterEntriesByPermission(List<Long> ids) throws ControllerException {
        ArrayList<Long> result = new ArrayList<Long>();
        for (Long id : ids) {
            if (hasReadPermissionById(id)) {
                result.add(id);
            }
        }
        return result;
    }
}
