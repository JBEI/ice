package org.jbei.ice.lib.bulkupload;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.bulkupload.EditMode;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.email.EmailFactory;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryLinks;
import org.jbei.ice.lib.entry.LinkType;
import org.jbei.ice.lib.entry.sample.SampleService;
import org.jbei.ice.lib.entry.sequence.PartSequence;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.InfoToModelFactory;
import org.jbei.ice.storage.hibernate.dao.BulkUploadDAO;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldDAO;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldValueDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Creates entries for bulk uploads
 *
 * @author Hector Plahar
 */
public class BulkUploadEntries {

    private final BulkUploadDAO dao;
    private final EntryDAO entryDAO;
    private final EntryCreator creator;
    private final AccountController accountController;
    private final BulkUploadAuthorization authorization;
    private final BulkUploads uploads;
    private final String userId;
    private final BulkUpload upload;

    BulkUploadEntries(String userId, EntryType addType) {
        dao = DAOFactory.getBulkUploadDAO();
        entryDAO = DAOFactory.getEntryDAO();
        creator = new EntryCreator();
        accountController = new AccountController();
        authorization = new BulkUploadAuthorization();
        uploads = new BulkUploads();
        this.userId = userId;
        this.upload = createBulkUpload(addType);
    }

    public BulkUploadEntries(String userId, long uploadId) {
        dao = DAOFactory.getBulkUploadDAO();
        entryDAO = DAOFactory.getEntryDAO();
        creator = new EntryCreator();
        accountController = new AccountController();
        authorization = new BulkUploadAuthorization();
        uploads = new BulkUploads();
        this.userId = userId;
        this.upload = dao.get(uploadId);
        if (this.upload == null)
            throw new IllegalArgumentException("Invalid upload id \"" + uploadId + "\"");
    }

    long getUploadId() {
        return this.upload.getId();
    }

    /**
     * Creates a new bulk upload record in the database
     *
     * @param entryType type of bulk upload
     * @return unique identifier for bulk upload created
     */
    private BulkUpload createBulkUpload(EntryType entryType) {
        BulkUploadInfo info = new BulkUploadInfo();
        info.setStatus(BulkUploadStatus.IN_PROGRESS);
        info.setType(entryType.getName());
        long uploadId = uploads.create(userId, info).getId();
        return dao.get(uploadId);
    }

    /**
     * Using data in the parameter, creates an entry for the specified upload
     *
     * @param data information (including field values) for creating entry object
     * @return c
     */
    public PartData createEntry(PartData data) {
        authorization.expectWrite(userId, upload);
        return createEntryForUpload(data);
    }

    private PartData createEntryForUpload(PartData partData) {
        partData.setVisibility(Visibility.DRAFT);

        if (StringUtils.isEmpty(partData.getOwnerEmail())) {
            Account account = accountController.getByEmail(userId);
            partData.setOwner(account.getFullName());
            partData.setOwnerEmail(account.getEmail());
        }
        EntryCreator creator = new EntryCreator();
        partData = creator.createPart(this.userId, partData);
        Entry entry = entryDAO.get(partData.getId());

        upload.getContents().add(entry);
        dao.update(upload);

        partData.setId(entry.getId());
        partData.setModificationTime(entry.getModificationTime().getTime());
        return partData;
    }

    public PartData updateEntry(long entryId, PartData data) {
        authorization.expectWrite(userId, upload);

        Entry entry = entryDAO.get(entryId);
        if (upload.getStatus() != BulkUploadStatus.BULK_EDIT)
            entry.setVisibility(Visibility.DRAFT.getValue());
        data = doUpdate(userId, entry, data);
        if (data == null)
            return null;

        upload.setLastUpdateTime(new Date(data.getModificationTime()));
        dao.update(upload);
        return data;
    }

