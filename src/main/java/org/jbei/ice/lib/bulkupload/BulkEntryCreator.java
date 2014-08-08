package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.BulkUploadDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.bulkupload.EditMode;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryEditor;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.servlet.InfoToModelFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Creates entries for bulk uploads
 *
 * @author Hector Plahar
 */
public class BulkEntryCreator {

    private final BulkUploadDAO dao;
    private final EntryDAO entryDAO;
    private final EntryCreator creator;
    private final AccountController accountController;
    private final EntryController entryController;
    private final BulkUploadAuthorization authorization;

    public BulkEntryCreator() {
        dao = DAOFactory.getBulkUploadDAO();
        entryDAO = DAOFactory.getEntryDAO();
        creator = new EntryCreator();
        accountController = new AccountController();
        entryController = new EntryController();
        authorization = new BulkUploadAuthorization();
    }

    protected BulkUpload createOrRetrieveBulkUpload(Account account, BulkUploadAutoUpdate autoUpdate,
            EntryType addType) {
        BulkUpload draft = dao.retrieveById(autoUpdate.getBulkUploadId());
        if (draft == null) {
            draft = new BulkUpload();
            draft.setName("Untitled");
            draft.setAccount(account);
            draft.setStatus(BulkUploadStatus.IN_PROGRESS);
            draft.setImportType(addType.toString());
            draft.setCreationTime(new Date(System.currentTimeMillis()));
            draft.setLastUpdateTime(draft.getCreationTime());
            dao.create(draft);
        }
        return draft;
    }

    public PartData createEntry(String userId, long bulkUploadId, PartData data) {
        BulkUpload upload = dao.get(bulkUploadId);
        authorization.expectWrite(userId, upload);
        return createEntryForUpload(userId, data, upload);
    }

    protected PartData createEntryForUpload(String userId, PartData data, BulkUpload upload) {
        Entry entry = InfoToModelFactory.infoToEntry(data);
        entry.setVisibility(Visibility.DRAFT.getValue());
        Account account = accountController.getByEmail(userId);
        entry.setOwner(account.getFullName());
        entry.setOwnerEmail(account.getEmail());

        // check if there is any linked parts. create if so (expect a max of 1)
        if (data.getLinkedParts() != null && data.getLinkedParts().size() > 0) {
            // create linked
            PartData linked = data.getLinkedParts().get(0);
            Entry linkedEntry = InfoToModelFactory.infoToEntry(linked);
            linkedEntry.setVisibility(Visibility.DRAFT.getValue());
            linkedEntry.setOwner(account.getFullName());
            linkedEntry.setOwnerEmail(account.getEmail());
            linkedEntry = entryDAO.create(linkedEntry);

            linked.setId(linkedEntry.getId());
            linked.setModificationTime(linkedEntry.getModificationTime().getTime());
            data.getLinkedParts().clear();
            data.getLinkedParts().add(linked);

            // link to main entry in the database
            entry.getLinkedEntries().add(linkedEntry);
        }

        entry = entryDAO.create(entry);
        upload.getContents().add(entry);

        dao.update(upload);
        data.setId(entry.getId());
        data.setModificationTime(entry.getModificationTime().getTime());
        return data;
    }

    public PartData updateEntry(String userId, long bulkUploadId, long id, PartData data) {
        BulkUpload upload = dao.get(bulkUploadId);
        authorization.expectWrite(userId, upload);

        // todo : check that entry is a part of upload and they are of the same type
        data = doUpdate(data, id);
        if (data == null)
            return null;

        upload.setLastUpdateTime(new Date(data.getModificationTime()));
        dao.update(upload);
        return data;
    }

    protected PartData doUpdate(PartData data, long id) {
        Entry entry = entryDAO.get(id);
        if (entry == null)
            return null;

        entry = InfoToModelFactory.updateEntryField(data, entry);
        entry.setVisibility(Visibility.DRAFT.getValue());
        entry.setModificationTime(new Date(System.currentTimeMillis()));
        entry = entryDAO.update(entry);

        data.setId(id);
        data.setModificationTime(entry.getModificationTime().getTime());

        // check if there is any linked parts. update if so (expect a max of 1)
        if (data.getLinkedParts() == null || data.getLinkedParts().size() == 0)
            return data;

        // recursively update
        PartData linked = doUpdate(data.getLinkedParts().get(0), data.getLinkedParts().get(0).getId());
        data.getLinkedParts().clear();
        if (linked != null)
            data.getLinkedParts().add(linked);

        return data;
    }

    public BulkUploadInfo updateStatus(String userId, long id, BulkUploadStatus status) {
        if (status == null)
            return null;

        // upload is allowed to be null
        BulkUpload upload = dao.get(id);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);
        Date updateTime = new Date(System.currentTimeMillis());
        upload.setLastUpdateTime(updateTime);
        upload.setStatus(status);

