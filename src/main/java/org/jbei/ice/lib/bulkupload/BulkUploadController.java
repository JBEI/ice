package org.jbei.ice.lib.bulkupload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.server.EntryToInfoFactory;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.Visibility;

import org.apache.commons.io.FileUtils;

/**
 * Controller for dealing with bulk imports (including drafts)
 *
 * @author Hector Plahar
 */
public class BulkUploadController {

    private BulkUploadDAO dao;
    private AccountController accountController;
    private EntryController entryController;
    private AttachmentController attachmentController;
    private SequenceController sequenceController;
    private GroupController groupController;

    /**
     * Initialises dao and controller dependencies. These need be injected
     */
    public BulkUploadController() {
        dao = new BulkUploadDAO();
        accountController = new AccountController();
        entryController = new EntryController();
        attachmentController = new AttachmentController();
        sequenceController = new SequenceController();
        groupController = new GroupController();
    }

    /**
     * Retrieves list of bulk imports that are owned by the system. System ownership is assigned to
     * all bulk imports that are submitted by non-admins and indicates that it is pending approval.
     * <p>Administrative privileges are required for making this call
     *
     * @param account account for user making call. expected to be an administrator
     * @return list of bulk imports pending verification
     * @throws ControllerException
     * @throws PermissionException
     */
    public ArrayList<BulkUploadInfo> retrievePendingImports(Account account)
            throws ControllerException, PermissionException {

        // check for admin privileges
        if (!accountController.isAdministrator(account))
            throw new PermissionException("Administrative privileges are required!");

        ArrayList<BulkUploadInfo> infoList = new ArrayList<BulkUploadInfo>();
        ArrayList<BulkUpload> results;

        try {
            results = dao.retrieveByAccount(accountController.getSystemAccount());
            if (results == null)
                return infoList;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        for (BulkUpload draft : results) {

            BulkUploadInfo info = new BulkUploadInfo();
            Account draftAccount = draft.getAccount();
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setEmail(draftAccount.getEmail());
            accountInfo.setFirstName(draftAccount.getFirstName());
            accountInfo.setLastName(draftAccount.getLastName());
            info.setAccount(accountInfo);

            info.setId(draft.getId());
            info.setLastUpdate(draft.getLastUpdateTime());
            int count = -1;
            try {
                count = dao.retrieveSavedDraftCount(draft.getId());
            } catch (DAOException e) {
                Logger.error(e); // we care about the data more than the count
            }

            info.setCount(count);
            info.setType(EntryAddType.stringToType(draft.getImportType()));
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
     * @param account account for user requesting
     * @param id      unique identifier for bulk import
     * @return data transfer object with the retrieved bulk import data and associated entries
     * @throws ControllerException
     * @throws PermissionException
     */
    public BulkUploadInfo retrieveById(Account account, long id) throws ControllerException,
            PermissionException {

        BulkUpload draft;

        try {
            draft = dao.retrieveByIdWithContents(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (draft == null)
            throw new ControllerException("Could not retrieve bulk import draft for id " + id);

        boolean isModerator = accountController.isAdministrator(account);
        boolean isOwner = account.equals(draft.getAccount());

        // check for permissions to retrieve this bulk import
        if (!isModerator && !isOwner) {
            throw new PermissionException("Insufficient privileges by " + account.getEmail()
                                                  + " to view bulk import for " + draft.getAccount().getEmail());
        }

        EntryAddType type = EntryAddType.stringToType(draft.getImportType());

        // convert bulk import db object to data transfer object
        BulkUploadInfo draftInfo = new BulkUploadInfo();
        draftInfo.setCount(draft.getContents().size());
        draftInfo.setCreated(draft.getCreationTime());
        draftInfo.setLastUpdate(draft.getLastUpdateTime());
        draftInfo.setId(draft.getId());
        draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));
        draftInfo.setName(draft.getName());

        // convert account db object to info object
        AccountInfo accountInfo = new AccountInfo();
        Account draftAccount = draft.getAccount();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());

        // set draft account
        draftInfo.setAccount(accountInfo);

        // retrieve the entries associated with the bulk import
        for (Entry entry : draft.getContents()) {
            ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
            boolean hasSequence = sequenceController.getByEntry(entry) != null;

            // convert to info object (no samples or trace sequences since bulk import does not have the ui for it yet)
            EntryInfo info = EntryToInfoFactory.getInfo(account, entry, attachments, null, null, hasSequence);
            if (type == EntryAddType.STRAIN_WITH_PLASMID) {

                // get plasmids
                String plasmids = ((Strain) entry).getPlasmids();
                Entry plasmid = BulkUploadUtil.getPartNumberForStrainPlasmid(account, entryController, plasmids);
                if (plasmid != null) {
                    attachments = attachmentController.getByEntry(account, plasmid);
                    hasSequence = sequenceController.getByEntry(entry) != null;
                    EntryInfo plasmidInfo = EntryToInfoFactory.getInfo(account, plasmid,
                                                                       attachments, null, null, hasSequence);
                    info.setInfo(plasmidInfo);
                }
            }

            if (info != null)
                draftInfo.getEntryList().add(info);
        }

        return draftInfo;
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

        ArrayList<BulkUploadInfo> infoArrayList = new ArrayList<BulkUploadInfo>();

        for (BulkUpload draft : results) {
            Account draftAccount = draft.getAccount();

            boolean isOwner = account.equals(draftAccount);
            boolean isAdmin = accountController.isAdministrator(account);
            if (!isOwner && !isAdmin)
                continue;

            BulkUploadInfo draftInfo = new BulkUploadInfo();
            draftInfo.setCreated(draft.getCreationTime());
            draftInfo.setLastUpdate(draft.getLastUpdateTime());
            draftInfo.setId(draft.getId());

            draftInfo.setName(draft.getName());
            draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));

            try {
                int count = dao.retrieveSavedDraftCount(draft.getId());
                draftInfo.setCount(count);
            } catch (DAOException e) {
                draftInfo.setCount(-1);
                Logger.error(e);
            }

            // set the account info
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setEmail(draftAccount.getEmail());
            accountInfo.setFirstName(draftAccount.getFirstName());
            accountInfo.setLastName(draftAccount.getLastName());
            draftInfo.setAccount(accountInfo);
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
        BulkUpload draft;
        try {
            draft = dao.retrieveByIdWithContents(draftId);
            if (draft == null)
                throw new ControllerException("Could not retrieve draft with id \"" + draftId + "\"");

            Account draftAccount = draft.getAccount();
            if (!requesting.equals(draftAccount) && !accountController.isAdministrator(requesting))
                throw new PermissionException("No permissions to delete draft " + draftId);

//            for (Entry entry : draft.getContents()) {
//                if (entry.getVisibility() == Visibility.DRAFT.getValue()) {
//                    entryController.delete(requesting, entry);
//                }
//            }

            draft.getContents().clear();
            dao.update(draft);
            dao.delete(draft);

        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        BulkUploadInfo draftInfo = new BulkUploadInfo();
        try {
            int count = dao.retrieveSavedDraftCount(draft.getId());
            draftInfo.setCount(count);
        } catch (DAOException e) {
            draftInfo.setCount(-1);
            Logger.error(e);
        }
        draftInfo.setCreated(draft.getCreationTime());
        draftInfo.setId(draft.getId());
        Account draftAccount = draft.getAccount();
        draftInfo.setName(draftAccount.getFullName());
        draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));

        // set the account info
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        draftInfo.setAccount(accountInfo);
        return draftInfo;
    }

