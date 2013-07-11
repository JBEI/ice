package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbei.ice.client.entry.view.model.SampleStorage;
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
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.AccountInfo;
import org.jbei.ice.lib.shared.dto.AccountType;
import org.jbei.ice.lib.shared.dto.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.SampleInfo;
import org.jbei.ice.lib.shared.dto.Visibility;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;
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
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setEmail(draftAccount.getEmail());
            accountInfo.setFirstName(draftAccount.getFirstName());
            accountInfo.setLastName(draftAccount.getLastName());
            info.setAccount(accountInfo);

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
        for (Entry entry : contents) {
            ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
            boolean hasSequence = sequenceController.hasSequence(entry);
            boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry);
            EntryInfo info = ModelToInfoFactory.getInfo(account, entry, attachments, null, null,
                                                        hasSequence, hasOriginalSequence);

            // retrieve permission
            Set<Permission> entryPermissions = entry.getPermissions();
            if (entryPermissions != null && !entryPermissions.isEmpty()) {
                for (Permission permission : entryPermissions) {
                    info.getPermissions().add(Permission.toDTO(permission));
                }
            }

            // this conditional statement makes sure that plasmids are ignored if we are dealing
            // with strain with plasmid
            if (type == EntryAddType.STRAIN_WITH_PLASMID) {
                if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.getName())) {
                    // get plasmids
                    String plasmids = ((Strain) entry).getPlasmids();
                    Entry plasmid = BulkUploadUtil.getPartNumberForStrainPlasmid(account, entryController, plasmids);
                    if (plasmid != null) {
                        attachments = attachmentController.getByEntry(account, plasmid);
                        hasSequence = sequenceController.hasSequence(plasmid);
                        hasOriginalSequence = sequenceController.hasOriginalSequence(plasmid);
                        EntryInfo plasmidInfo = ModelToInfoFactory.getInfo(account, plasmid, attachments, null, null,
                                                                           hasSequence, hasOriginalSequence);
                        Set<Permission> permissions = plasmid.getPermissions();
                        if (permissions != null && !permissions.isEmpty()) {
                            for (Permission permission : permissions) {
                                plasmidInfo.getPermissions().add(Permission.toDTO(permission));
                            }
                        }
                        info.setInfo(plasmidInfo);
                    }
                } else
                    continue;
            }

            if (info != null) {
                SampleStorage sampleStorage = retrieveSampleStorage(entry);
                if (sampleStorage != null) {
                    ArrayList<SampleStorage> sampleStorageArrayList = new ArrayList<>();
                    sampleStorageArrayList.add(sampleStorage);
                    info.setSampleMap(sampleStorageArrayList);
                }
            }

            if (info != null)
                draftInfo.getEntryList().add(info);
        }

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

    protected SampleStorage retrieveSampleStorage(Entry entry) {
        ArrayList<Sample> samples;
        try {
            if (!sampleController.hasSample(entry))
                return null;

            samples = sampleController.getSamples(entry);
        } catch (ControllerException e) {
            return null;
        }

        if (samples != null && !samples.isEmpty()) {
            Sample sample = samples.get(0);
            SampleStorage sampleStorage = new SampleStorage();

            // convert sample to info
            SampleInfo sampleInfo = new SampleInfo();
            sampleInfo.setCreationTime(sample.getCreationTime());
            sampleInfo.setLabel(sample.getLabel());
            sampleInfo.setNotes(sample.getNotes());
            sampleInfo.setDepositor(sample.getDepositor());
            sampleStorage.setSample(sampleInfo);

            // convert sample to info
            Storage storage = sample.getStorage();

            while (storage != null) {

                if (storage.getStorageType() == Storage.StorageType.SCHEME) {
                    sampleInfo.setLocationId(storage.getId() + "");
                    sampleInfo.setLocation(storage.getName());
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
            draft = dao.retrieveById(draftId);
            if (draft == null)
                throw new ControllerException("Could not retrieve draft with id \"" + draftId + "\"");

            Account draftAccount = draft.getAccount();
            if (!requesting.equals(draftAccount) && !accountController.isAdministrator(requesting))
                throw new PermissionException("No permissions to delete draft " + draftId);

            // delete all associated entries. for strain with plasmids both are returned
            for (Entry entry : draft.getContents()) {
                try {
                    entryController.delete(requesting, entry);
                } catch (PermissionException pe) {
                    Logger.warn("Could not delete entry " + entry.getRecordId() + " for bulk upload " + draftId);
                }
            }

            dao.delete(draft);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        BulkUploadInfo draftInfo = BulkUpload.toDTO(draft);
        AccountInfo accountInfo = Account.toDTO(draft.getAccount());
        draftInfo.setAccount(accountInfo);
        return draftInfo;
    }

    public BulkUploadAutoUpdate autoUpdateBulkUpload(Account account, BulkUploadAutoUpdate autoUpdate,
            EntryAddType addType) throws ControllerException {
        // deal with bulk upload
        BulkUpload draft;
        try {
            draft = dao.retrieveById(autoUpdate.getBulkUploadId());
            if (draft == null) {
                // validate add type and entrytype
                if (addType != EntryAddType.STRAIN_WITH_PLASMID && EntryType.nameToType(addType.name()) != autoUpdate
                        .getType()) {
                    throw new ControllerException("Incompatible add type (" + addType.toString()
                                                          + " and auto update entry type ("
                                                          + autoUpdate.getType().toString() + ")");
                }

                draft = new BulkUpload();
                draft.setName("Untitled");
                draft.setAccount(account);
                draft.setStatus(BulkUploadStatus.IN_PROGRESS);
                draft.setImportType(addType.toString());
                draft.setCreationTime(new Date(System.currentTimeMillis()));
                draft.setLastUpdateTime(draft.getCreationTime());
                dao.save(draft);
                autoUpdate.setBulkUploadId(draft.getId());
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        // for strain with plasmid this is the strain
        Entry entry = entryController.get(account, autoUpdate.getEntryId());
        Entry otherEntry = null;  // for strain with plasmid this is the entry

        // if entry is null, create entry
        if (entry == null) {
            switch (autoUpdate.getType()) {
                case PLASMID:
                    entry = new Plasmid();
                    break;
                case STRAIN:
                    entry = new Strain();
                    break;
                case PART:
                    entry = new Part();
                    break;
                case ARABIDOPSIS:
                    entry = new ArabidopsisSeed();
                    break;

                default:
                    throw new ControllerException("Don't know what to do with entry type");
            }

            entry.setOwner(account.getFullName());
            entry.setOwnerEmail(account.getEmail());
            entry.setCreator(account.getFullName());
            entry.setCreatorEmail(account.getEmail());
            entry = entryController.createEntry(account, entry, null);

            // creates strain/plasmid at the same time for strain with plasmid
            if (addType == EntryAddType.STRAIN_WITH_PLASMID) {
                if (autoUpdate.getType() == EntryType.STRAIN) {
                    // created strain, now create plasmid
                    otherEntry = new Plasmid();
                    otherEntry.setOwner(account.getFullName());
                    otherEntry.setOwnerEmail(account.getEmail());
                    otherEntry.setCreator(account.getFullName());
                    otherEntry.setCreatorEmail(account.getEmail());
                    otherEntry.setVisibility(Visibility.DRAFT.getValue());
                    entryController.createEntry(account, otherEntry, null);
                    // link the plasmid to strain (strain gets updated later on)
                    String plasmidPartNumberString = "[[" + Utils.getConfigValue(ConfigurationKey.WIKILINK_PREFIX)
                            + ":" + otherEntry.getOnePartNumber().getPartNumber() + "|"
                            + otherEntry.getOnePartNumber().getPartNumber() + "]]";
                    ((Strain) entry).setPlasmids(plasmidPartNumberString);
                } else {
                    // created plasmid, now create strain and link
                    String plasmidPartNumberString = "[[" + Utils.getConfigValue(ConfigurationKey.WIKILINK_PREFIX)
                            + ":" + entry.getOnePartNumber().getPartNumber() + "|"
                            + entry.getOnePartNumber().getPartNumber() + "]]";
                    otherEntry = entry;
                    entry = new Strain();
                    entry.setOwner(account.getFullName());
                    entry.setOwnerEmail(account.getEmail());
                    entry.setCreator(account.getFullName());
                    entry.setCreatorEmail(account.getEmail());
                    ((Strain) entry).setPlasmids(plasmidPartNumberString);
                    entry.setVisibility(Visibility.DRAFT.getValue());
                    entryController.createEntry(account, entry, null);
                }
            }

            autoUpdate.setEntryId(entry.getId());
            draft.getContents().add(entry);
        } else {
            // entry not null (fetch plasmid for strain) if this is a strain with plasmid
            if (addType == EntryAddType.STRAIN_WITH_PLASMID) {
                String plasmid = ((Strain) entry).getPlasmids();
                otherEntry = BulkUploadUtil.getPartNumberForStrainPlasmid(account, entryController, plasmid);
            }
        }

        try {
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

            if (otherEntry != null) {
                if (otherEntry.getVisibility() == null || otherEntry.getVisibility() != Visibility.DRAFT.getValue())
                    otherEntry.setVisibility(Visibility.DRAFT.getValue());

                entryController.update(account, otherEntry);
            }

            if (entry.getVisibility() == null || entry.getVisibility() != Visibility.DRAFT.getValue())
                entry.setVisibility(Visibility.DRAFT.getValue());
            entryController.update(account, entry);
        } catch (PermissionException e) {
            throw new ControllerException(e);
        }

        // update bulk upload. even if no new entry was created, entries belonging to it was updated
        try {
            draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
            autoUpdate.setLastUpdate(draft.getLastUpdateTime());
            dao.update(draft);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
        return autoUpdate;
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

                    if (isStrainWithPlasmid && entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.getName())) {
                        String plasmids = ((Strain) entry).getPlasmids();
                        Entry plasmid = BulkUploadUtil.getPartNumberForStrainPlasmid(account, entryController,
                                                                                     plasmids);
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

            // set permissions
            for (Permission permission : bulkUpload.getPermissions()) {
                PermissionInfo info = new PermissionInfo();
                info.setType(PermissionInfo.Type.READ_ENTRY);
                info.setTypeId(entry.getId());
                info.setArticleId(permission.getGroup().getId());
                info.setArticle(PermissionInfo.Article.GROUP);
                Permission entryPermission = ControllerFactory.getPermissionController().addPermission(account, info);
                entry.getPermissions().add(entryPermission);
            }
            entryController.update(account, entry);
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

    public Long updatePermissions(Account account, long bulkUploadId, EntryAddType addType,
            ArrayList<PermissionInfo> permissions) throws ControllerException {
        BulkUpload upload;

        try {
            upload = dao.retrieveById(bulkUploadId);
            if (upload == null) {
                upload = BulkUploadUtil.createNewBulkUpload(addType);
                upload.setAccount(account);
                upload = dao.save(upload);
            } else {
                if (!upload.getAccount().equals(account) && account.getType() != AccountType.ADMIN)
                    throw new ControllerException("No permissions to update bulk upload");
            }

            ArrayList<Permission> existingPermissions = new ArrayList<>(upload.getPermissions());
            upload.getPermissions().clear();

            // update permissions
            for (PermissionInfo info : permissions) {
                Group group = ControllerFactory.getGroupController().getGroupById(info.getArticleId());
                if (group == null)
                    continue;

                long startSize = upload.getPermissions().size();

                // article is not unique to each permission, but the combination will be unique
                // currently, permissions for bulk upload is restricted to read permissions by groups
                for (Permission permission : existingPermissions) {
                    if (permission.getGroup().getId() == info.getArticleId()) {
                        upload.getPermissions().add(permission);
                        break;
                    }
                }

                // existing permission was found
                if (upload.getPermissions().size() > startSize)
                    continue;

                // new permission
                Permission permission = ControllerFactory.getPermissionController().recordPermission(info);
                upload.getPermissions().add(permission);
            }

            dao.update(upload);
            return upload.getId();
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