        switch (status) {
            case PENDING_APPROVAL:
            default:
                ArrayList<Long> list = dao.getEntryIds(id);
                for (Number l : list) {
                    Entry entry = entryDAO.get(l.longValue());
                    if (entry == null)
                        continue;

                    entry.setVisibility(Visibility.PENDING.getValue());
                    entryDAO.update(entry);
                }
                return dao.update(upload).toDataTransferObject();

            // approved by an administrator
            case APPROVED:
                Account account = accountController.getByEmail(userId);
                if (new BulkUploadController().approveBulkImport(account, id))
                    return upload.toDataTransferObject();
                return null;
        }
    }

    /**
     * Renames the bulk upload referenced by the id in the parameter
     *
     * @param userId unique identifier of user performing action. Must with be an administrator
     *               own the bulk upload
     * @param id     unique identifier referencing the bulk upload
     * @param name   name to assign to the bulk upload
     * @return data transfer object for the bulk upload.
     *         returns null if no
     * @throws org.jbei.ice.lib.access.AuthorizationException
     *          is user performing action doesn't have privileges
     */
    public BulkUploadInfo renameBulkUpload(String userId, long id, String name) {
        BulkUpload upload = dao.get(id);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);

        if (StringUtils.isEmpty(name))
            return upload.toDataTransferObject();

        upload.setName(name);
        upload.setLastUpdateTime(new Date());
        return dao.update(upload).toDataTransferObject();
    }

    /**
     * Creates (or updates) entry based on information in parameters
     *
     * @param userId     unique identifier for user making request
     * @param autoUpdate wrapper for information used to create entry
     * @param addType    type of entry being created
     * @return updated wrapper for information used to create entry. Will contain additional information
     *         such as the unique identifier for the part, if one was created
     * @throws ControllerException
     */
    public BulkUploadAutoUpdate createOrUpdateEntry(String userId, BulkUploadAutoUpdate autoUpdate,
            EntryType addType) {
        Account account = accountController.getByEmail(userId);
        BulkUpload draft = null;

        // for bulk edit, drafts will not exist
        if (autoUpdate.getEditMode() != EditMode.BULK_EDIT) {
            draft = createOrRetrieveBulkUpload(account, autoUpdate, addType);
            autoUpdate.setBulkUploadId(draft.getId());
        }

        // for strain with plasmid this is the strain
        Entry entry = entryDAO.get(autoUpdate.getEntryId());
        Entry otherEntry = null;  // for strain with plasmid this is the entry

        // if entry is null, create entry
        if (entry == null) {
            entry = EntryUtil.createEntryFromType(autoUpdate.getType(), account.getFullName(), account.getEmail());
            if (entry == null)
                return null;

            entry = creator.createEntry(account, entry, null);

            autoUpdate.setEntryId(entry.getId());
            if (draft != null) {
                draft.getContents().add(entry);
            }
        }

        // now update the values (for strain with plasmid, some values are for both
        for (Map.Entry<EntryField, String> set : autoUpdate.getKeyValue().entrySet()) {
            String value = set.getValue();
            EntryField field = set.getKey();

            Entry[] ret = InfoToModelFactory.infoToEntryForField(entry, otherEntry, value, field);
            entry = ret[0];

            if (ret.length == 2) {
                otherEntry = ret[1];
            }
        }

        if (draft != null && draft.getStatus() != BulkUploadStatus.PENDING_APPROVAL) {
            if (otherEntry != null && autoUpdate.getEditMode() != EditMode.BULK_EDIT) {
                if (otherEntry.getVisibility() == null || otherEntry.getVisibility() != Visibility.DRAFT.getValue())
                    otherEntry.setVisibility(Visibility.DRAFT.getValue());

                entryController.update(account, otherEntry);
            }

            if ((entry.getVisibility() == null || entry.getVisibility() != Visibility.DRAFT.getValue())
                    && autoUpdate.getEditMode() != EditMode.BULK_EDIT)
                entry.setVisibility(Visibility.DRAFT.getValue());
        }

        EntryEditor editor = new EntryEditor();
        // set the plasmids and update
        if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.toString())
                && entry.getLinkedEntries().isEmpty()) {
            Strain strain = (Strain) entry;
            editor.setStrainPlasmids(account, strain, strain.getPlasmids());
        }

        entryController.update(account, entry);

        // update bulk upload. even if no new entry was created, entries belonging to it was updated
        if (draft != null) {
            draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
            autoUpdate.setLastUpdate(draft.getLastUpdateTime());
            dao.update(draft);
        }
        return autoUpdate;
    }

    public BulkUploadInfo createOrUpdateEntries(String userId, long draftId, List<PartData> data) {
        BulkUpload draft = dao.retrieveById(draftId);
        if (draft == null)
            return null;

        // check permissions
        authorization.expectWrite(userId, draft);

        BulkUploadInfo uploadInfo = draft.toDataTransferObject();

        for (PartData datum : data) {
            int index = datum.getIndex();

            if (datum.getId() > 0) {
                datum = doUpdate(datum, datum.getId());
            } else {
                datum = createEntryForUpload(userId, datum, draft);
            }

            if (datum == null)
                return null;

            datum.setIndex(index);
            uploadInfo.getEntryList().add(datum);
        }
        return uploadInfo;
    }
}
