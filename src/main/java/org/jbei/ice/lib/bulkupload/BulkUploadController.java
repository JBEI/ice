package org.jbei.ice.lib.bulkupload;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.BulkUploadDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

/**
 * Controller for dealing with bulk imports (including drafts)
 *
 * @author Hector Plahar
 */
public class BulkUploadController {

    private final BulkUploadDAO dao;
    private final EntryDAO entryDAO;
    private final BulkUploadAuthorization authorization;
    private final AccountController accountController;
    private final EntryController entryController;
    private final AttachmentController attachmentController;
    private final SequenceController sequenceController;

    public BulkUploadController() {
        dao = DAOFactory.getBulkUploadDAO();
        entryDAO = DAOFactory.getEntryDAO();
        authorization = new BulkUploadAuthorization();
        accountController = new AccountController();
        entryController = new EntryController();
        attachmentController = new AttachmentController();
        sequenceController = new SequenceController();
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
    public BulkUploadInfo create(String userId, BulkUploadInfo info) {
        Account account = accountController.getByEmail(userId);
        BulkUpload upload = new BulkUpload();
        upload.setName(info.getName());
        upload.setAccount(account);
        upload.setCreationTime(new Date());
        upload.setLastUpdateTime(upload.getCreationTime());
        if (info.getStatus() == BulkUploadStatus.BULK_EDIT) {
            // only one instance of bulk edit is allowed to remain
            clearBulkEdits(userId);
            upload.setStatus(BulkUploadStatus.BULK_EDIT);
        } else
            upload.setStatus(BulkUploadStatus.IN_PROGRESS);

        upload.setImportType(info.getType());

        // set default permissions
        GroupController groupController = new GroupController();
        ArrayList<Group> publicGroups = groupController.getAllPublicGroupsForAccount(account);
        for (Group group : publicGroups) {
            Permission permission = new Permission();
            permission.setCanRead(true);
            permission.setUpload(upload);
            permission.setGroup(group);
            permission = DAOFactory.getPermissionDAO().create(permission);
            upload.getPermissions().add(permission);
        }

        upload = dao.create(upload);

        if (info.getEntryList() != null) {
            for (PartData data : info.getEntryList()) {
                Entry entry = entryDAO.get(data.getId());
                // todo if entry is in another bulk upload, then update will fail
                if (entry == null)
                    continue;

                upload.getContents().add(entry);
            }
        }

        dao.update(upload);
        return upload.toDataTransferObject();
    }

    /**
     * Removes any bulk edits belonging to the specified user
     *
     * @param userId unique identifier for user whose bulk edits are to be removed
     */
    protected void clearBulkEdits(String userId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account == null)
            return;

        List<BulkUpload> userEdits = dao.retrieveByAccount(account);
        if (userEdits == null || userEdits.isEmpty())
            return;

        for (BulkUpload upload : userEdits) {
            if (upload.getStatus() != BulkUploadStatus.BULK_EDIT)
                continue;
            upload.getContents().clear();
            dao.delete(upload);
        }
    }

