package org.jbei.ice.lib.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.entry.sequence.PartSequence;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

/**
 * @author Hector Plahar
 */
public class EntryCreator extends HasEntry {

    private final EntryDAO dao;
    private final PermissionDAO permissionDAO;
    private final SequenceDAO sequenceDAO;
    private final EntryAuthorization entryAuthorization;

    public EntryCreator() {
        dao = DAOFactory.getEntryDAO();
        permissionDAO = DAOFactory.getPermissionDAO();
        sequenceDAO = DAOFactory.getSequenceDAO();
        this.entryAuthorization = new EntryAuthorization();
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
    public Entry createEntry(Account account, Entry entry, ArrayList<AccessPermission> accessPermissions) {
        if (entry.getRecordId() == null) {
            entry.setRecordId(Utils.generateUUID());
            entry.setVersionId(entry.getRecordId());
        }
        entry.setCreationTime(Calendar.getInstance().getTime());
        entry.setModificationTime(entry.getCreationTime());

        if (StringUtils.isEmpty(entry.getOwner()))
            entry.setOwner(account.getFullName());

        if (StringUtils.isEmpty(entry.getOwnerEmail()))
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

        if (entry.getStatus() == null)
            entry.setStatus("");

        if (entry.getBioSafetyLevel() == null)
            entry.setBioSafetyLevel(0);

        entry = dao.create(entry);

        // check for pi
        String piEmail = entry.getPrincipalInvestigatorEmail();
        if (StringUtils.isNotEmpty(piEmail)) {
            Account pi = DAOFactory.getAccountDAO().getByEmail(piEmail);
            if (pi != null) {
                // add write permission for the PI
                addWritePermission(pi, entry);
            }
        }

        // add write permissions for owner
        addWritePermission(account, entry);

        // add read permission for all public groups
        ArrayList<Group> groups = new GroupController().getAllPublicGroupsForAccount(account);
        for (Group group : groups) {
            addReadPermission(null, group, entry);
        }

        if (accessPermissions != null) {
            for (AccessPermission accessPermission : accessPermissions) {
                if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT) {
                    Account accessAccount = DAOFactory.getAccountDAO().get(accessPermission.getArticleId());
                    // add account read permission
                    addReadPermission(accessAccount, null, entry);
                } else {
                    // add group read permission
                    Group group = DAOFactory.getGroupDAO().get(accessPermission.getArticleId());
                    addReadPermission(null, group, entry);
                }
            }
        }

        // rebuild blast database
        if (sequenceDAO.hasSequence(entry.getId())) {
            RebuildBlastIndexTask task = new RebuildBlastIndexTask(true);
            IceExecutorService.getInstance().runTask(task);
        }

        return entry;
    }

    private void addReadPermission(Account account, Group group, Entry entry) {
        Permission permission = new Permission();
        if (group != null)
            permission.setGroup(group);
        else if (account != null)
            permission.setAccount(account);
        else
            return; // either group or account required

        permission.setEntry(entry);
        entry.getPermissions().add(permission); // triggers the permission class bridge
        permission.setCanRead(true);
        permissionDAO.create(permission);
    }

    private void addWritePermission(Account account, Entry entry) {
        Permission permission = new Permission();
        permission.setCanWrite(true);
        permission.setEntry(entry);
        entry.getPermissions().add(permission); // triggers the permission class bridge
        permission.setAccount(account);
        permissionDAO.create(permission);
    }

