package org.jbei.ice.controllers;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.lang.NotImplementedException;
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
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.lib.query.QueryException;

public class EntryController extends Controller {
    public EntryController(Account account) {
        super(account, new EntryPermissionVerifier());
    }

    public Entry createEntry(Entry entry) throws ControllerException {
        return createEntry(entry, true, true);
    }

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

    public Entry get(int id) throws ControllerException, PermissionException {
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

    public Entry getByIdentifier(String identifier) throws ControllerException, PermissionException {
        int entryId = 0;

        Entry entry = null;

        try {
            entryId = Integer.parseInt(identifier);

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

    public boolean hasReadPermission(Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to check read permissions for null entry!");
        }

        return getEntryPermissionVerifier().hasReadPermissions(entry, getAccount());
    }

    public boolean hasWritePermission(Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to check write permissions for null entry!");
        }

        return getEntryPermissionVerifier().hasWritePermissions(entry, getAccount());
    }

    public boolean hasReadPermissionById(int entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasReadPermissionsById(entryId, getAccount());
    }

    public boolean hasWritePermissionById(int entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasWritePermissionsById(entryId, getAccount());
    }

    public boolean hasReadPermissionByRecordId(String entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasReadPermissionsByRecordId(entryId, getAccount());
    }

    public boolean hasWritePermissionByRecordId(String entryId) throws ControllerException {
        return getEntryPermissionVerifier().hasWritePermissionsByRecordId(entryId, getAccount());
    }

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

    public ArrayList<Entry> getEntries() throws ControllerException {
        return getEntries(0, -1, null, true);
    }

    public ArrayList<Entry> getEntries(int offset, int limit, String field, boolean ascending)
            throws ControllerException {
        ArrayList<Entry> entries = null;

        try {
            entries = EntryManager.getEntriesByIdSet(filterEntriesByPermissionAndOffsetLimit(
                EntryManager.getEntries(field, ascending), offset, limit));
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return entries;
    }

    public ArrayList<Entry> getEntriesByOwner(String owner, int offset, int limit)
            throws ControllerException {
        ArrayList<Entry> entries = null;

        try {
            entries = EntryManager.getEntriesByIdSet(filterEntriesByPermissionAndOffsetLimit(
                EntryManager.getEntriesByOwner(owner), offset, limit));
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return entries;
    }

    public ArrayList<Entry> getEntriesByQueries(ArrayList<String[]> filters, int offset, int limit)
            throws ControllerException {
        ArrayList<Entry> entries = null;

        try {
            ArrayList<Integer> queryResultIds = Query.getInstance().query(filters);

            entries = EntryManager.getEntriesByIdSet(filterEntriesByPermissionAndOffsetLimit(
                queryResultIds, offset, limit));
        } catch (QueryException e) {
            throw new ControllerException(e);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return entries;
    }

    public int getNumberOfVisibleEntries() throws ControllerException {
        int numberOfVisibleEntries = 0;

        try {
            numberOfVisibleEntries = EntryManager.getNumberOfVisibleEntries();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return numberOfVisibleEntries;
    }

    public int getNumberOfEntriesByOwner(String owner) throws ControllerException {
        int numberOfEntriesByOwner = 0;

        try {
            ArrayList<Integer> allEntries = EntryManager.getEntriesByOwner(owner);

            for (Integer entryId : allEntries) {
                if (hasReadPermissionById(entryId)) {
                    numberOfEntriesByOwner++;
                }
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return numberOfEntriesByOwner;
    }

    public int getNumberOfEntriesByQueries(ArrayList<String[]> filters) throws ControllerException {
        int numberOfEntriesByQueries = 0;

        try {
            ArrayList<Integer> queryResultIds = Query.getInstance().query(filters);

            for (Integer entryId : queryResultIds) {
                if (hasReadPermissionById(entryId)) {
                    numberOfEntriesByQueries++;
                }
            }
        } catch (QueryException e) {
            throw new ControllerException(e);
        }

        return numberOfEntriesByQueries;
    }

    public Entry save(Entry entry) throws ControllerException, PermissionException {
        return save(entry, true);
    }

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

    public void delete(Entry entry) throws ControllerException, PermissionException {
        delete(entry, true);
    }

    public void delete(Entry entry, boolean scheduleIndexRebuild) throws ControllerException,
            PermissionException {
        throw new NotImplementedException();

        // EntryManager.delete(entry);
    }

    protected EntryPermissionVerifier getEntryPermissionVerifier() {
        return (EntryPermissionVerifier) getPermissionVerifier();
    }

    protected ArrayList<Integer> filterEntriesByPermissionAndOffsetLimit(ArrayList<Integer> ids,
            int offset, int limit) throws ControllerException {
        ArrayList<Integer> entryIds = new ArrayList<Integer>();

        int skip = 0;
        int counter = 0;
        for (Integer entryId : ids) {
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
}
