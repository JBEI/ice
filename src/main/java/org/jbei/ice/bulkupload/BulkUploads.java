package org.jbei.ice.bulkupload;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.account.Account;
import org.jbei.ice.account.AccountController;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.entry.attachment.Attachments;
import org.jbei.ice.entry.sequence.PartSequence;
import org.jbei.ice.executor.IceExecutorService;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.BulkUploadDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Attachment;
import org.jbei.ice.storage.model.BulkUploadModel;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

/**
 * Controller for dealing with bulk imports (including drafts)
 *
 * @author Hector Plahar
 */
public class BulkUploads {

    private final BulkUploadDAO dao;
    private final EntryDAO entryDAO;
    private final BulkUploadAuthorization authorization;
    private final AccountController accountController;
    private final Attachments attachments;

    public BulkUploads() {
        dao = DAOFactory.getBulkUploadDAO();
        entryDAO = DAOFactory.getEntryDAO();
        authorization = new BulkUploadAuthorization();
        accountController = new AccountController();
        attachments = new Attachments();
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
     * @throws PermissionException user performing action doesn't have privileges
     */
    public BulkUpload rename(String userId, long id, String name) {
        BulkUploadModel upload = dao.get(id);
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
     * Creates a new bulk upload. If upload type is not bulk edit, then the status is set to in progress.
     * Default permissions consisting of read permissions for the public groups that the requesting user is
     * a part of are added
     *
     * @param userId identifier for user making request
     * @param info   bulk upload data
     * @return created upload
     */
    public BulkUpload create(String userId, BulkUpload info) {
        // validate type
        if (EntryType.nameToType(info.getType()) == null)
            throw new IllegalArgumentException("Cannot create upload of type: " + info.getType());

        AccountModel account = accountController.getByEmail(userId);
        BulkUploadModel upload = new BulkUploadModel();
        upload.setName(info.getName());
        upload.setAccount(account);
        upload.setCreationTime(new Date());
        upload.setLastUpdateTime(upload.getCreationTime());
        if (info.getStatus() == BulkUploadStatus.BULK_EDIT) {
            upload.setStatus(BulkUploadStatus.BULK_EDIT);
        } else
            upload.setStatus(BulkUploadStatus.IN_PROGRESS);

        upload.setImportType(info.getType());
        upload = dao.create(upload);

        // list of entries associated with this upload (if available at this time)
        if (info.getEntryList() != null) {
            for (PartData data : info.getEntryList()) {
                Entry entry = entryDAO.get(data.getId());
                if (entry == null)
                    continue;

                upload.getContents().add(entry);
            }
        }

        dao.update(upload);
        return upload.toDataTransferObject();
    }

    public void updateLinkType(String userId, long id, EntryType linkType) {
        BulkUploadModel upload = dao.get(id);
        if (upload == null)
            throw new IllegalArgumentException("Could not retrieve upload with id " + id);

        authorization.expectWrite(userId, upload);
        upload.setLinkType(linkType.getName());
        dao.update(upload);
    }

    /**
     * Retrieves list of bulk imports that are owned by the system. System ownership is assigned to
     * all bulk imports that are submitted by non-admins and indicates that it is pending approval.
     * <p>Administrative privileges are required for making this call
     *
     * @param userId account for user making request; expected to be an administrator
     * @return list of bulk imports pending verification
     */
    public HashMap<String, ArrayList<BulkUpload>> getPendingImports(String userId) {
        // check for admin privileges
        authorization.expectAdmin(userId);

        HashMap<String, ArrayList<BulkUpload>> infoList = new HashMap<>();
        List<BulkUploadModel> results;

        results = dao.retrieveByStatus(BulkUploadStatus.PENDING_APPROVAL);
        if (results == null || results.isEmpty())
            return infoList;

        for (BulkUploadModel draft : results) {
            BulkUpload info = new BulkUpload();
            AccountModel draftAccount = draft.getAccount();
            String userEmail = draftAccount.getEmail();
            Account account = new Account();
            account.setEmail(userEmail);
            account.setFirstName(draftAccount.getFirstName());
            account.setLastName(draftAccount.getLastName());
            info.setAccount(account);

            info.setId(draft.getId());
            info.setLastUpdate(draft.getLastUpdateTime());
            int count = draft.getContents().size();
            info.setCount(count);
            info.setType(draft.getImportType());
            info.setCreated(draft.getCreationTime());
            info.setName(draft.getName());

            // add to list
            ArrayList<BulkUpload> userList = infoList.computeIfAbsent(userEmail, k -> new ArrayList<>());
            userList.add(info);
        }

        return infoList;
    }

    /**
     * Retrieves bulk import and entries associated with it that are referenced by the id in the parameter. Only
     * owners or administrators are allowed to retrieve bulk imports
     *
     * @param userId identifier for account of user requesting
     * @param id     unique identifier for bulk import
     * @param offset offset for upload entries (start)
     * @param limit  maximum number of entries to return with the upload
     * @return data transfer object with the retrieved bulk import data and associated entries
     * @throws PermissionException if user doesn't have read permissions for specified bulk import
     */
    public BulkUpload get(String userId, long id, int offset, int limit) {
        BulkUploadModel draft = dao.get(id);
        if (draft == null)
            return null;

        AccountModel account = accountController.getByEmail(userId);
        authorization.expectRead(account.getEmail(), draft);

        // retrieve the entries associated with the bulk import
        BulkUpload info = draft.toDataTransferObject();

        List<Entry> list = dao.retrieveDraftEntries(id, offset, limit);
        for (Entry entry : list) {
            PartData partData = setFileData(userId, entry, ModelToInfoFactory.getInfo(entry));

            // get custom data
            CustomFields fields = new CustomFields();
            partData.getCustomEntryFields().addAll(fields.getCustomFieldValuesForPart(entry.getId()));

            // check if any links and convert
            if (!entry.getLinkedEntries().isEmpty()) {
                Entry linked = (Entry) entry.getLinkedEntries().toArray()[0];
                PartData linkedData = setFileData(userId, linked, ModelToInfoFactory.getInfo(linked));
                CustomFields linkedFields = new CustomFields();
                linkedData.getCustomEntryFields().addAll(linkedFields.getCustomFieldValuesForPart(linked.getId()));
                partData.getLinkedParts().set(0, linkedData);
            }

            info.getEntryList().add(partData);
        }

        info.setCount(dao.retrieveSavedDraftCount(id));
        return info;
    }

    private PartData setFileData(String userId, Entry entry, PartData partData) {
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();

        if (sequenceDAO.hasSequence(entry.getId())) {
            partData.setHasSequence(true);
            String name = sequenceDAO.getSequenceFilename(entry);
            partData.setSequenceFileName(name);
        }

        // check attachment
        if (DAOFactory.getAttachmentDAO().hasAttachment(entry)) {
            partData.setHasAttachment(true);
            partData.setAttachments(attachments.getByEntry(userId, entry.getId()));
        }

        // todo: trace sequences

        return partData;
    }

    /**
     * Retrieves list of user saved bulk imports. Only the owner or an administrator can retrieve it
     *
     * @param requesterId   account of requesting user
     * @param userAccountId account identifier for user whose saved drafts are being requested
     * @return list of draft infos representing saved drafts.
     */
    public ArrayList<BulkUpload> retrieveByUser(String requesterId, String userAccountId) {
        AccountModel userAccount = accountController.getByEmail(userAccountId);
        List<BulkUploadModel> results = dao.retrieveByAccount(userAccount);
        ArrayList<BulkUpload> infoArrayList = new ArrayList<>();

        for (BulkUploadModel draft : results) {
            boolean isOwner = userAccountId.equals(requesterId);
            boolean isAdmin = accountController.isAdministrator(requesterId);
            if (!isOwner && !isAdmin)
                continue;

            BulkUpload draftInfo = draft.toDataTransferObject();
            draftInfo.setCount(dao.retrieveSavedDraftCount(draft.getId()));
            infoArrayList.add(draftInfo);
        }

        return infoArrayList;
    }

    public ArrayList<BulkUpload> getPendingUploads(String userId) {
        if (!accountController.isAdministrator(userId))
            return null;

        List<BulkUploadModel> results = dao.retrieveByStatus(BulkUploadStatus.PENDING_APPROVAL);
        ArrayList<BulkUpload> infoArrayList = new ArrayList<>();

        for (BulkUploadModel draft : results) {
            BulkUpload info = draft.toDataTransferObject();
            info.setCount(dao.retrieveSavedDraftCount(draft.getId()));
            infoArrayList.add(info);
        }

        return infoArrayList;
    }

    /**
     * Deletes a bulk import draft referenced by a unique identifier. only owners of the bulk import
     * or administrators are permitted to delete bulk imports
     *
     * @param userId  account of user making the request
     * @param draftId unique identifier for bulk import
     * @return deleted bulk import
     * @throws PermissionException if lacking permissions
     */
    public BulkUpload deleteDraftById(String userId, long draftId) throws PermissionException {
        BulkUploadModel draft = dao.get(draftId);
        if (draft == null)
            return null;

        AccountModel draftAccount = draft.getAccount();
        if (!userId.equals(draftAccount.getEmail()) && !accountController.isAdministrator(userId))
            throw new PermissionException("No permissions to delete draft " + draftId);

        BulkUploadDeleteTask task = new BulkUploadDeleteTask(userId, draftId);
        IceExecutorService.getInstance().runTask(task);

        BulkUpload draftInfo = draft.toDataTransferObject();
        Account account = draft.getAccount().toDataTransferObject();
        draftInfo.setAccount(account);
        return draftInfo;
    }

    BulkUploadAutoUpdate autoUpdateBulkUpload(String userId, BulkUploadAutoUpdate autoUpdate) {
        BulkUploadEntries creator = new BulkUploadEntries(userId, autoUpdate.getBulkUploadId());
        return creator.createOrUpdateEntry(autoUpdate);
    }

    boolean revertSubmitted(AccountModel account, long uploadId) {
        boolean isAdmin = accountController.isAdministrator(account.getEmail());
        if (!isAdmin) {
            Logger.warn(account.getEmail() + " attempting to revert submitted bulk upload "
                + uploadId + " without admin privs");
            return false;
        }

        BulkUploadModel upload = dao.get(uploadId);
        if (upload == null) {
            Logger.warn("Could not retrieve bulk upload " + uploadId + " for reversal");
            return false;
        }

        if (upload.getStatus() != BulkUploadStatus.PENDING_APPROVAL)
            return false;

        upload.setStatus(BulkUploadStatus.IN_PROGRESS);
        String newName = StringUtils.isEmpty(upload.getName()) ? "Returned upload" : upload.getName() + "(Returned)";
        upload.setName(newName);
        upload.setLastUpdateTime(new Date());
        dao.update(upload);
        return true;
    }

    boolean approveBulkImport(String userId, long id) {
        // only admins allowed
        if (!accountController.isAdministrator(userId)) {
            Logger.warn("Only administrators can approve bulk imports");
            return false;
        }

        // retrieve bulk upload in question (at this point it is owned by system)
        BulkUploadModel bulkUploadModel = dao.get(id);
        if (bulkUploadModel == null) {
            Logger.error("Could not retrieve bulk upload with id \"" + id + "\" for approval");
            return false;
        }

        // when done approving, delete the bulk upload record but not the entries associated with it.
        bulkUploadModel.getContents().clear();
        dao.delete(bulkUploadModel);
        return true;
    }

    public SequenceInfo addSequence(String userId, long bulkUploadId, long entryId, String sequenceString,
                                    String fileName) {
        BulkUploadModel upload = dao.get(bulkUploadId);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);

        PartSequence partSequence = new PartSequence(userId, Long.toString(entryId));
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(sequenceString.getBytes(StandardCharsets.UTF_8));
            return partSequence.parseSequenceFile(inputStream, fileName, false);
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
    }

    public AttachmentInfo addAttachment(String userId, long bulkUploadId, long entryId, InputStream fileInputStream,
                                        String fileName) {
        BulkUploadModel upload = dao.get(bulkUploadId);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);

        String fileId = Utils.generateUUID();
        File attachmentFile = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
            Attachments.attachmentDirName, fileId).toFile();

