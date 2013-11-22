package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.Preference;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadStatus;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;
import org.jbei.ice.lib.shared.dto.user.User;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.server.ModelToInfoFactory;

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
    private SampleController sampleController;
    private PreferencesController preferencesController;

    /**
     * Initialises dao and controller dependencies. These need be injected
     */
    public BulkUploadController() {
        dao = new BulkUploadDAO();
        accountController = ControllerFactory.getAccountController();
        entryController = ControllerFactory.getEntryController();
        attachmentController = ControllerFactory.getAttachmentController();
        sequenceController = ControllerFactory.getSequenceController();
        sampleController = ControllerFactory.getSampleController();
        preferencesController = ControllerFactory.getPreferencesController();
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
            User user = new User();
            user.setEmail(draftAccount.getEmail());
            user.setFirstName(draftAccount.getFirstName());
            user.setLastName(draftAccount.getLastName());
            info.setAccount(user);

            info.setId(draft.getId());
            info.setLastUpdate(draft.getLastUpdateTime());
            int count = draft.getContents().size();
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
    public BulkUploadInfo retrieveById(Account account, long id, int start, int limit)
            throws ControllerException, PermissionException {
        BulkUpload draft;

        try {
            draft = dao.retrieveById(id);
            if (draft == null)
                return null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        boolean isModerator = accountController.isAdministrator(account);
        boolean isOwner = account.equals(draft.getAccount());

        // check for permissions to retrieve this bulk import
        if (!isModerator && !isOwner) {
            throw new PermissionException("Insufficient privileges by " + account.getEmail()
                                                  + " to view bulk import for " + draft.getAccount().getEmail());
        }

        // convert bulk import db object to data transfer object
        int size = 0;
        try {
            size = dao.retrieveSavedDraftCount(id);
        } catch (DAOException e) {
            Logger.error(e);
        }
        BulkUploadInfo draftInfo = BulkUpload.toDTO(draft);
        draftInfo.setCount(size);
        EntryAddType type = EntryAddType.stringToType(draft.getImportType());
        EntryType retrieveType = type == EntryAddType.STRAIN_WITH_PLASMID ? EntryType.STRAIN : null;

        // retrieve the entries associated with the bulk import
        ArrayList<Entry> contents;
        try {
            contents = dao.retrieveDraftEntries(retrieveType, id, start, limit);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }

        // convert
        draftInfo.getEntryList().addAll(convertParts(account, type, contents));

        HashMap<PreferenceKey, String> userSaved = null;
        try {
            ArrayList<PreferenceKey> keys = new ArrayList<>();
            keys.add(PreferenceKey.FUNDING_SOURCE);
            keys.add(PreferenceKey.PRINCIPAL_INVESTIGATOR);
            userSaved = preferencesController.retrieveAccountPreferences(account, keys);
        } catch (ControllerException ce) {
            // bulk upload should continue to work in the event of this exception
            Logger.warn(ce.getMessage());
        }

        // retrieve preferences (if any: this is where you also add user's saved preferences if it does not exist)
        for (Preference preference : draft.getPreferences()) {
            PreferenceKey preferenceKey = PreferenceKey.fromString(preference.getKey());
            if (preferenceKey != null && userSaved != null && userSaved.containsKey(preferenceKey))
                userSaved.remove(preferenceKey); // bulk preferences has precedence over user saved

            PreferenceInfo preferenceInfo = Preference.toDTO(preference);
            if (preferenceInfo != null)
                draftInfo.getPreferences().add(preferenceInfo);
        }

        if (userSaved != null && !userSaved.isEmpty()) {
            for (Map.Entry<PreferenceKey, String> entry : userSaved.entrySet()) {
                PreferenceInfo preferenceInfo = new PreferenceInfo(true, entry.getKey().toString(), entry.getValue());
                draftInfo.getPreferences().add(preferenceInfo);
            }
        }
        return draftInfo;
    }

    protected ArrayList<PartData> convertParts(Account account, EntryAddType type, ArrayList<Entry> contents)
            throws ControllerException {
        ArrayList<PartData> addList = new ArrayList<>();

        for (Entry entry : contents) {
            ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
            boolean hasSequence = sequenceController.hasSequence(entry.getId());
            boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
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
                    info.getAccessPermissions().add(Permission.toDTO(permission));
                }
            }

            // this conditional statement makes sure that plasmids are ignored if we are dealing
            // with strain with plasmid
            if (type != null && type == EntryAddType.STRAIN_WITH_PLASMID) {
                if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.getName()) &&
                        !entry.getLinkedEntries().isEmpty()) {
                    // get plasmids
                    Entry plasmid = (Entry) entry.getLinkedEntries().toArray()[0];
                    attachments = attachmentController.getByEntry(account, plasmid);
                    hasSequence = sequenceController.hasSequence(plasmid.getId());
                    hasOriginalSequence = sequenceController.hasOriginalSequence(plasmid.getId());
                    PartData plasmidInfo = ModelToInfoFactory.getInfo(plasmid);
                    ArrayList<AttachmentInfo> partAttachments = ModelToInfoFactory.getAttachments(attachments);
                    plasmidInfo.setAttachments(partAttachments);
                    plasmidInfo.setHasAttachment(!partAttachments.isEmpty());
                    plasmidInfo.setHasSequence(hasSequence);
                    plasmidInfo.setHasOriginalSequence(hasOriginalSequence);
                    Set<Permission> permissions = plasmid.getPermissions();
                    if (permissions != null && !permissions.isEmpty()) {
                        for (Permission permission : permissions) {
                            plasmidInfo.getAccessPermissions().add(Permission.toDTO(permission));
                        }
                    }
                    info.setInfo(plasmidInfo);
                } else
                    continue;
            }

