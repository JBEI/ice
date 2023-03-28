package org.jbei.ice.entry;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.dto.folder.FolderAuthorization;
import org.jbei.ice.dto.folder.FolderType;
import org.jbei.ice.dto.search.SearchQuery;
import org.jbei.ice.dto.search.SearchResult;
import org.jbei.ice.dto.search.SearchResults;
import org.jbei.ice.entry.sequence.PartSequence;
import org.jbei.ice.executor.IceExecutorService;
import org.jbei.ice.folder.collection.CollectionEntries;
import org.jbei.ice.folder.collection.CollectionType;
import org.jbei.ice.group.GroupController;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.search.SearchIndexes;
import org.jbei.ice.search.blast.Action;
import org.jbei.ice.search.blast.RebuildBlastIndexTask;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.InfoToModelFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldDAO;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldValueDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;
import org.jbei.ice.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Hector Plahar
 */
public class Entries extends HasEntry {

    private final EntryDAO dao;
    private final String userId;
    private final EntryAuthorization authorization;
    private final SequenceDAO sequenceDAO;
    private final CustomEntryFieldValueDAO entryFieldValueDAO;

    /**
     * @param userId unique identifier for user creating permissions. Must have write privileges on the entry
     *               if one exists
     */
    public Entries(String userId) {
        this.dao = DAOFactory.getEntryDAO();
        this.authorization = new EntryAuthorization();
        this.userId = userId;
        sequenceDAO = DAOFactory.getSequenceDAO();
        entryFieldValueDAO = DAOFactory.getCustomEntryFieldValueDAO();
    }

    /**
     * Update the information associated with the specified part.<br>
     * <b>Note</b> that any missing information will be deleted from the original entry.
     * In other words, if the part referenced by id <code>partId</code> has an alias value
     * of <code>alias</code> and the part object passed in the parameter does not contain this value,
     * when this method returns, the original entry's alias field will be removed.
     *
     * @param partId   unique identifier for part being updated. This overrides the id in the partData object
     * @param partData information to update part with
     * @return unique identifier for part that was updated
     * @throws IllegalArgumentException if the entry associated with the partId cannot be located
     */
    public long update(long partId, PartData partData) {
        Entry entry = dao.get(partId);
        if (entry == null || partData.getFields() == null)
            throw new IllegalArgumentException("Could not retrieve entry with id " + partId);

        authorization.expectWrite(userId, entry);

        entry.setModificationTime(Calendar.getInstance().getTime());
        entry = dao.update(entry);
        partData.setModificationTime(entry.getModificationTime().getTime());

        if (entry.getVisibility() == Visibility.OK.getValue()) {
            EntryHistory history = new EntryHistory(userId, partId);
            history.addEdit();
        }

        // update custom fields
        updateOrCreateCustomFieldValues(entry, partData);

        return entry.getId();
    }

    /**
     * Update the specified entry using information contained in the field
     */
    public void updateFieldValue(long partId, EntryField entryField) {
        Entry existing = dao.get(partId);
        if (existing == null)
            throw new IllegalArgumentException("Could not retrieve entry with id " + partId);

        if (entryField.getFieldType() == null)
            throw new IllegalArgumentException("Invalid entry field label");

        authorization.expectWrite(userId, existing);
    }

    private void updateOrCreateCustomFieldValues(Entry entry, PartData data) {
        if (data == null || data.getCustomEntryFields() == null || entry == null)
            return;

        for (CustomEntryField customEntryField : data.getCustomEntryFields()) {
            // skip existing because that is covered by "existing" fields
            if (customEntryField.getFieldType() == FieldType.EXISTING)
                continue;

            updateCustomField(entry, customEntryField);
        }
    }

    private void updateCustomField(Entry entry, CustomEntryField field) {
        CustomEntryFieldDAO dao = DAOFactory.getCustomEntryFieldDAO();
        CustomEntryFieldModel customEntryFieldModel = dao.get(field.getId());
        if (customEntryFieldModel == null) {
            // get details about custom field (note: this is different from value)
            if (field.getEntryType() == null) {
                field.setEntryType(EntryType.nameToType(entry.getRecordType()));
            }

            // try again with label and type
            Optional<CustomEntryFieldModel> optional = dao.getLabelForType(field.getEntryType(), field.getLabel());
            if (optional.isEmpty()) {
                Logger.error("Could not retrieve custom field with id " + field.getId());
                return;
            }
            customEntryFieldModel = optional.get();
        }

        CustomEntryFieldValueModel model = entryFieldValueDAO.getByFieldAndEntry(entry, customEntryFieldModel);
        if (model == null) {
            // create new
            model = new CustomEntryFieldValueModel();
            model.setEntry(entry);
            model.setField(customEntryFieldModel);
            model.setValue(field.getValue());
            entryFieldValueDAO.create(model);
        } else {
            model.setValue(field.getValue());
            entryFieldValueDAO.update(model);
        }
    }