    /**
     * Retrieves list of bulk imports that are owned by the system. System ownership is assigned to
     * all bulk imports that are submitted by non-admins and indicates that it is pending approval.
     * <p>Administrative privileges are required for making this call
     *
     * @param userId account for user making request; expected to be an administrator
     * @return list of bulk imports pending verification
     */
    public HashMap<String, ArrayList<BulkUploadInfo>> getPendingImports(String userId) {
        // check for admin privileges
        authorization.expectAdmin(userId);

        HashMap<String, ArrayList<BulkUploadInfo>> infoList = new HashMap<>();
        ArrayList<BulkUpload> results;

        results = dao.retrieveByStatus(BulkUploadStatus.PENDING_APPROVAL);
        if (results == null || results.isEmpty())
            return infoList;

        for (BulkUpload draft : results) {
            BulkUploadInfo info = new BulkUploadInfo();
            Account draftAccount = draft.getAccount();
            String userEmail = draftAccount.getEmail();
            AccountTransfer accountTransfer = new AccountTransfer();
            accountTransfer.setEmail(userEmail);
            accountTransfer.setFirstName(draftAccount.getFirstName());
            accountTransfer.setLastName(draftAccount.getLastName());
            info.setAccount(accountTransfer);

            info.setId(draft.getId());
            info.setLastUpdate(draft.getLastUpdateTime());
            int count = draft.getContents().size();
            info.setCount(count);
            info.setType(draft.getImportType());
            info.setCreated(draft.getCreationTime());
            info.setName(draft.getName());

            // add to list
            ArrayList<BulkUploadInfo> userList = infoList.get(userEmail);
            if (userList == null) {
                userList = new ArrayList<>();
                infoList.put(userEmail, userList);
            }
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
     * @throws PermissionException
     */
    public BulkUploadInfo getBulkImport(String userId, long id, int offset, int limit) {
        BulkUpload draft = dao.get(id);
        if (draft == null)
            return null;

        Account account = accountController.getByEmail(userId);
        authorization.expectRead(account.getEmail(), draft);

        // retrieve the entries associated with the bulk import
        BulkUploadInfo info = draft.toDataTransferObject();

        List<Entry> list = dao.retrieveDraftEntries(id, offset, limit);
        for (Entry entry : list) {
            PartData partData = setFileData(userId, entry, ModelToInfoFactory.getInfo(entry));

            // check if any links and convert
            if (!entry.getLinkedEntries().isEmpty()) {
                Entry linked = (Entry) entry.getLinkedEntries().toArray()[0];
                PartData linkedData = partData.getLinkedParts().remove(0);
                linkedData = setFileData(userId, linked, linkedData);
                partData.getLinkedParts().add(linkedData);
            }

            info.getEntryList().add(partData);
        }

        info.setCount(dao.retrieveSavedDraftCount(id));
        return info;
    }

    protected PartData setFileData(String userId, Entry entry, PartData partData) {
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();

        if (sequenceDAO.hasSequence(entry.getId())) {
            partData.setHasSequence(true);
            String name = sequenceDAO.getSequenceFilename(entry);
            partData.setSequenceFileName(name);
        }

        // check attachment
        if (attachmentController.hasAttachment(entry)) {
            partData.setHasAttachment(true);
            partData.setAttachments(attachmentController.getByEntry(userId, entry.getId()));
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
    public ArrayList<BulkUploadInfo> retrieveByUser(String requesterId, String userAccountId) {
        Account userAccount = accountController.getByEmail(userAccountId);
        ArrayList<BulkUpload> results = dao.retrieveByAccount(userAccount);
        ArrayList<BulkUploadInfo> infoArrayList = new ArrayList<>();

        for (BulkUpload draft : results) {
            boolean isOwner = userAccountId.equals(requesterId);
            boolean isAdmin = accountController.isAdministrator(requesterId);
            if (!isOwner && !isAdmin)
                continue;

            BulkUploadInfo draftInfo = draft.toDataTransferObject();
            draftInfo.setCount(dao.retrieveSavedDraftCount(draft.getId()));
            infoArrayList.add(draftInfo);
        }

        return infoArrayList;
    }

    public ArrayList<BulkUploadInfo> getPendingUploads(String userId) {
        if (!accountController.isAdministrator(userId))
            return null;

        ArrayList<BulkUpload> results = dao.retrieveByStatus(BulkUploadStatus.PENDING_APPROVAL);
        ArrayList<BulkUploadInfo> infoArrayList = new ArrayList<>();

        for (BulkUpload draft : results) {
            BulkUploadInfo info = draft.toDataTransferObject();
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
     * @throws PermissionException
     */
    public BulkUploadInfo deleteDraftById(String userId, long draftId) throws PermissionException {
        BulkUpload draft = dao.get(draftId);
        if (draft == null)
            return null;

        Account draftAccount = draft.getAccount();
        if (!userId.equals(draftAccount.getEmail()) && !accountController.isAdministrator(userId))
            throw new PermissionException("No permissions to delete draft " + draftId);

        BulkUploadDeleteTask task = new BulkUploadDeleteTask(userId, draftId);
        IceExecutorService.getInstance().runTask(task);

        BulkUploadInfo draftInfo = draft.toDataTransferObject();
        AccountTransfer accountTransfer = draft.getAccount().toDataTransferObject();
        draftInfo.setAccount(accountTransfer);
        return draftInfo;
    }

    public BulkUploadAutoUpdate autoUpdateBulkUpload(String userId, BulkUploadAutoUpdate autoUpdate,
                                                     EntryType addType) {
        BulkEntryCreator creator = new BulkEntryCreator();
        return creator.createOrUpdateEntry(userId, autoUpdate, addType);
    }


    public boolean revertSubmitted(Account account, long uploadId) {
        boolean isAdmin = accountController.isAdministrator(account.getEmail());
        if (!isAdmin) {
            Logger.warn(account.getEmail() + " attempting to revert submitted bulk upload "
                    + uploadId + " without admin privs");
            return false;
        }

        BulkUpload upload = dao.get(uploadId);
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

    public boolean approveBulkImport(String userId, long id) {
        // only admins allowed
        if (!accountController.isAdministrator(userId)) {
            Logger.warn("Only administrators can approve bulk imports");
            return false;
        }

        // retrieve bulk upload in question (at this point it is owned by system)
        BulkUpload bulkUpload = dao.get(id);
        if (bulkUpload == null) {
            Logger.error("Could not retrieve bulk upload with id \"" + id + "\" for approval");
            return false;
        }

        // get permissions for bulk upload and set it to the individual entries
        PermissionsController permissionsController = new PermissionsController();
        ArrayList<AccessPermission> permissions = new ArrayList<>();
        for (Permission permission : bulkUpload.getPermissions()) {
            AccessPermission accessPermission = permission.toDataTransferObject();
            // read or write access
            if (accessPermission.getType() == AccessPermission.Type.READ_UPLOAD)
                accessPermission.setType(AccessPermission.Type.READ_ENTRY);
            else
                accessPermission.setType(AccessPermission.Type.WRITE_ENTRY);
            permissions.add(accessPermission);
        }

        // go through passed contents
        // TODO : this needs to go into a task that auto updates
        for (Entry entry : bulkUpload.getContents()) {
            entry.setVisibility(Visibility.OK.getValue());
            Set<Entry> linked = entry.getLinkedEntries();
            Entry plasmid = null;
            if (linked != null && !linked.isEmpty()) {
                plasmid = (Entry) linked.toArray()[0];
                plasmid.setVisibility(Visibility.OK.getValue());
            }

            // set permissions
            for (AccessPermission accessPermission : permissions) {
                accessPermission.setTypeId(entry.getId());

                permissionsController.addPermission(userId, accessPermission);
                if (plasmid != null) {
                    accessPermission.setTypeId(plasmid.getId());
                    permissionsController.addPermission(userId, accessPermission);
                }
            }

            entryController.update(userId, entry);
            if (plasmid != null)
                entryController.update(userId, plasmid);
        }

        // when done approving, delete the bulk upload record but not the entries associated with it.
        bulkUpload.getContents().clear();
        dao.delete(bulkUpload);
        return true;
    }

    public SequenceInfo addSequence(String userId, long bulkUploadId, long entryId, String sequenceString,
                                    String fileName) {
        BulkUpload upload = dao.get(bulkUploadId);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);
        Entry entry = entryDAO.get(entryId);

        // parse actual sequence
        DNASequence dnaSequence = SequenceController.parse(sequenceString);
        if (dnaSequence == null)
            return null;

        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(entry);
        if (fileName != null)
            sequence.setFileName(fileName);
        SequenceInfo info = sequenceController.save(userId, sequence).toDataTransferObject();
        info.setSequence(dnaSequence);
        return info;
    }

    public AttachmentInfo addAttachment(String userId, long bulkUploadId, long entryId, InputStream fileInputStream,
                                        String fileName) {
        BulkUpload upload = dao.get(bulkUploadId);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);

        String fileId = Utils.generateUUID();
        File attachmentFile = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                AttachmentController.attachmentDirName, fileId).toFile();

        try {
            FileUtils.copyInputStreamToFile(fileInputStream, attachmentFile);
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        AttachmentInfo info = new AttachmentInfo();
        info.setFileId(fileId);
        info.setFilename(fileName);

        return attachmentController.addAttachmentToEntry(userId, entryId, info);
    }

    public boolean deleteAttachment(String userId, long bulkUploadId, long entryId) {
        BulkUpload upload = dao.get(bulkUploadId);
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
            BulkUpload upload = dao.get(uploadId);
            Entry entry = new EntryDAO().get(entryId);
            authorization.expectWrite(userId, upload);
            upload.getContents().remove(entry);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    public List<AccessPermission> getUploadPermissions(String userId, long uploadId) {
        List<AccessPermission> permissions = new ArrayList<>();
        BulkUpload upload = dao.get(uploadId);
        if (upload == null)
            return permissions;

        authorization.expectWrite(userId, upload);

        if (upload.getPermissions() != null) {
            for (Permission permission : upload.getPermissions())
                permissions.add(permission.toDataTransferObject());
        }

        return permissions;
    }

    /**
     * Adds specified access permission to the bulk upload.
     *
     * @param userId   unique identifier of user making the request. Must be an admin or owner of the upload
     * @param uploadId unique identifier for bulk upload
     * @param access   details about the permission to the added
     * @return added permission with identifier that can be used to remove/delete the permission
     * @throws java.lang.IllegalArgumentException if the upload cannot be located using its identifier
     */
    public AccessPermission addPermission(String userId, long uploadId, AccessPermission access) {
        BulkUpload upload = dao.get(uploadId);
        if (upload == null)
            throw new IllegalArgumentException("Could not locate bulk upload with id " + uploadId);

        access.setTypeId(uploadId);
        Permission permission = new PermissionsController().addPermission(userId, access);
        upload.getPermissions().add(permission);
        dao.update(upload);
        return permission.toDataTransferObject();
    }

    /**
     * Removes specified permission from bulk upload
     *
     * @param userId       unique identifier of user making the request. Must be an admin or owner of the bulk upload
     * @param uploadId     unique identifier for bulk upload
     * @param permissionId unique identifier for permission that has been previously added to upload
     * @return true if deletion is successful
     * @throws java.lang.IllegalArgumentException if upload or permission cannot be located by their identifiers
     */
    public boolean deletePermission(String userId, long uploadId, long permissionId) {
        BulkUpload upload = dao.get(uploadId);
        if (upload == null)
            throw new IllegalArgumentException("Could not locate bulk upload with id " + uploadId);

        authorization.expectWrite(userId, upload);
        Permission toDelete = null;

        if (upload.getPermissions() != null) {
            for (Permission permission : upload.getPermissions()) {
                if (permission.getId() == permissionId) {
                    toDelete = permission;
                    break;
                }
            }
        }

        if (toDelete == null)
            throw new IllegalArgumentException("Could not locate permission for deletion");

        upload.getPermissions().remove(toDelete);
        DAOFactory.getPermissionDAO().delete(toDelete);
        return dao.update(upload) != null;
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
