package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.BulkUploadDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.PartSample;
import org.jbei.ice.lib.dto.StorageInfo;
import org.jbei.ice.lib.dto.bulkupload.EditMode;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.sample.SampleStorage;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryEditor;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.servlet.InfoToModelFactory;

import org.apache.commons.lang.StringUtils;

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

    public BulkEntryCreator() {
        dao = DAOFactory.getBulkUploadDAO();
        entryDAO = DAOFactory.getEntryDAO();
        creator = new EntryCreator();
        accountController = new AccountController();
        entryController = new EntryController();
        authorization = new BulkUploadAuthorization();
    }

    protected BulkUpload createOrRetrieveBulkUpload(Account account, BulkUploadAutoUpdate autoUpdate,
            EntryType addType) throws ControllerException {
        try {
            BulkUpload draft = dao.retrieveById(autoUpdate.getBulkUploadId());
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
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public PartData createEntry(String userId, long bulkUploadId, PartData data) {
        BulkUpload upload = dao.get(bulkUploadId);
        authorization.expectRead(userId, upload);
        data.setType(EntryType.nameToType(upload.getImportType()));
        Entry entry = InfoToModelFactory.infoToEntry(data);
        entry.setVisibility(Visibility.DRAFT.getValue());
        entry = entryDAO.create(entry);
        upload.getContents().add(entry);  // todo : performance ; add manually instead of loading
        dao.update(upload);
        data.setId(entry.getId());
        data.setModificationTime(entry.getModificationTime().getTime());
        return data;
    }

    public PartData updateEntry(String userId, long bulkUploadId, long id, PartData data) {
        BulkUpload upload = dao.get(bulkUploadId);
        authorization.expectWrite(userId, upload);

        Entry entry = entryDAO.get(id);
        // todo : check that entry is a part of upload and they are of the same type
        entry = InfoToModelFactory.updateEntryField(data, entry);
        entry.setVisibility(Visibility.DRAFT.getValue());
        entry.setModificationTime(new Date(System.currentTimeMillis()));
        entry = entryDAO.update(entry);

        upload.setLastUpdateTime(entry.getModificationTime());
        dao.update(upload);
        data.setId(id);
        data.setModificationTime(entry.getModificationTime().getTime());
        return data;
    }

    public BulkUploadInfo bulkUpdate(String userId, long id, BulkUploadInfo info) {
        // upload is allowed to be null
        BulkUpload upload = dao.get(id);
        if (upload == null)
            return null;

        authorization.expectWrite(userId, upload);
        Date updateTime = new Date(System.currentTimeMillis());
        upload.setLastUpdateTime(updateTime);

        if(StringUtils.isNotEmpty(info.getName()))
            upload.setName(info.getName());

        if(info.getStatus() != null)
            upload.setStatus(info.getStatus());
        dao.update(upload);
        info.setLastUpdate(updateTime);
        return info;
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
            EntryType addType) throws ControllerException {
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
                throw new ControllerException("Don't know what to do with entry type");

            entry = creator.createEntry(account, entry, null);

            autoUpdate.setEntryId(entry.getId());
            if (draft != null) {
                draft.getContents().add(entry);
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

            EntryEditor editor = new EntryEditor();
            // set the plasmids and update
            if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.toString())
                    && entry.getLinkedEntries().isEmpty()) {
                Strain strain = (Strain) entry;
                editor.setStrainPlasmids(account, strain, strain.getPlasmids());
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

        Storage seedRootStorage = DAOFactory.getStorageDAO().get(uuid);
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
        new SampleController().createSample(account, autoUpdate.getEntryId(), sampleStorage);
    }
}