    public List<Long> updateVisibility(List<Long> entryIds, Visibility visibility) {
        List<Long> updated = new ArrayList<>();
        for (long entryId : entryIds) {
            Entry entry = dao.get(entryId);
            if (entry.getVisibility() == visibility.getValue())
                continue;

            if (!authorization.canWrite(userId, entry))
                continue;

            entry.setVisibility(visibility.getValue());
            dao.update(entry);
            updated.add(entryId);
        }
        return updated;
    }

    public List<Long> getEntriesFromSelectionContext(EntrySelection context) {
        boolean all = context.isAll();
        EntryType entryType = context.getEntryType();

        if (context.getSelectionType() == null)
            return context.getEntries();

        switch (context.getSelectionType()) {
            default:
            case FOLDER:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    long folderId = Long.decode(context.getFolderId());
                    return getFolderEntries(folderId, all, entryType);
                }

            case SEARCH:
                SearchQuery query = context.getSearchQuery();
                query.getParameters().setRetrieveCount(Integer.MAX_VALUE);
                return getSearchResults(query);

            case COLLECTION:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    return getCollectionEntries(context.getFolderId(), all, entryType);
                }
        }
    }

    /**
     * Validate list of entries in a csv file either via names or partnumbers
     *
     * @param stream    csv file input stream
     * @param checkName whether to check names or part numbers
     */
    public List<ParsedEntryId> validateEntries(InputStream stream, boolean checkName) throws IOException {
        List<ParsedEntryId> accepted = new ArrayList<>();
        EntryAuthorization authorization = new EntryAuthorization();

        try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String[]> results = reader.readAll();

            for (String[] result : results) {
                if (result[0].isEmpty())
                    continue;

                List<Entry> entries;
                if (checkName) {
                    entries = dao.getByName(result[0].trim());
                } else {
                    Entry entry = dao.getByPartNumber(result[0].trim());
                    entries = new ArrayList<>(1);
                    if (entry != null)
                        entries.add(entry);
                }

                if (entries.isEmpty())
                    accepted.add(new ParsedEntryId(result[0], null));
                else {
                    for (Entry e : entries) {
                        if (!authorization.canRead(this.userId, e)) {
                            accepted.add(new ParsedEntryId(result[0], null));
                            continue;
                        }
                        PartData partData = new PartData(EntryType.nameToType(e.getRecordType()));
                        partData.setPartId(e.getPartNumber());
                        partData.setId(e.getId());
                        accepted.add(new ParsedEntryId(result[0], partData));
                    }
                }
            }
        } catch (CsvException exception) {
            Logger.error("Exception reading file: " + exception);
        }
        return accepted;
    }

    private List<Long> getCollectionEntries(String collection, boolean all, EntryType type) {
        if (collection == null || collection.isEmpty())
            return null;

        CollectionType collectionType = CollectionType.valueOf(collection.trim().toUpperCase());
        CollectionEntries collectionEntries = new CollectionEntries(userId, collectionType);
        if (all)
            type = null;
        return collectionEntries.getEntriesById(type);
    }

    // todo : folder controller
    private List<Long> getFolderEntries(long folderId, boolean all, EntryType type) {
        Folder folder = DAOFactory.getFolderDAO().get(folderId);
        FolderAuthorization folderAuthorization = new FolderAuthorization();
        folderAuthorization.expectRead(userId, folder);

        if (all)
            type = null;

        boolean visibleOnly = folder.getType() != FolderType.TRANSFERRED;
        return DAOFactory.getFolderDAO().getFolderContentIds(folderId, type, visibleOnly);
    }

    private List<Long> getSearchResults(SearchQuery searchQuery) {
        SearchIndexes searchIndexes = new SearchIndexes();
        SearchResults searchResults = searchIndexes.runSearch(userId, searchQuery);
        // todo : inefficient: have search return ids only
        List<Long> results = new LinkedList<>();
        for (SearchResult result : searchResults.getResults()) {
            results.add(result.getEntryInfo().getId());
        }
        return results;
    }

    /**
     * Creates a copy of referenced part
     *
     * @param sourceId unique identifier for part acting as source of copy. Can be the part id, uuid or id
     * @return wrapper around the id and record id of the newly created entry
     * @throws IllegalArgumentException if the source part for the copy cannot be located using the identifier
     */
    public PartData copy(String sourceId) {
        Entry entry = getEntry(sourceId);
        if (entry == null)
            throw new IllegalArgumentException("Could not retrieve entry \"" + sourceId + "\" for copy");

        // check permission (expecting read permission)
        authorization.expectRead(userId, entry);

        Sequence sequence = null;
        if (sequenceDAO.hasSequence(entry.getId())) {
            sequence = sequenceDAO.getByEntry(entry);
        }

        // copy to data model and back ??
        PartData partData = ModelToInfoFactory.getInfo(entry);

        entry = InfoToModelFactory.infoToEntry(partData);

        // create entry
        AccountModel account = DAOFactory.getAccountDAO().getByEmail(userId);
        entry.setRecordId(Utils.generateUUID());
        entry.setOwnerEmail(account.getEmail());
        entry.setOwner(account.getFullName());
        entry = createEntry(account, entry, new ArrayList<>());

        // check sequence
        if (sequence != null) {
            PartSequence partSequence = new PartSequence(userId, sourceId);
            new PartSequence(userId, entry.getPartNumber()).save(partSequence.get(true));
        }

        PartData copy = new PartData(EntryType.nameToType(entry.getRecordType()));
        copy.setId(entry.getId());
        copy.setRecordId(entry.getRecordId());
        return copy;
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps.
     * Optionally set the record globally visible or schedule an index rebuild.
     *
     * @param account           account of user creating entry
     * @param entry             entry record being created
     * @param accessPermissions list of permissions to associate with created entry
     * @return entry that was saved in the database.
     */
    private Entry createEntry(AccountModel account, Entry entry, ArrayList<AccessPermission> accessPermissions) {
        if (entry.getRecordId() == null) {
            entry.setRecordId(Utils.generateUUID());
        }
        entry.setCreationTime(Calendar.getInstance().getTime());
        entry.setModificationTime(entry.getCreationTime());

        if (StringUtils.isEmpty(entry.getOwner()))
            entry.setOwner(account.getFullName());

        if (StringUtils.isEmpty(entry.getOwnerEmail()))
            entry.setOwnerEmail(account.getEmail());

        entry = dao.create(entry);

        // set permissions
        EntryPermissions permissions = new EntryPermissions(entry.getRecordId(), userId);


        // add write permissions for owner
        permissions.addAccount(account.getEmail(), true);

        // add read permission for all public groups
        ArrayList<Group> groups = new GroupController().getAllPublicGroupsForAccount(account);
        for (Group group : groups) {
            permissions.addGroup(group.getId(), false);
        }


        if (accessPermissions != null) {
            for (AccessPermission accessPermission : accessPermissions) {
                permissions.addAccount(accessPermission);
            }
        }

        // rebuild blast database
        if (sequenceDAO.hasSequence(entry.getId())) {
            RebuildBlastIndexTask task = new RebuildBlastIndexTask(Action.CREATE, entry.getPartNumber());
            IceExecutorService.getInstance().runTask(task);
        }

        return entry;
    }

    /**
     * Create new part with the minimum requirements and a status of DRAFT
     */
    public PartData createNew(PartData part) {
        Entry entry = new Entry();
        entry.setRecordType(part.getType().getName());
        entry.setRecordId(UUID.randomUUID().toString());
        entry.setVisibility(Visibility.DRAFT.getValue());
        entry.setCreationTime(new Date());
        AccountModel account = DAOFactory.getAccountDAO().getByEmail(this.userId);
        entry.setOwner(account.getFullName());
        entry.setOwnerEmail(account.getEmail());

        entry = this.dao.create(entry);

        // todo : if parts has list of new, send to EntryFieldValues

        return entry.toDataTransferObject();
    }

    /**
     * Creates a new entry using the passed data
     *
     * @param part data used to create new part
     * @return new part data id and record id information
     */
    public PartData create(PartData part) {
        Entry entry = InfoToModelFactory.infoToEntry(part);
        AccountModel account = DAOFactory.getAccountDAO().getByEmail(userId);

        // linked entries can be a combination of new and existing parts
//        if (part.getLinkedParts() != null) {
//            for (PartData data : part.getLinkedParts()) {
//                Entry linked;
//                if (data.getId() > 0) {
//                    linked = dao.get(data.getId());
//                    if (linked == null || !authorization.canRead(userId, linked)) {
//                        continue;
//                    }
//
//                    // TODO : may contain new information e.g. if the sequence is uploaded before
//                    // TODO : this entry was created then the general information is added here
//                    linked = InfoToModelFactory.updateEntryField(data, linked);
//                    linked.setVisibility(Visibility.OK.getValue());
//
//                    if (authorization.canWrite(userId, linked)) {
//                        // then update
//                    }
//                } else {
//                    // create new linked (can only do one deep)
//                    Entry linkedEntry = InfoToModelFactory.infoToEntry(data);
//                    linked = createEntry(account, linkedEntry, data.getAccessPermissions());
//                    updateOrCreateCustomFieldValues(linkedEntry, data);
//                }
//
//                entry.getLinkedEntries().add(linked);
//            }
//        }

        entry = createEntry(account, entry, part.getAccessPermissions());
        updateOrCreateCustomFieldValues(entry, part);
        PartData partData = new PartData(part.getType());
        partData.setId(entry.getId());
        partData.setRecordId(entry.getRecordId());
        for (Entry linked : entry.getLinkedEntries()) {
            PartData linkedData = new PartData(EntryType.nameToType(linked.getRecordType()));
            linkedData.setId(linked.getId());
            partData.getLinkedParts().add(linkedData);
        }
        return partData;
    }
}