    /**
     * Creates a new entry using the passed data
     *
     * @param userId unique identifier for user creating entry
     * @param part   data used to create new part
     * @return new part data id and record id information
     */
    public PartData createPart(String userId, PartData part) {
        Entry entry = InfoToModelFactory.infoToEntry(part);
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        // linked entries can be a combination of new and existing parts
        if (part.getLinkedParts() != null) {
            for (PartData data : part.getLinkedParts()) {
                Entry linked;
                if (data.getId() > 0) {
                    linked = dao.get(data.getId());
                    if (linked == null || !entryAuthorization.canRead(userId, linked)) {
                        continue;
                    }

                    // TODO : may contain new information e.g. if the sequence is uploaded before
                    // TODO : this entry was created then the general information is added here
                    linked = InfoToModelFactory.updateEntryField(data, linked);
                    linked.setVisibility(Visibility.OK.getValue());

                    if (entryAuthorization.canWrite(userId, linked)) {
                        // then update
                    }
                } else {
                    // create new linked (can only do one deep)
                    Entry linkedEntry = InfoToModelFactory.infoToEntry(data);
                    linked = createEntry(account, linkedEntry, data.getAccessPermissions());
                    setCustomFieldValuesForPart(linkedEntry, data);
                }

                entry.getLinkedEntries().add(linked);
            }
        }

        entry = createEntry(account, entry, part.getAccessPermissions());
        setCustomFieldValuesForPart(entry, part);
        PartData partData = new PartData(part.getType());
        partData.setId(entry.getId());
        partData.setRecordId(entry.getRecordId());
        return partData;
    }

    private void setCustomFieldValuesForPart(Entry entry, PartData data) {
        if (data == null || data.getCustomEntryFields() == null)
            return;

        CustomEntryFieldDAO dao = DAOFactory.getCustomEntryFieldDAO();

        for (CustomEntryField customEntryField : data.getCustomEntryFields()) {
            if (customEntryField.getFieldType() == FieldType.EXISTING)
                continue;

            CustomEntryFieldValueModel model = new CustomEntryFieldValueModel();
            model.setEntry(entry);
            CustomEntryFieldModel customEntryFieldModel = dao.get(customEntryField.getId());
            if (customEntryFieldModel == null) {

                // try again with label and type
                Optional<CustomEntryFieldModel> optional = dao.getLabelForType(customEntryField.getEntryType(), customEntryField.getLabel());
                if (!optional.isPresent()) {
                    Logger.error("Could not retrieve custom field with id " + customEntryField.getId());
                    continue;
                }
                customEntryFieldModel = optional.get();
            }

            model.setField(customEntryFieldModel);
            model.setValue(customEntryField.getValue());
            DAOFactory.getCustomEntryFieldValueDAO().create(model);
        }
    }

    /**
     * Creates a copy of
     *
     * @param userId   identifier for user making request
     * @param sourceId unique identifier for part acting as source of copy. Can be the part id, uuid or id
     * @return wrapper around the id and record id of the newly created entry
     * @throws IllegalArgumentException if the source part for the copy cannot be located using the identifier
     */
    public PartData copyPart(String userId, String sourceId) {
        Entry entry = getEntry(sourceId);
        if (entry == null)
            throw new IllegalArgumentException("Could not retrieve entry \"" + sourceId + "\" for copy");

        // check permission (expecting read permission)
        entryAuthorization.expectRead(userId, entry);

        Sequence sequence = null;
        if (sequenceDAO.hasSequence(entry.getId())) {
            sequence = sequenceDAO.getByEntry(entry);
        }

        // copy to data model and back ??
        PartData partData = ModelToInfoFactory.getInfo(entry);
        if (partData == null)
            throw new IllegalArgumentException("Cannot retrieve information for entry \"" + sourceId + "\"");

        entry = InfoToModelFactory.infoToEntry(partData);

        // create entry
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        entry.setName(entry.getName() + " (copy)");
        entry.setRecordId(Utils.generateUUID());
        entry.setVersionId(entry.getRecordId());
        entry.setOwnerEmail(account.getEmail());
        entry.setOwner(account.getFullName());
        entry = createEntry(account, entry, new ArrayList<>());

        // check sequence
        if (sequence != null) {
            PartSequence partSequence = new PartSequence(userId, sourceId);
            new PartSequence(userId, entry.getPartNumber()).save(partSequence.get());
        }

        PartData copy = new PartData(EntryType.nameToType(entry.getRecordType()));
        copy.setId(entry.getId());
        copy.setRecordId(entry.getRecordId());
        return copy;
    }
}
