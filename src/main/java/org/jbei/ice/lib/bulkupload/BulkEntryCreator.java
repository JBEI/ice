package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.StorageInfo;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadStatus;
import org.jbei.ice.lib.shared.dto.bulkupload.EditMode;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.InfoToModelFactory;

/**
 * Creates entries for bulk uploads
 *
 * @author Hector Plahar
 */
public class BulkEntryCreator {

    private final BulkUploadDAO dao;
    private final AccountController accountController;
    private final EntryController entryController;

    public BulkEntryCreator() {
        dao = new BulkUploadDAO();
        accountController = ControllerFactory.getAccountController();
        entryController = ControllerFactory.getEntryController();
    }

    protected BulkUpload createOrRetrieveBulkUpload(Account account, BulkUploadAutoUpdate autoUpdate,
            EntryAddType addType) throws ControllerException {
        try {
            BulkUpload draft = dao.retrieveById(autoUpdate.getBulkUploadId());
            if (draft == null) {
                // validate add type and entry type
                if (addType != EntryAddType.STRAIN_WITH_PLASMID && EntryType.nameToType(
                        addType.name()) != autoUpdate.getType()) {
                    throw new ControllerException("Incompatible add type [" + addType.toString()
                                                          + "] and auto update entry type ["
                                                          + autoUpdate.getType().toString() + "]");
                }

                draft = new BulkUpload();
                draft.setName("Untitled");
                draft.setAccount(account);
                draft.setStatus(BulkUploadStatus.IN_PROGRESS);
                draft.setImportType(addType.toString());
                draft.setCreationTime(new Date(System.currentTimeMillis()));
                draft.setLastUpdateTime(draft.getCreationTime());
                dao.save(draft);
            }
            return draft;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
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
            EntryAddType addType) throws ControllerException {
        Account account = accountController.getByEmail(userId);
        BulkUpload draft = null;

        // for bulk edit, drafts will not exist
        if (autoUpdate.getEditMode() != EditMode.BULK_EDIT) {
            draft = createOrRetrieveBulkUpload(account, autoUpdate, addType);
            autoUpdate.setBulkUploadId(draft.getId());
        }

        // for strain with plasmid this is the strain
        Entry entry = entryController.get(account, autoUpdate.getEntryId());
        Entry otherEntry = null;  // for strain with plasmid this is the entry

        // if entry is null, create entry
        if (entry == null) {
            entry = EntryUtil.createEntryFromType(autoUpdate.getType(), account.getFullName(), account.getEmail());
            if (entry == null)
                throw new ControllerException("Don't know what to do with entry type");

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
                    entry.getLinkedEntries().add(otherEntry);
                } else {
                    // created plasmid, now create strain and link
                    otherEntry = entry;
                    entry = new Strain();
                    entry.setOwner(account.getFullName());
                    entry.setOwnerEmail(account.getEmail());
                    entry.setCreator(account.getFullName());
                    entry.setCreatorEmail(account.getEmail());
                    entry.getLinkedEntries().add(otherEntry);
                    entry.setVisibility(Visibility.DRAFT.getValue());
                    entryController.createEntry(account, entry, null);
                }
            }

            autoUpdate.setEntryId(entry.getId());
            if (draft != null) {
                draft.getContents().add(entry);
            }
        } else {
            // entry not null (fetch plasmid for strain) if this is a strain with plasmid
            if (addType == EntryAddType.STRAIN_WITH_PLASMID && !entry.getLinkedEntries().isEmpty()) {
                otherEntry = (Entry) entry.getLinkedEntries().toArray()[0];
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

            // set the plasmids and update
            if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.toString())
                    && entry.getLinkedEntries().isEmpty()) {
                Strain strain = (Strain) entry;
                entryController.setStrainPlasmids(account, strain, strain.getPlasmids());
            }

            entryController.update(account, entry);

            // create Sample
            try {
                createSample(account, entry, autoUpdate);
            } catch (ControllerException ce) {
                Logger.error("Exception creating sample", ce);
            }
        } catch (PermissionException e) {
            throw new ControllerException(e);
        }

        // update bulk upload. even if no new entry was created, entries belonging to it was updated
        if (draft != null) {
            try {
                draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
                autoUpdate.setLastUpdate(draft.getLastUpdateTime());
                dao.update(draft);
            } catch (DAOException de) {
                throw new ControllerException(de);
            }
        }
        return autoUpdate;
    }

    protected void createSample(Account account, Entry entry, BulkUploadAutoUpdate autoUpdate)
            throws ControllerException {
        // only supports seed storage default
        if (!entry.getRecordType().equalsIgnoreCase(EntryType.ARABIDOPSIS.getName()))
            return;

        String uuid = Utils.getConfigValue(ConfigurationKey.ARABIDOPSIS_STORAGE_DEFAULT);
        if (uuid == null || uuid.trim().isEmpty())
            return;

        Storage seedRootStorage = ControllerFactory.getStorageController().retrieveByUUID(uuid);
        if (seedRootStorage == null)
            return;

        // fields anticipated
        String name = autoUpdate.getKeyValue().get(EntryField.SAMPLE_NAME);
        String notes = autoUpdate.getKeyValue().get(EntryField.SAMPLE_NOTES);
        String shelf = autoUpdate.getKeyValue().get(EntryField.SAMPLE_SHELF);
        String box = autoUpdate.getKeyValue().get(EntryField.SAMPLE_BOX);
        String tubeNumber = autoUpdate.getKeyValue().get(EntryField.SAMPLE_TUBE_NUMBER);
        String tubeBarcode = autoUpdate.getKeyValue().get(EntryField.SAMPLE_TUBE_BARCODE);

        // expected fields
        if (name == null || shelf == null || box == null || tubeNumber == null || tubeBarcode == null) {
            return;
        }

        PartSample partSample = new PartSample();
        partSample.setCreationTime(new Date(System.currentTimeMillis()));
        partSample.setLabel(name);
        partSample.setNotes(notes == null ? "" : notes);
        partSample.setDepositor(account.getEmail());
        partSample.setLocationId(Long.toString(seedRootStorage.getId()));

        // storage list
        List<StorageInfo> storageList = new ArrayList<>();
        storageList.add(new StorageInfo(shelf));
        storageList.add(new StorageInfo(box));
        storageList.add(new StorageInfo(tubeNumber));
        storageList.add(new StorageInfo(tubeBarcode));

        SampleStorage sampleStorage = new SampleStorage(partSample, storageList);
        ControllerFactory.getSampleController().createSample(account, autoUpdate.getEntryId(), sampleStorage);
    }
}