    private PartData doUpdate(String userId, Entry entry, PartData data) {
        if (entry == null)
            return null;

        entry = InfoToModelFactory.updateEntryField(data, entry);
        if (entry == null)
            return null;

        entry.setModificationTime(new Date());
        entry = entryDAO.update(entry);
        data.setModificationTime(entry.getModificationTime().getTime());

        // custom fields
        setCustomFieldValuesForPart(entry, data);

        // check if there is any linked parts. update if so (expect a max of 1)
        if (data.getLinkedParts() == null || data.getLinkedParts().size() == 0)
            return data;

        // retrieve the entry (this is the only time you can create another entry on update)
        PartData linkedPartData = data.getLinkedParts().get(0); // bulk upload can only link 1
        Entry linkedEntry = entryDAO.get(linkedPartData.getId());
        if (linkedEntry == null && !StringUtils.isEmpty(linkedPartData.getPartId())) {
            // try partId
            linkedEntry = entryDAO.getByPartNumber(linkedPartData.getPartId());
            if (linkedEntry != null)
                linkedPartData.setId(linkedEntry.getId());
        }

        if (linkedEntry == null) {
            linkedEntry = InfoToModelFactory.infoToEntry(linkedPartData);
            if (linkedEntry != null) {
                linkedEntry.setVisibility(Visibility.DRAFT.getValue());
                Account account = accountController.getByEmail(userId);
                linkedEntry.setOwner(account.getFullName());
                linkedEntry.setOwnerEmail(account.getEmail());
                linkedEntry = entryDAO.create(linkedEntry);
                entry.getLinkedEntries().add(linkedEntry);
                entryDAO.update(linkedEntry);
            }
        } else {
            // linking to existing
            EntryLinks entryLinks = new EntryLinks(userId, Long.toString(entry.getId()));
            entryLinks.addLink(linkedPartData, LinkType.CHILD);
        }

        // recursively update
        PartData linked = doUpdate(userId, linkedEntry, linkedPartData);
        data.getLinkedParts().clear();
        if (linked != null)
            data.getLinkedParts().add(linked);

        return data;
    }

    private void setCustomFieldValuesForPart(Entry entry, PartData data) {
        if (data == null || data.getCustomEntryFields() == null || entry == null)
            return;

        CustomEntryFieldDAO dao = DAOFactory.getCustomEntryFieldDAO();

        for (CustomEntryField customEntryField : data.getCustomEntryFields()) {
            if (customEntryField.getFieldType() == FieldType.EXISTING)
                continue;

            CustomEntryFieldModel customEntryFieldModel = dao.get(customEntryField.getId());
            if (customEntryFieldModel == null) {
                // get details about custom field (note: this is different from value)
                if (customEntryField.getEntryType() == null) {
                    customEntryField.setEntryType(EntryType.nameToType(entry.getRecordType()));
                }

                // try again with label and type
                Optional<CustomEntryFieldModel> optional = dao.getLabelForType(customEntryField.getEntryType(), customEntryField.getLabel());
                if (!optional.isPresent()) {
                    Logger.error("Could not retrieve custom field with id " + customEntryField.getId());
                    continue;
                }
                customEntryFieldModel = optional.get();
            }

            CustomEntryFieldValueDAO customValueDAO = DAOFactory.getCustomEntryFieldValueDAO();

            // values for custom field currently stored in the database.
            CustomEntryFieldValueModel model = customValueDAO.getByFieldAndEntry(entry, customEntryFieldModel);
            if (model == null) {
                model = new CustomEntryFieldValueModel();
                model.setEntry(entry);
                model.setField(customEntryFieldModel);
                model.setValue(customEntryField.getValue());
                customValueDAO.create(model);
            } else {
                model.setValue(customEntryField.getValue());
                customValueDAO.update(model);
            }
        }
    }

    public ProcessedBulkUpload updateStatus(String userId, long id, BulkUploadStatus status) {
        if (status == null)
            return null;

        // upload is allowed to be null
        BulkUpload upload = dao.get(id);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);
        ProcessedBulkUpload processedBulkUpload = new ProcessedBulkUpload();
        processedBulkUpload.setUploadId(id);

