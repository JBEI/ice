package org.jbei.ice.lib.bulkupload;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jbei.ice.ApplicationController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.BulkUploadDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.bulkupload.EditMode;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryEditor;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.servlet.InfoToModelFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

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
    private final BulkUploadController controller;

    public BulkEntryCreator() {
        dao = DAOFactory.getBulkUploadDAO();
        entryDAO = DAOFactory.getEntryDAO();
        creator = new EntryCreator();
        accountController = new AccountController();
        entryController = new EntryController();
        authorization = new BulkUploadAuthorization();
        controller = new BulkUploadController();
    }

    protected BulkUpload createOrRetrieveBulkUpload(Account account, BulkUploadAutoUpdate autoUpdate,
            EntryType addType) {
        BulkUpload draft = dao.get(autoUpdate.getBulkUploadId());
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

    public long createBulkUpload(String userId, EntryType entryType) {
        BulkUpload draft = new BulkUpload();
        draft.setName("Untitled");
        Account account = accountController.getByEmail(userId);
        draft.setAccount(account);
        draft.setStatus(BulkUploadStatus.IN_PROGRESS);
        draft.setImportType(entryType.toString());
        draft.setCreationTime(new Date());
        draft.setLastUpdateTime(draft.getCreationTime());
        return dao.create(draft).getId();
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
        Entry entry = entryDAO.get(id);
        data = doUpdate(userId, entry, data);
        if (data == null)
            return null;

        upload.setLastUpdateTime(new Date(data.getModificationTime()));
        dao.update(upload);
        return data;
    }

    protected PartData doUpdate(String userId, Entry entry, PartData data) {
        if (entry == null)
            return null;

        entry = InfoToModelFactory.updateEntryField(data, entry);
        entry.setVisibility(Visibility.DRAFT.getValue());
        entry.setModificationTime(new Date(System.currentTimeMillis()));
        entry = entryDAO.update(entry);

        data.setModificationTime(entry.getModificationTime().getTime());

        // check if there is any linked parts. update if so (expect a max of 1)
        if (data.getLinkedParts() == null || data.getLinkedParts().size() == 0)
            return data;

        // retrieve the entry (this is the only time you can create another entry on update)
        PartData linkedPartData = data.getLinkedParts().get(0); // bulk upload can only link 1
        Entry linkedEntry = entryDAO.get(linkedPartData.getId());
        if (linkedEntry == null) {
            linkedEntry = InfoToModelFactory.infoToEntry(linkedPartData);
            linkedEntry.setVisibility(Visibility.DRAFT.getValue());
            Account account = accountController.getByEmail(userId);
            linkedEntry.setOwner(account.getFullName());
            linkedEntry.setOwnerEmail(account.getEmail());
            linkedEntry = entryDAO.create(linkedEntry);
            entry.getLinkedEntries().add(linkedEntry);
            entryDAO.update(linkedEntry);
        }

        // recursively update
        PartData linked = doUpdate(userId, linkedEntry, linkedPartData);
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

        switch (status) {
            case PENDING_APPROVAL:
            default:
                return controller.submitBulkImportDraft(userId, id);

            // rejected by admin
            case IN_PROGRESS:
                ArrayList<Long> entryList = dao.getEntryIds(id);
                for (Number l : entryList) {
                    Entry entry = entryDAO.get(l.longValue());
                    if (entry == null || entry.getVisibility() != Visibility.PENDING.getValue())
                        continue;

                    entry.setVisibility(Visibility.DRAFT.getValue());
                    entryDAO.update(entry);
                }

                Date updateTime = new Date(System.currentTimeMillis());
                upload.setLastUpdateTime(updateTime);
                upload.setStatus(status);
                return dao.update(upload).toDataTransferObject();

            // approved by an administrator
            case APPROVED:
                Account account = accountController.getByEmail(userId);
                if (new BulkUploadController().approveBulkImport(account, id))
                    return upload.toDataTransferObject();
                return null;

            case BULK_EDIT:
                upload.getContents().clear();
                dao.delete(upload);
                return upload.toDataTransferObject();
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

                entryController.update(userId, otherEntry);
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

        entryController.update(userId, entry);

        // update bulk upload. even if no new entry was created, entries belonging to it was updated
        if (draft != null) {
            draft.setLastUpdateTime(new Date());
            autoUpdate.setLastUpdate(draft.getLastUpdateTime());
            dao.update(draft);
        }
        return autoUpdate;
    }

    public BulkUploadInfo createOrUpdateEntries(String userId, long draftId, List<PartData> data) {
        BulkUpload draft = dao.get(draftId);
        if (draft == null)
            return null;

        // check permissions
        authorization.expectWrite(userId, draft);

        BulkUploadInfo uploadInfo = draft.toDataTransferObject();

        for (PartData datum : data) {
            if (datum == null)
                continue;

            int index = datum.getIndex();

            Entry entry = entryDAO.get(datum.getId());
            if (entry != null)
                datum = doUpdate(userId, entry, datum);
            else
                datum = createEntryForUpload(userId, datum, draft);

            if (datum == null)
                return null;

            datum.setIndex(index);
            uploadInfo.getEntryList().add(datum);
        }
        return uploadInfo;
    }

    public boolean createEntries(String userId, long draftId, List<PartData> data, HashMap<String, InputStream> files) {
        BulkUpload draft = dao.get(draftId);
        if (draft == null)
            return false;

        // check permissions
        authorization.expectWrite(userId, draft);

        for (PartData datum : data) {
            if (datum == null)
                continue;

            Entry entry = InfoToModelFactory.infoToEntry(datum);
            entry.setVisibility(Visibility.DRAFT.getValue());
            Account account = accountController.getByEmail(userId);
            entry.setOwner(account.getFullName());
            entry.setOwnerEmail(account.getEmail());

            // check if there is any linked parts. create if so (expect a max of 1)
            if (datum.getLinkedParts() != null && datum.getLinkedParts().size() > 0) {
                // create linked
                PartData linked = datum.getLinkedParts().get(0);
                Entry linkedEntry = InfoToModelFactory.infoToEntry(linked);
                linkedEntry.setVisibility(Visibility.DRAFT.getValue());
                linkedEntry.setOwner(account.getFullName());
                linkedEntry.setOwnerEmail(account.getEmail());
                linkedEntry = entryDAO.create(linkedEntry);

                linked.setId(linkedEntry.getId());
                linked.setModificationTime(linkedEntry.getModificationTime().getTime());

                // check for attachments and sequences for linked entry
                saveFiles(userId, linked, linkedEntry, files);

                // link to main entry in the database
                entry.getLinkedEntries().add(linkedEntry);
            }

            entry = entryDAO.create(entry);
            draft.getContents().add(entry);

            dao.update(draft);

            // save files
            saveFiles(userId, datum, entry, files);
        }

        return true;
    }

    protected void saveFiles(String userId, PartData data, Entry entry, HashMap<String, InputStream> files) {
        // check sequence
        try {
            String sequenceName = data.getSequenceFileName();
            if (!StringUtils.isBlank(sequenceName)) {
                String sequenceString = IOUtils.toString(files.get(sequenceName));
                DNASequence dnaSequence = SequenceController.parse(sequenceString);

                if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
                    Logger.error("Couldn't parse sequence file " + sequenceName);
                } else {
                    Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
                    sequence.setSequenceUser(sequenceString);
                    sequence.setEntry(entry);
                    sequence.setFileName(sequenceName);
                    Sequence result = DAOFactory.getSequenceDAO().saveSequence(sequence);
                    if (result != null)
                        ApplicationController.scheduleBlastIndexRebuildTask(true);
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        }

        // check attachment
        try {
            if (data.getAttachments() != null && !data.getAttachments().isEmpty()) {
                String attachmentName = data.getAttachments().get(0).getFilename();
                InputStream attachmentStream = files.get(attachmentName);

                // clear
                ArrayList<Attachment> attachments = DAOFactory.getAttachmentDAO().getByEntry(entry);
                if (attachments != null && !attachments.isEmpty()) {
                    for (Attachment attachment : attachments) {
                        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
                        File attachmentDir = Paths.get(dataDir, "attachments").toFile();
                        DAOFactory.getAttachmentDAO().delete(attachmentDir, attachment);
                    }
                }

                Attachment attachment = new Attachment();
                attachment.setEntry(entry);
                attachment.setDescription("");
                String fileId = Utils.generateUUID();
                attachment.setFileId(fileId);
                attachment.setFileName(attachmentName);
                String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
                File attachmentDir = Paths.get(dataDir, "attachments").toFile();
                DAOFactory.getAttachmentDAO().save(attachmentDir, attachment, attachmentStream);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
