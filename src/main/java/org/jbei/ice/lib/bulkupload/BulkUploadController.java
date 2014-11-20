package org.jbei.ice.lib.bulkupload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.BulkUploadDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.servlet.ModelToInfoFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

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

    public BulkUploadInfo create(String userId, BulkUploadInfo info) {
        Account account = accountController.getByEmail(userId);
        BulkUpload upload = new BulkUpload();
        upload.setName(StringUtils.isEmpty(info.getName()) ? "untitled" : info.getName());
        upload.setAccount(account);
        upload.setCreationTime(new Date(System.currentTimeMillis()));
        upload.setLastUpdateTime(upload.getCreationTime());
        upload.setStatus(BulkUploadStatus.IN_PROGRESS);
        upload.setImportType(info.getType());
        return dao.create(upload).toDataTransferObject();
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
     * @return data transfer object with the retrieved bulk import data and associated entries
     * @throws ControllerException
     * @throws PermissionException
     */
    public BulkUploadInfo retrieveById(String userId, long id, int start, int limit)
            throws ControllerException, PermissionException {
        BulkUpload draft = dao.get(id);
        if (draft == null)
            return null;

        Account account = accountController.getByEmail(userId);
        authorization.expectRead(userId, draft);

        // convert bulk import db object to data transfer object
        int size = 0;
        try {
            size = dao.retrieveSavedDraftCount(id);
        } catch (DAOException e) {
            Logger.error(e);
        }
        BulkUploadInfo draftInfo = draft.toDataTransferObject();
        draftInfo.setCount(size);
        EntryType type = EntryType.nameToType(draft.getImportType().split("\\s+")[0]);

        // retrieve the entries associated with the bulk import
        List<Entry> contents = dao.retrieveDraftEntries(id, start, limit);

        // convert
        draftInfo.getEntryList().addAll(convertParts(account, contents));
        return draftInfo;
    }

    public BulkUploadInfo getBulkImport(String userId, long id, int offset, int limit) {
        BulkUpload draft = dao.get(id);
        if (draft == null)
            return null;

        Account account = accountController.getByEmail(userId);
        authorization.expectRead(account.getEmail(), draft);
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();

        // retrieve the entries associated with the bulk import
        BulkUploadInfo info = draft.toDataTransferObject();

        List<Entry> list = dao.retrieveDraftEntries(id, offset, limit);
        for (Entry entry : list) {
            PartData partData = ModelToInfoFactory.getInfo(entry);
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

            // trace sequences
            info.getEntryList().add(partData);
        }

        info.setCount(dao.retrieveSavedDraftCount(id));
        return info;
    }

    protected ArrayList<PartData> convertParts(Account account, List<Entry> contents)
            throws ControllerException {
        ArrayList<PartData> addList = new ArrayList<>();
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();

        for (Entry entry : contents) {
            ArrayList<Attachment> attachments = attachmentController.getByEntry(account.getEmail(), entry);
            boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
            boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
            PartData info = ModelToInfoFactory.getInfo(entry);
            ArrayList<AttachmentInfo> attachmentInfos = ModelToInfoFactory.getAttachments(attachments);
            info.setAttachments(attachmentInfos);
            info.setHasAttachment(!attachmentInfos.isEmpty());
            info.setHasSequence(hasSequence);
            info.setHasOriginalSequence(hasOriginalSequence);

            // retrieve permission
            Set<Permission> entryPermissions = entry.getPermissions();
            if (entryPermissions != null && !entryPermissions.isEmpty()) {
                for (Permission permission : entryPermissions) {
                    info.getAccessPermissions().add(permission.toDataTransferObject());
                }
            }

            addList.add(info);
        }

        return addList;
    }

    /**
     * Retrieves list of parts that are intended to be edited in bulk. User must
     * have write permissions on all parts
     *
     * @param account user account making request. Should have write permissions on all accounts
     * @param partIds unique part identifiers
     * @return list of retrieved part data wrapped in the bulk upload data transfer object
     * @throws ControllerException
     */
    public BulkUploadInfo getPartsForBulkEdit(Account account, ArrayList<Long> partIds) throws ControllerException {
        ArrayList<Entry> parts = entryController.getEntriesByIdSet(account, partIds);
        BulkUploadInfo bulkUploadInfo = new BulkUploadInfo();
        bulkUploadInfo.getEntryList().addAll(convertParts(account, parts));
        return bulkUploadInfo;
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
     * @param requesting account of user making the request
     * @param draftId    unique identifier for bulk import
     * @return deleted bulk import
     * @throws ControllerException
     * @throws PermissionException
     */
    public BulkUploadInfo deleteDraftById(String userId, long draftId) throws PermissionException {
        BulkUpload draft = dao.get(draftId);
        if (draft == null)
            return null;

        Account draftAccount = draft.getAccount();
        if (!userId.equals(draftAccount.getEmail()) && !accountController.isAdministrator(userId))
            throw new PermissionException("No permissions to delete draft " + draftId);

        // delete all associated entries. for strain with plasmids both are returned
        // todo : use task to speed up process and also check for status

        ArrayList<Long> entryIds = dao.getEntryIds(draftId);
        for (long entryId : entryIds) {
            try {
                entryController.delete(userId, entryId);
            } catch (PermissionException pe) {
                Logger.warn("Could not delete entry " + entryId + " for bulk upload " + draftId);
            }
        }

        dao.delete(draft);

        BulkUploadInfo draftInfo = draft.toDataTransferObject();
        AccountTransfer accountTransfer = draft.getAccount().toDataTransferObject();
        draftInfo.setAccount(accountTransfer);
        return draftInfo;
    }

    public BulkUploadAutoUpdate autoUpdateBulkUpload(String userId, BulkUploadAutoUpdate autoUpdate,
            EntryType addType) throws ControllerException {
        BulkEntryCreator creator = new BulkEntryCreator();
        return creator.createOrUpdateEntry(userId, autoUpdate, addType);
    }

    /**
     * Submits a bulk import that has been saved. This action is restricted to the owner of the
     * draft or to administrators.
     *
     * @param userId  Account identifier of user performing save
     * @param draftId unique identifier for saved bulk import
     * @return true, if draft was sa
     */
    public BulkUploadInfo submitBulkImportDraft(String userId, long draftId) throws PermissionException {
        // retrieve draft
        BulkUpload draft = dao.get(draftId);
        if (draft == null)
            return null;

        // check permissions
        authorization.expectWrite(userId, draft);

        if (!BulkUploadUtil.validate(draft)) {
            Logger.warn("Attempting to submit a bulk upload draft (" + draftId + ") which does not validate");
            return null;
        }

        draft.setStatus(BulkUploadStatus.PENDING_APPROVAL);
        draft.setLastUpdateTime(new Date());
        draft.setName(userId);

        BulkUpload bulkUpload = dao.update(draft);
        if (bulkUpload != null) {
            // convert entries to pending
            ArrayList<Long> list = dao.getEntryIds(draftId);
            for (Number l : list) {
                Entry entry = entryDAO.get(l.longValue());
                if (entry == null)
                    continue;

                entry.setVisibility(Visibility.PENDING.getValue());
                entryDAO.update(entry);

                // if linked entries
                for (Entry linked : entry.getLinkedEntries()) {
                    linked.setVisibility(Visibility.PENDING.getValue());
                    entryDAO.update(linked);
                }
            }

            String email = Utils.getConfigValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (email != null && !email.isEmpty()) {
                String subject = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME) + " Bulk Upload Notification";
                String body = "A bulk upload has been submitted and is pending verification.\n\n";
                body += "Please login to the registry at:\n\n";
                body += Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
                body += "\n\nand use the \"Pending Approval\" menu item to approve it\n\nThanks.";
                Emailer.send(email, subject, body);
            }
            return bulkUpload.toDataTransferObject();
        }
        return null;
    }

    public boolean revertSubmitted(Account account, long uploadId) throws ControllerException {
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

        String previousOwner = upload.getName();
        Account prevOwnerAccount = accountController.getByEmail(previousOwner);
        if (prevOwnerAccount == null)
            return false;

        upload.setStatus(BulkUploadStatus.IN_PROGRESS);
        upload.setName("Returned Upload");
        upload.setLastUpdateTime(new Date());
        dao.update(upload);

        return true;
    }

    public boolean approveBulkImport(Account account, long id) {
        // only admins allowed
        if (!accountController.isAdministrator(account.getEmail())) {
            Logger.warn("Only administrators can approve bulk imports");
            return false;
        }

        // retrieve bulk upload in question (at this point it is owned by system)
        BulkUpload bulkUpload = dao.get(id);
        if (bulkUpload == null) {
            Logger.error("Could not retrieve bulk upload with id \"" + id + "\" for approval");
            return false;
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

            PermissionsController permissionsController = new PermissionsController();
            // set permissions
            for (Permission permission : bulkUpload.getPermissions()) {
                // add permission for entry
                AccessPermission access = new AccessPermission();
                access.setType(AccessPermission.Type.READ_ENTRY);
                access.setTypeId(entry.getId());
                access.setArticleId(permission.getGroup().getId());
                access.setArticle(AccessPermission.Article.GROUP);
                permissionsController.addPermission(account.getEmail(), access);
                if (plasmid != null) {
                    access.setTypeId(plasmid.getId());
                    permissionsController.addPermission(account.getEmail(), access);
                }
            }

            entryController.update(account, entry);
            if (plasmid != null)
                entryController.update(account, plasmid);
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
        Entry entry = dao.getUploadEntry(bulkUploadId, entryId);
        // todo : enforcing that entry must exist
        if (entry == null) {
            Logger.error("Could not find entry with id " + entryId + " in bulk upload " + bulkUploadId);
            return null;
        }

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
        Entry entry = dao.getUploadEntry(bulkUploadId, entryId);
        // todo : enforcing that entry must exist
        if (entry == null) {
            Logger.error("Could not find entry with id " + entryId + " in bulk upload " + bulkUploadId);
            return null;
        }

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
        Entry entry = dao.getUploadEntry(bulkUploadId, entryId);
        // todo : enforcing that entry must exist
        if (entry == null) {
            Logger.error("Could not find entry with id " + entryId + " in bulk upload " + bulkUploadId);
            return false;
        }

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
}