        switch (status) {
            case PENDING_APPROVAL:
            default:
                return submitBulkImportDraft(userId, upload, processedBulkUpload);

            // rejected by admin
            case IN_PROGRESS:
                List<Long> entryList = dao.getEntryIds(upload);
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
                return processedBulkUpload;

            // approved by an administrator
            case APPROVED:
                if (new BulkUploads().approveBulkImport(userId, id))
                    return processedBulkUpload;
                return null;

            case BULK_EDIT:
                upload.getContents().clear();
                dao.delete(upload);
                return processedBulkUpload;
        }
    }

    /**
     * Submits a bulk import that has been saved. This action is restricted to the owner of the
     * draft or to administrators.
     */
    private ProcessedBulkUpload submitBulkImportDraft(String userId, BulkUpload draft,
                                                      ProcessedBulkUpload processedBulkUpload) throws PermissionException {
        // validate entries
        BulkUploadValidation validation = new BulkUploadValidation(draft);
        if (!validation.isValid()) {
            processedBulkUpload.setSuccess(false);
            for (EntryField entryField : validation.getFailedFields()) {
                processedBulkUpload.getHeaders().add(new EntryHeaderValue(entryField));
            }
            processedBulkUpload.setUserMessage("Cannot submit your bulk upload due to a validation failure");
            return processedBulkUpload;
        }

        draft.setStatus(BulkUploadStatus.PENDING_APPROVAL);
        draft.setLastUpdateTime(new Date());
        draft.setName(userId);

        BulkUpload bulkUpload = dao.update(draft);
        if (bulkUpload != null) {
            // convert entries to pending
            dao.setEntryStatus(bulkUpload, Visibility.PENDING);

            String email = Utils.getConfigValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (email != null && !email.isEmpty()) {
                String subject = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME) + " Bulk Upload Notification";
                String body = "A bulk upload has been submitted and is pending verification.\n\n";
                body += "Please login to the registry at:\n\n";
                body += Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
                body += "\n\nand use the \"Pending Approval\" menu item to approve it\n\nThanks.";
                EmailFactory.getEmail().send(email, subject, body);
            }
            return processedBulkUpload;
        }
        return null;
    }


    /**
     * Renames the bulk upload referenced by the id in the parameter
     *
     * @param userId unique identifier of user performing action. Must with be an administrator
     *               own the bulk upload
     * @param id     unique identifier referencing the bulk upload
     * @param name   name to assign to the bulk upload
     * @return data transfer object for the bulk upload.
     * returns null if no
     * @throws org.jbei.ice.lib.access.AuthorizationException is user performing action doesn't have privileges
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
     * @param autoUpdate wrapper for information used to create entry
     * @return updated wrapper for information used to create entry. Will contain additional information
     * such as the unique identifier for the part, if one was created
     */
    BulkUploadAutoUpdate createOrUpdateEntry(BulkUploadAutoUpdate autoUpdate) {
        Account account = accountController.getByEmail(userId);

        // todo : split out update from bulk edit
        // for bulk edit, drafts will not exist
        if (autoUpdate.getEditMode() != EditMode.BULK_EDIT) {
            autoUpdate.setBulkUploadId(this.upload.getId());
        }

        // for strain with plasmid this is the strain
        Entry entry = entryDAO.get(autoUpdate.getEntryId());
        Entry otherEntry = null;  // for strain with plasmid this is the entry

        // if entry is null, create entry
        if (entry == null) {
            PartData partData = new PartData(autoUpdate.getType());
            partData.setOwner(account.getFullName());
            partData.setOwnerEmail(account.getEmail());
            partData.setCreator(account.getFullName());
            partData.setCreatorEmail(account.getEmail());
            partData = creator.createPart(userId, partData);
            entry = entryDAO.get(partData.getId());

            autoUpdate.setEntryId(entry.getId());
            if (this.upload != null) {
                this.upload.getContents().add(entry);
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

        if (this.upload != null && this.upload.getStatus() != BulkUploadStatus.PENDING_APPROVAL) {
            if (otherEntry != null && autoUpdate.getEditMode() != EditMode.BULK_EDIT) {
                if (otherEntry.getVisibility() == null || otherEntry.getVisibility() != Visibility.DRAFT.getValue())
                    otherEntry.setVisibility(Visibility.DRAFT.getValue());

                updateEntry(otherEntry);
            }

            if ((entry.getVisibility() == null || entry.getVisibility() != Visibility.DRAFT.getValue())
                    && autoUpdate.getEditMode() != EditMode.BULK_EDIT)
                entry.setVisibility(Visibility.DRAFT.getValue());
        }

        // todo : que?
//        EntryEditor editor = new EntryEditor();
//        // set the plasmids and update
//        if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.toString())
//                && entry.getLinkedEntries().isEmpty()) {
//            Strain strain = (Strain) entry;
//            editor.setStrainPlasmids(account, strain, strain.getPlasmids());
//        }

        updateEntry(entry);

        // update bulk upload. even if no new entry was created, entries belonging to it was updated
        if (this.upload != null) {
            this.upload.setLastUpdateTime(new Date());
            autoUpdate.setLastUpdate(this.upload.getLastUpdateTime());
            dao.update(this.upload);
        }
        return autoUpdate;
    }

    private void updateEntry(Entry entry) {
        if (entry == null) {
            return;
        }

        entry.setModificationTime(Calendar.getInstance().getTime());
        if (entry.getVisibility() == null)
            entry.setVisibility(Visibility.OK.getValue());
        entryDAO.update(entry);
    }

    public BulkUploadInfo createOrUpdateEntries(List<PartData> data) {
        // check permissions
        authorization.expectWrite(userId, upload);

        BulkUploadInfo uploadInfo = upload.toDataTransferObject();

        for (PartData datum : data) {
            if (datum == null)
                continue;

            int index = datum.getIndex();

            Entry entry = entryDAO.get(datum.getId());
            if (entry != null) {
                if (upload.getStatus() != BulkUploadStatus.BULK_EDIT)
                    entry.setVisibility(Visibility.DRAFT.getValue());
                datum = doUpdate(userId, entry, datum);
            } else
                datum = createEntryForUpload(datum);

            if (datum == null)
                return null;

            datum.setIndex(index);
            uploadInfo.getEntryList().add(datum);
        }
        return uploadInfo;
    }

    boolean createEntries(List<PartWithSample> data, HashMap<String, InputStream> files) {
        // check permissions
        authorization.expectWrite(userId, upload);
        SampleService sampleService = new SampleService();

        if (data == null)
            return false;

        for (PartWithSample partWithSample : data) {
            if (partWithSample == null)
                continue;

            PartData partData = partWithSample.getPartData();
            if (partData == null)
                continue;

            partData = createEntryForUpload(partData);
            Entry entry = entryDAO.get(partData.getId());

            // save files
            saveFiles(partData, entry, files);

            // save sample, if available
            PartSample partSample = partWithSample.getPartSample();
            if (partSample == null)
                continue;

            sampleService.createSample(userId, Long.toString(partData.getId()), partSample, null);
        }

        return true;
    }

    private void saveFiles(PartData data, Entry entry, HashMap<String, InputStream> files) {
        // check sequence
        try {
            String sequenceName = data.getSequenceFileName();
            if (!StringUtils.isBlank(sequenceName)) {
                PartSequence partSequence = new PartSequence(entry.getOwnerEmail(), entry.getRecordId());
                partSequence.parseSequenceFile(files.get(sequenceName), sequenceName, false);
            }
        } catch (IOException e) {
            Logger.error(e);
        }

        // check attachment
        try {
            if (data.getAttachments() != null && !data.getAttachments().isEmpty()) {
                String attachmentName = data.getAttachments().get(0).getFilename();
                if (StringUtils.isEmpty(attachmentName))
                    return;

                InputStream attachmentStream = files.get(attachmentName);
                if (attachmentStream == null)
                    return;

                // clear
                List<Attachment> attachments = DAOFactory.getAttachmentDAO().getByEntry(entry);
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
                Path path = Paths.get(dataDir, "attachments", attachment.getFileId());
                Files.write(path, IOUtils.toByteArray(attachmentStream));
                DAOFactory.getAttachmentDAO().create(attachment);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