        try {
            FileUtils.copyInputStreamToFile(fileInputStream, attachmentFile);
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        AttachmentInfo info = new AttachmentInfo();
        info.setFileId(fileId);
        info.setFilename(fileName);

        return attachments.addAttachmentToEntry(userId, entryId, info);
    }

    public boolean deleteAttachment(String userId, long bulkUploadId, long entryId) {
        BulkUploadModel upload = dao.get(bulkUploadId);
        if (upload == null)
            return false;

        authorization.expectWrite(userId, upload);
        Entry entry = entryDAO.get(entryId);
        if (entry == null)
            return false;

        List<Attachment> attachments = DAOFactory.getAttachmentDAO().getByEntry(entry);

        // actually expect only 1
        try {
            for (Attachment attachment : attachments) {
                DAOFactory.getAttachmentDAO().delete(attachment);
            }
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }

        return true;
    }

    public boolean deleteEntry(String userId, long uploadId, long entryId) {
        try {
            BulkUploadModel upload = dao.get(uploadId);
            Entry entry = new EntryDAO().get(entryId);
            authorization.expectWrite(userId, upload);
            upload.getContents().remove(entry);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    /**
     * Retrieves part numbers that match the token passed in the parameter, that are compatible with the type
     * in the parameter. Two entry types are compatible if they can be associated with specific entries (as descendants)
     * in a hierarchical relationship
     *
     * @param type  type of entry the part numbers must be compatible with
     * @param token part number token to match
     * @param limit maximum number of matches to return
     * @return list of part numbers that can be linked to the type of entry
     */
    public ArrayList<String> getMatchingPartNumbersForLinks(EntryType type, String token, int limit) {
        ArrayList<String> dataList = new ArrayList<>();
        if (token == null)
            return dataList;

        Set<String> compatibleTypes = new HashSet<>();
        compatibleTypes.add(type.getName());
        compatibleTypes.add(EntryType.PART.getName());
        if (type == EntryType.STRAIN)
            compatibleTypes.add(EntryType.PLASMID.getName());

        token = token.replaceAll("'", "");
        return new ArrayList<>(DAOFactory.getEntryDAO().getMatchingEntryPartNumbers(token, limit, compatibleTypes));
    }
}