    /**
     * Create a bulk import draft
     *
     * @param draftOwner account for owner of the draft. This is also the person creating the event
     * @param type       type of bulk upload which represents the contents
     * @param name       user entered name for the bulk upload
     * @param entryList  contents for the bulk upload
     * @return created BulkUpload object converted to data transfer equivalent
     * @throws ControllerException
     */
    public BulkUploadInfo createBulkImportDraft(Account draftOwner, EntryAddType type, String name,
            ArrayList<EntryInfo> entryList, String uuid) throws ControllerException {

        BulkUpload draft = new BulkUpload();
        draft.setName(name);
        draft.setAccount(draftOwner);
        draft.setImportType(type.toString());

        ArrayList<EntryInfo> contentInfoList = new ArrayList<EntryInfo>();
        ArrayList<Entry> newContents = new ArrayList<Entry>();
        Group group = groupController.getGroupByUUID(uuid);

        // convert info contents to Entry
        for (EntryInfo info : entryList) {
            info.setVisibility(Visibility.DRAFT);
            processEntryInfo(draftOwner, type, info, newContents, contentInfoList, group);
        }

        // update draft data
        draft.setCreationTime(new Date(System.currentTimeMillis()));
        draft.setLastUpdateTime(draft.getCreationTime());
        draft.setContents(newContents);
        draft.setReadGroup(group);

        try {
            draft = dao.save(draft);
            BulkUploadInfo savedUploadInfo = BulkUploadUtil.modelToInfo(draft);
            savedUploadInfo.setEntryList(contentInfoList);
            return savedUploadInfo;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    // TODO : really need transaction rollback here
    public void processEntryInfo(Account draftOwner, EntryAddType type, EntryInfo info, ArrayList<Entry> contents,
            ArrayList<EntryInfo> contentInfoList, Group group) throws ControllerException {

        Entry entry;
        Entry enclosedEntry = null;

        try {
            boolean exists = updateIfExists(draftOwner, contents, contentInfoList, info);

            if (!exists) {
                // entry does not exist so create new one
                entry = InfoToModelFactory.infoToEntry(info);
                entry = entryController.createEntry(draftOwner, entry, false, group);
                contents.add(entry);
                saveSequence(draftOwner, info.getSequenceAnalysis(), entry);
                saveAttachments(draftOwner, info.getAttachments(), entry);

                if (type == EntryAddType.STRAIN_WITH_PLASMID) {
                    info.getInfo().setVisibility(info.getVisibility());
                    enclosedEntry = InfoToModelFactory.infoToEntry(info.getInfo());
                    enclosedEntry = entryController.createEntry(draftOwner, enclosedEntry, false, group);

                    String name = enclosedEntry.getOneName() == null ? "" : enclosedEntry.getOneName().getName();

                    String plasmidPartNumberString = "[["
                            + JbeirSettings.getSetting("WIKILINK_PREFIX") + ":"
                            + enclosedEntry.getOnePartNumber().getPartNumber() + "|"
                            + name + "]]";
                    ((Strain) entry).setPlasmids(plasmidPartNumberString);

                    try {
                        // update plasmids for strain
                        // TODO : update groups
                        entryController.update(draftOwner, entry);
                    } catch (PermissionException e) {
                        Logger.error(e);
                    }

                    saveSequence(draftOwner, info.getInfo().getSequenceAnalysis(), enclosedEntry);
                    saveAttachments(draftOwner, info.getInfo().getAttachments(), enclosedEntry);
                }

                EntryInfo convertedInfo = BulkUploadUtil.toEntryInfo(attachmentController,
                                                                     sequenceController,
                                                                     draftOwner,
                                                                     entry,
                                                                     enclosedEntry);
                contentInfoList.add(convertedInfo);
            }
        } catch (PermissionException pe) {
            Logger.error(pe);
        }
    }

    /**
     * @param account   account of user performing action
     * @param draftId   unique identifier for bulk upload representing draft
     * @param entryList entry info data containing the latest data
     * @return updated bulk info object
     * @throws ControllerException
     * @throws PermissionException
     */
    public BulkUploadInfo updateBulkImportDraft(Account account, long draftId, ArrayList<EntryInfo> entryList,
            String groupUUID) throws ControllerException, PermissionException {

        BulkUpload draft;
        try {
            draft = dao.retrieveByIdWithContents(draftId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        boolean isAdmin = accountController.isAdministrator(account);

        // check permissions
        if (!draft.getAccount().equals(account) && !isAdmin)
            throw new PermissionException("User " + account.getEmail()
                                                  + " does not have permission to update draft " + draftId);

        ArrayList<EntryInfo> contentInfoList = new ArrayList<EntryInfo>();
        ArrayList<Entry> contentList = new ArrayList<Entry>();

        EntryAddType type = EntryAddType.stringToType(draft.getImportType());
        if (type == null)
            throw new ControllerException("Could not determine type for draft " + draftId);

        Group group = groupController.getGroupByUUID(groupUUID);

        // convert info contents to Entry
        for (EntryInfo info : entryList) {
            info.setVisibility(Visibility.DRAFT);
            processEntryInfo(account, type, info, contentList, contentInfoList, group);
        }
        draft.setReadGroup(group);

        // update the draft
        try {
            draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
            draft.setContents(contentList);
            BulkUpload updatedUpload = dao.update(draft);
            BulkUploadInfo savedUploadInfo = BulkUploadUtil.modelToInfo(updatedUpload);
            savedUploadInfo.setEntryList(contentInfoList);
            return savedUploadInfo;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    protected void saveAttachments(Account account,
            ArrayList<AttachmentInfo> attachmentInfoArrayList, Entry entry)
            throws ControllerException {

        if (attachmentInfoArrayList == null)
            return;

        // retrieve attachments that are associated with entry and clear
        ArrayList<Attachment> list = attachmentController.getAttachments(entry);
        HashSet<String> existingIds = new HashSet<String>();

        if (list != null) {
            for (Attachment attachment : list) {
                existingIds.add(attachment.getFileId());
            }
        }

        // cycle through list of new attachments
        for (AttachmentInfo attachmentInfo : attachmentInfoArrayList) {
            if (existingIds.remove(attachmentInfo.getFileId()))
                continue;

            // create and save attachment
            Attachment attachment = new Attachment();
            attachment.setEntry(entry);
            attachment.setDescription(""); // no way of entering description in bulk import
            attachment.setFileName(attachmentInfo.getFilename());
            attachment.setFileId(attachmentInfo.getFileId());
            attachmentController.saveExistingFile(account, attachment);
        }

        // delete whatever files are remaining
        for (String fileId : existingIds) {
            try {
                attachmentController.delete(account, fileId);
            } catch (PermissionException e) {
                Logger.error(e);
            }
        }
    }

    /**
     * Checks the list of existing draft entries, and updates the entries
     *
     * @param account user account
     * @param info    current info being updated
     * @return true if entry exists and is being updated, false otherwise
     * @throws ControllerException
     * @throws PermissionException
     */
    private boolean updateIfExists(Account account, ArrayList<Entry> contents, ArrayList<EntryInfo> infoList,
            EntryInfo info) throws ControllerException, PermissionException {

        Entry entry = entryController.get(account, info.getId());
        if (entry == null) // no existing
            return false;

        Entry enclosedEntry = null;

        // check enclosing info
        if (info.getInfo() != null && info.getType() == EntryType.STRAIN) {
            enclosedEntry = entryController.get(account, info.getInfo().getId());

            // and if it exists
            if (enclosedEntry != null) {

                // both exist
                info.getInfo().setVisibility(info.getVisibility());
                InfoToModelFactory.infoToEntry(info.getInfo(), enclosedEntry);

                // update enclosing and get the part number
                enclosedEntry = entryController.update(account, enclosedEntry);
                String name = enclosedEntry.getOneName() == null ? "" : enclosedEntry.getOneName().getName();
                String plasmidPartNumberString = "[[" + JbeirSettings.getSetting("WIKILINK_PREFIX")
                        + ":" + enclosedEntry.getOnePartNumber().getPartNumber() + "|"
                        + name + "]]";
                ((Strain) entry).setPlasmids(plasmidPartNumberString);

                try {
                    // update plasmids for strain
                    entryController.update(account, entry);
                } catch (PermissionException e) {
                    Logger.error(e);
                }

                // update sequence and attachment
                saveSequence(account, info.getInfo().getSequenceAnalysis(), enclosedEntry);
                saveAttachments(account, info.getInfo().getAttachments(), enclosedEntry);
            }
        }

        // perform update for main entry
        entry = InfoToModelFactory.infoToEntry(info, entry);
        entryController.update(account, entry);

        // update main entry sequence and attachment
        saveSequence(account, info.getSequenceAnalysis(), entry);
        saveAttachments(account, info.getAttachments(), entry);

        EntryInfo convertedInfo = BulkUploadUtil.toEntryInfo(attachmentController,
                                                             sequenceController,
                                                             account,
                                                             entry,
                                                             enclosedEntry);
        infoList.add(convertedInfo);
        contents.add(entry);
        return true;
    }

    /**
     * Submits a bulk import that has been saved. This action is restricted to the owner of the
     * draft
     * or to administrators.
     *
     * @param account Account of user performing save
     * @param draftId unique identifier for saved bulk import
     * @return true, if draft was sa
     */
    public boolean submitBulkImportDraft(Account account, long draftId, ArrayList<EntryInfo> entryList,
            String groupUUID) throws ControllerException, PermissionException {

        // retrieve draft
        BulkUpload draft;
        try {
            draft = dao.retrieveByIdWithContents(draftId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (draft == null)
            throw new ControllerException("Could not retrieve draft by id " + draftId);

        // check permissions
        if (!draft.getAccount().equals(account) && !accountController.isAdministrator(account))
            throw new PermissionException("User " + account.getEmail()
                                                  + " does not have permission to update draft " + draftId);

        // determine visibility based on account performing action
        boolean isAdmin = accountController.isAdministrator(account);
        Visibility visibility;
        if (!isAdmin)
            visibility = Visibility.PENDING;
        else
            visibility = Visibility.OK;

        ArrayList<EntryInfo> infoList = new ArrayList<EntryInfo>();
        ArrayList<Entry> newContents = new ArrayList<Entry>();
        EntryAddType type = EntryAddType.stringToType(draft.getImportType());

        Group group = groupController.getGroupByUUID(groupUUID);

        // go through passed contents
        for (EntryInfo info : entryList) {
            info.setVisibility(visibility);
            processEntryInfo(account, type, info, newContents, infoList, group);
        }

        // if an admin is submitting this then
        if (isAdmin) {
            // delete the draft since it has been recorded and return
            try {
                draft.setContents(null);
                dao.delete(draft);
            } catch (DAOException e) {
                throw new ControllerException("Could not delete draft " + draft.getId()
                                                      + ". Delete manually.", e);
            }
            return true;
        }

        // change draft owner to system
        draft.setAccount(accountController.getSystemAccount());
        draft.setContents(newContents);
        draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
        draft.setReadGroup(group);

        try {
            return dao.update(draft) != null;
        } catch (DAOException e) {
            throw new ControllerException("Could not assign draft " + draftId + " to system", e);
        }
    }

    /**
     * Submits bulk import. If user performing the submission is not an administrator
     * then it is submitted for verification.
     *
     * @param entryAccount account of user performing the submission
     * @param type         type of bulk import
     * @param entryList    list of entries contained in this bulk import
     * @return true is import was submitted successfully, false otherwise
     * @throws ControllerException
     */
    public boolean submitBulkImport(Account entryAccount, EntryAddType type, ArrayList<EntryInfo> entryList,
            String groupUUID) throws ControllerException {

        boolean isAdmin = accountController.isAdministrator(entryAccount);
        Visibility visibility;

        if (!isAdmin)
            visibility = Visibility.PENDING;
        else
            visibility = Visibility.OK;

        ArrayList<Entry> contents = new ArrayList<Entry>();
        ArrayList<EntryInfo> contentList = new ArrayList<EntryInfo>();

        Group group = groupController.getGroupByUUID(groupUUID);

        // go through passed contents
        for (EntryInfo info : entryList) {
            info.setVisibility(visibility);
            processEntryInfo(entryAccount, type, info, contents, contentList, group);
        }

        if (isAdmin)
            return true;

        Account draftOwner = accountController.getSystemAccount();

        BulkUpload draft = new BulkUpload();
        draft.setName(entryAccount.getEmail());
        draft.setAccount(draftOwner);
        draft.setImportType(type.toString());
        draft.setContents(contents);
        draft.setCreationTime(new Date(System.currentTimeMillis()));
        draft.setLastUpdateTime(draft.getCreationTime());
        draft.setReadGroup(group);

        try {
            boolean success = dao.save(draft) != null;
            if (!success)
                return success;

            // TODO : send notification email
            return success;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public boolean approveBulkImport(Account account, long id, ArrayList<EntryInfo> entryList, String groupUUID)
            throws ControllerException, PermissionException {

        // only admins allowed
        if (!accountController.isAdministrator(account)) {
            throw new PermissionException("Only administrators can approve bulk imports");
        }

        // retrieve bulk upload in question
        BulkUpload bulkUpload;

        try {
            bulkUpload = dao.retrieveByIdWithContents(id);
            if (bulkUpload == null)
                throw new ControllerException("Could not retrieve bulk upload with id \"" + id
                                                      + "\" for approval");
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        // approve entries taking note of changes
        ArrayList<Entry> newContents = new ArrayList<Entry>();
        ArrayList<EntryInfo> contentList = new ArrayList<EntryInfo>();
        EntryAddType type = EntryAddType.stringToType(bulkUpload.getImportType());

        Group group = groupController.getGroupByUUID(groupUUID);

        // go through passed contents
        for (EntryInfo info : entryList) {
            info.setVisibility(Visibility.OK);
            processEntryInfo(account, type, info, newContents, contentList, group);
        }

        // TODO : there may be situations where the admin added something

        // when done approving, delete the bulk upload record but not the entries associated with it.
        try {
            bulkUpload.setContents(null);
            dao.delete(bulkUpload);
            return true;
        } catch (DAOException e) {
            throw new ControllerException("Could not delete bulk upload " + bulkUpload.getId()
                                                  + ". Contents were approved so please delete manually.", e);
        }
    }

    protected void saveSequence(Account account, ArrayList<SequenceAnalysisInfo> sequenceInfoList,
            Entry entry) throws ControllerException {
        if (sequenceInfoList == null || sequenceInfoList.isEmpty())
            return;

        String fileId = sequenceInfoList.get(0).getFileId();
        File file = new File(JbeirSettings.getSetting("TEMPORARY_DIRECTORY") + File.separatorChar
                                     + fileId);

        if (!file.exists()) {
            Logger.error("Could not find sequence file \"" + file.getAbsolutePath() + "\"");
            return;
        }

        try {
            String sequenceString = FileUtils.readFileToString(file);
            SequenceController controller = new SequenceController();
            controller.parseAndSaveSequence(account, entry, sequenceString);
        } catch (IOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }
}