//            SampleStorage sampleStorage = retrieveSampleStorage(entry);
//            if (sampleStorage != null) {
//                ArrayList<SampleStorage> sampleStorageArrayList = new ArrayList<>();
//                sampleStorageArrayList.add(sampleStorage);
//                info.setSampleMap(sampleStorageArrayList);
//            }

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
        bulkUploadInfo.getEntryList().addAll(convertParts(account, null, parts));
        return bulkUploadInfo;
    }

    protected SampleStorage retrieveSampleStorage(Entry entry) {
        ArrayList<Sample> samples = null;
        try {
            if (!sampleController.hasSample(entry))
                return null;

//            samples = sampleController.getSamples(entry);
        } catch (ControllerException e) {
            return null;
        }

        if (samples != null && !samples.isEmpty()) {
            Sample sample = samples.get(0);
            SampleStorage sampleStorage = new SampleStorage();

            // convert sample to info
            PartSample partSample = new PartSample();
            partSample.setCreationTime(sample.getCreationTime());
            partSample.setLabel(sample.getLabel());
            partSample.setNotes(sample.getNotes());
            partSample.setDepositor(sample.getDepositor());
            sampleStorage.setPartSample(partSample);

            // convert sample to info
            Storage storage = sample.getStorage();

            while (storage != null) {
                if (storage.getStorageType() == Storage.StorageType.SCHEME) {
                    partSample.setLocationId(storage.getId() + "");
                    partSample.setLocation(storage.getName());
                    break;
                }

                sampleStorage.getStorageList().add(ModelToInfoFactory.getStorageInfo(storage));
                storage = storage.getParent();
            }
            return sampleStorage;
        }
        return null;
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
            boolean isAdmin = accountController.isAdministrator(account);
            if (!isOwner && !isAdmin)
                continue;

            BulkUploadInfo draftInfo = new BulkUploadInfo();
            draftInfo.setCreated(draft.getCreationTime());
            draftInfo.setLastUpdate(draft.getLastUpdateTime());
            draftInfo.setId(draft.getId());

            draftInfo.setName(draft.getName());
            draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));
            draftInfo.setCount(draft.getContents().size());

            // set the account info
            User user = new User();
            user.setEmail(draftAccount.getEmail());
            user.setFirstName(draftAccount.getFirstName());
            user.setLastName(draftAccount.getLastName());
            draftInfo.setAccount(user);
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
            draft = dao.retrieveById(draftId);
            if (draft == null)
                throw new ControllerException("Could not retrieve draft with id \"" + draftId + "\"");

            Account draftAccount = draft.getAccount();
            if (!requesting.equals(draftAccount) && !accountController.isAdministrator(requesting))
                throw new PermissionException("No permissions to delete draft " + draftId);

            // delete all associated entries. for strain with plasmids both are returned
            for (Entry entry : draft.getContents()) {
                try {
                    entryController.delete(requesting, entry.getId());
                } catch (PermissionException pe) {
                    Logger.warn("Could not delete entry " + entry.getRecordId() + " for bulk upload " + draftId);
                }
            }

            dao.delete(draft);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        BulkUploadInfo draftInfo = BulkUpload.toDTO(draft);
        User user = Account.toDTO(draft.getAccount());
        draftInfo.setAccount(user);
        return draftInfo;
    }

    public BulkUploadAutoUpdate autoUpdateBulkUpload(String userId, BulkUploadAutoUpdate autoUpdate,
            EntryAddType addType) throws ControllerException {
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
    public boolean submitBulkImportDraft(Account account, long draftId, ArrayList<UserGroup> readGroups)
            throws ControllerException, PermissionException {
        // retrieve draft
        BulkUpload draft;
        try {
            draft = dao.retrieveById(draftId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (draft == null)
            throw new ControllerException("Could not retrieve draft by id " + draftId);

        // check permissions
        if (!draft.getAccount().equals(account) && !accountController.isAdministrator(account))
            throw new PermissionException("User " + account.getEmail()
                                                  + " does not have permission to update draft " + draftId);

        // update permissions
        if (readGroups != null && !readGroups.isEmpty()) {
            updatePermissions(draft, readGroups);
        }

        // set preferences
        Set<Preference> bulkUploadPreferences = new HashSet<>(draft.getPreferences());

        if (!bulkUploadPreferences.isEmpty()) {
            for (Entry entry : draft.getContents()) {
                for (Preference preference : bulkUploadPreferences) {
                    EntryField field = EntryField.fromString(preference.getKey());
                    InfoToModelFactory.infoToEntryForField(entry, null, preference.getValue(), field);
                }
            }
        }

        if (!BulkUploadUtil.validate(draft)) {
            Logger.warn("Attempting to submit a bulk upload draft (" + draftId + ") which does not validate");
            return false;
        }

        boolean isStrainWithPlasmid = EntryAddType.STRAIN_WITH_PLASMID.getDisplay().equalsIgnoreCase(
                draft.getImportType());

        draft.setStatus(BulkUploadStatus.PENDING_APPROVAL);
        draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
        draft.setName(account.getEmail());

        try {
            boolean success = dao.update(draft) != null;
            if (success) {
                // convert entries to pending
                for (Entry entry : draft.getContents()) {
                    entry.setVisibility(Visibility.PENDING.getValue());

                    entryController.update(account, entry);

                    if (isStrainWithPlasmid && entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.getName())
                            && !entry.getLinkedEntries().isEmpty()) {
                        Entry plasmid = (Entry) entry.getLinkedEntries().toArray()[0];
                        plasmid.setVisibility(Visibility.PENDING.getValue());
                        entryController.update(account, plasmid);
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
        } catch (DAOException e) {
            throw new ControllerException("Could not assign draft " + draftId + " to system", e);
        }
    }

    public boolean revertSubmitted(Account account, long uploadId) throws ControllerException {
        boolean isAdmin = accountController.isAdministrator(account);
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
        if (!accountController.isAdministrator(account)) {
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

            // set permissions
            for (Permission permission : bulkUpload.getPermissions()) {
                // add permission for entry
                AccessPermission access = new AccessPermission();
                access.setType(AccessPermission.Type.READ_ENTRY);
                access.setTypeId(entry.getId());
                access.setArticleId(permission.getGroup().getId());
                access.setArticle(AccessPermission.Article.GROUP);
                ControllerFactory.getPermissionController().addPermission(account, access);
                if (plasmid != null) {
                    access.setTypeId(plasmid.getId());
                    ControllerFactory.getPermissionController().addPermission(account, access);
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

    public boolean renameDraft(Account account, long id, String draftName) throws ControllerException {
        BulkUpload upload;

        try {
            upload = dao.retrieveById(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (!upload.getAccount().equals(account) && account.getType() != AccountType.ADMIN)
            throw new ControllerException("No permissions to rename");

        upload.setName(draftName);
        try {
            return dao.update(upload) != null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    void updatePermissions(BulkUpload upload, ArrayList<UserGroup> groups) throws ControllerException {
        try {
            ArrayList<Permission> existingPermissions = new ArrayList<>(upload.getPermissions());
            upload.getPermissions().clear();

            // update permissions
            for (UserGroup userGroup : groups) {
                Group group = ControllerFactory.getGroupController().getGroupById(userGroup.getId());
                if (group == null)
                    continue;

                long startSize = upload.getPermissions().size();

                // article is not unique to each permission, but the combination will be unique
                // currently, permissions for bulk upload is restricted to read permissions by groups
                for (Permission permission : existingPermissions) {
                    if (permission.getGroup().getId() == group.getId()) {
                        upload.getPermissions().add(permission);
                        break;
                    }
                }

                // existing permission was found
                if (upload.getPermissions().size() > startSize)
                    continue;

                // new permission
                AccessPermission access = new AccessPermission();
                access.setArticle(AccessPermission.Article.GROUP);
                access.setType(AccessPermission.Type.READ_ENTRY);
                access.setArticleId(group.getId());

                Permission permission = ControllerFactory.getPermissionController().recordGroupPermission(access);
                upload.getPermissions().add(permission);
            }

            dao.update(upload);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public long updatePreference(Account account, long bulkUploadId, EntryAddType addType, PreferenceInfo info)
            throws ControllerException {
        BulkUpload upload;

        try {
            upload = dao.retrieveById(bulkUploadId);
            if (upload == null) {
                upload = BulkUploadUtil.createNewBulkUpload(addType);
                upload.setAccount(account);
                upload = dao.save(upload);
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (!upload.getAccount().equals(account) && account.getType() != AccountType.ADMIN)
            throw new ControllerException("No permissions");

        if (info.isAdd()) {
            Preference preference = preferencesController.createPreference(account, info.getKey(), info.getValue());
            upload.getPreferences().add(preference);
        } else {
            Preference preference = preferencesController.retrievePreference(account, info.getKey(), info.getValue());
            if (preference != null)
                upload.getPreferences().remove(preference);
        }
        upload.setLastUpdateTime(new Date(System.currentTimeMillis()));
        try {
            dao.update(upload);
            return upload.getId();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
