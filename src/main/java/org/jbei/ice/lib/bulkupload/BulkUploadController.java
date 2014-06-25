package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;
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
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.servlet.ModelToInfoFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Controller for dealing with bulk imports (including drafts)
 *
 * @author Hector Plahar
 */
public class BulkUploadController {

    private final BulkUploadDAO dao;
    private final BulkUploadAuthorization authorization;
    private final AccountController accountController;
    private final EntryController entryController;
    private final AttachmentController attachmentController;

    public BulkUploadController() {
        dao = DAOFactory.getBulkUploadDAO();
        authorization = new BulkUploadAuthorization();
        accountController = new AccountController();
        entryController = new EntryController();
        attachmentController = new AttachmentController();
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
     * @param account account for user making call. expected to be an administrator
     * @return list of bulk imports pending verification
     * @throws ControllerException
     */
    public ArrayList<BulkUploadInfo> retrievePendingImports(Account account) throws ControllerException {
        // check for admin privileges
        authorization.expectAdmin(account.getEmail());

        ArrayList<BulkUploadInfo> infoList = new ArrayList<>();
        ArrayList<BulkUpload> results;

        try {
            results = dao.retrieveByStatus(BulkUploadStatus.PENDING_APPROVAL);
            if (results == null || results.isEmpty())
                return infoList;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        for (BulkUpload draft : results) {
            BulkUploadInfo info = new BulkUploadInfo();
            Account draftAccount = draft.getAccount();
            AccountTransfer accountTransfer = new AccountTransfer();
            accountTransfer.setEmail(draftAccount.getEmail());
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

            infoList.add(info);
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
        BulkUpload draft;

        try {
            draft = dao.retrieveById(id);
            if (draft == null)
                return null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        Account account = accountController.getByEmail(userId);
        authorization.expectRead(account.getEmail(), draft);

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
        ArrayList<Entry> contents = dao.retrieveDraftEntries(type, id, start, limit);

        // convert
        draftInfo.getEntryList().addAll(convertParts(account, contents));
        return draftInfo;
    }

    public BulkUploadInfo getBulkImport(String userId, long id, int offset, int limit) {
        BulkUpload draft = dao.retrieveById(id);
        if (draft == null)
            return null;

        Account account = accountController.getByEmail(userId);
        authorization.expectRead(account.getEmail(), draft);

        // retrieve the entries associated with the bulk import
        BulkUploadInfo info = draft.toDataTransferObject();

        ArrayList<Entry> list = dao.retrieveDraftEntries(EntryType.PLASMID, id, offset, limit);
        for (Entry entry : list) {
            info.getEntryList().add(ModelToInfoFactory.getInfo(entry));
        }

        info.setCount(dao.retrieveSavedDraftCount(id));
        return info;
    }

    protected ArrayList<PartData> convertParts(Account account, ArrayList<Entry> contents)
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
     * Retrieves list of user saved bulk imports
     *
     * @param account     account of requesting user
     * @param userAccount account whose saved drafts are being requested
     * @return list of draft infos representing saved drafts.
     * @throws ControllerException
     */
    public ArrayList<BulkUploadInfo> retrieveByUser(Account account, Account userAccount)
            throws ControllerException {

        ArrayList<BulkUpload> results;

        try {
            results = dao.retrieveByAccount(userAccount);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        ArrayList<BulkUploadInfo> infoArrayList = new ArrayList<>();

        for (BulkUpload draft : results) {
            Account draftAccount = draft.getAccount();

            boolean isOwner = account.equals(draftAccount);
            boolean isAdmin = accountController.isAdministrator(account.getEmail());
            if (!isOwner && !isAdmin)
                continue;

            BulkUploadInfo draftInfo = new BulkUploadInfo();
            draftInfo.setCreated(draft.getCreationTime());
            draftInfo.setLastUpdate(draft.getLastUpdateTime());
            draftInfo.setId(draft.getId());

            draftInfo.setName(draft.getName());
            draftInfo.setType(draft.getImportType());
            draftInfo.setCount(dao.retrieveSavedDraftCount(draft.getId()));

            // set the account info
            AccountTransfer accountTransfer = new AccountTransfer();
            accountTransfer.setEmail(draftAccount.getEmail());
            accountTransfer.setFirstName(draftAccount.getFirstName());
            accountTransfer.setLastName(draftAccount.getLastName());
            draftInfo.setAccount(accountTransfer);
            infoArrayList.add(draftInfo);
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
    public BulkUploadInfo deleteDraftById(Account requesting, long draftId)
            throws ControllerException, PermissionException {
        BulkUpload draft = dao.retrieveById(draftId);
        if (draft == null)
            throw new ControllerException("Could not retrieve draft with id \"" + draftId + "\"");

        Account draftAccount = draft.getAccount();
        if (!requesting.equals(draftAccount) && !accountController.isAdministrator(requesting.getEmail()))
            throw new PermissionException("No permissions to delete draft " + draftId);

        // delete all associated entries. for strain with plasmids both are returned
        // todo : use task to speed up process and also check for status
//        for (Entry entry : draft.getContents()) {
//            try {
//                entryController.delete(requesting, entry.getId());
//            } catch (PermissionException pe) {
//                Logger.warn("Could not delete entry " + entry.getRecordId() + " for bulk upload " + draftId);
//            }
//        }

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
     * @param account Account of user performing save
     * @param draftId unique identifier for saved bulk import
     * @return true, if draft was sa
     */
    public boolean submitBulkImportDraft(Account account, long draftId)
            throws ControllerException, PermissionException {
        // retrieve draft
        BulkUpload draft = dao.retrieveById(draftId);
        if (draft == null)
            return false;

        // check permissions
        authorization.expectWrite(account.getEmail(), draft);

        if (!BulkUploadUtil.validate(draft)) {
            Logger.warn("Attempting to submit a bulk upload draft (" + draftId + ") which does not validate");
            return false;
        }

        draft.setStatus(BulkUploadStatus.PENDING_APPROVAL);
        draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
        draft.setName(account.getEmail());

        boolean success = dao.update(draft) != null;
        if (success) {
            // convert entries to pending
            for (Entry entry : draft.getContents()) {
                entry.setVisibility(Visibility.PENDING.getValue());
                entryController.update(account, entry);

                // if linked entries
                for (Entry linked : entry.getLinkedEntries()) {
                    linked.setVisibility(Visibility.PENDING.getValue());
                    entryController.update(account, linked);
                }
            }

            String email = Utils.getConfigValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (email != null && !email.isEmpty()) {
                String subject = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME) + " Bulk Upload Notification";
                String body = "A bulk upload has been submitted and is pending verification.\n\n";
                body += "Please go to the following link to verify.\n\n";
                body += Utils.getConfigValue(ConfigurationKey.URI_PREFIX) + "/#page=bulk";
                Emailer.send(email, subject, body);
            }
        }
        return success;
    }

    public boolean revertSubmitted(Account account, long uploadId) throws ControllerException {
        boolean isAdmin = accountController.isAdministrator(account.getEmail());
        if (!isAdmin) {
            Logger.warn(account.getEmail() + " attempting to revert submitted bulk upload "
                                + uploadId + " without admin privs");
            return false;
        }

        try {
            BulkUpload upload = dao.retrieveById(uploadId);
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
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return true;
    }

    public boolean approveBulkImport(Account account, long id) throws ControllerException, PermissionException {
        // only admins allowed
        if (!accountController.isAdministrator(account.getEmail())) {
            throw new PermissionException("Only administrators can approve bulk imports");
        }

        // retrieve bulk upload in question (at this point it is owned by system)
        BulkUpload bulkUpload;

        try {
            bulkUpload = dao.retrieveById(id);
            if (bulkUpload == null)
                throw new ControllerException("Could not retrieve bulk upload with id \"" + id + "\" for approval");
        } catch (DAOException e) {
            throw new ControllerException(e);
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
        try {
            bulkUpload.getContents().clear();
            dao.delete(bulkUpload);
            return true;
        } catch (DAOException e) {
            throw new ControllerException("Could not delete bulk upload " + bulkUpload.getId()
                                                  + ". Contents were approved so please delete manually.", e);
        }
    }
}
