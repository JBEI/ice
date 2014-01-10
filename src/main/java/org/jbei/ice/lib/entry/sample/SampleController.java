package org.jbei.ice.lib.entry.sample;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.StorageInfo;

/**
 * ABI to manipulate {@link Sample}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class SampleController {

    private final SampleDAO dao;
    private final StorageController storageController;

    public SampleController() {
        dao = new SampleDAO();
        storageController = ControllerFactory.getStorageController();
    }

    /**
     * Checks if the user has write permission of the {@link Sample}. This is based on the entry that is
     * associated with the sample
     *
     * @param account Account of user
     * @param sample  sample being checked
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Account account, Sample sample) throws ControllerException {
        if (sample == null || sample.getEntry() == null) {
            throw new ControllerException("Failed to check write permissions for null sample!");
        }

        return ControllerFactory.getPermissionController().hasWritePermission(account, sample.getEntry());
    }

    /**
     * Save the {@link Sample} into the database, then rebuilds the search index.
     *
     * @param sample
     * @return Saved sample.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sample saveSample(Account account, Sample sample) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, sample)) {
            throw new PermissionException("No permissions to save sample!");
        }

        try {
            return dao.save(sample);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Delete the {@link Sample} in the database, then rebuild the search index. Also deletes the
     * associated {@link Storage}, if it is a tube.
     *
     * @param sample
     * @throws ControllerException
     * @throws PermissionException
     */
    public void deleteSample(Account account, Sample sample) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, sample)) {
            throw new PermissionException("No permissions to delete sample!");
        }

        try {
            Storage storage = sample.getStorage();
            dao.delete(sample);
            if (storage.getStorageType() == Storage.StorageType.TUBE) {
                storageController.delete(storage);
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the {@link Sample}s associated with the {@link Entry}.
     *
     * @param entry
     * @return ArrayList of {@link Sample}s.
     * @throws ControllerException
     */
    public ArrayList<Sample> getSamples(Entry entry) throws ControllerException {
        ArrayList<Sample> samples;
        try {
            samples = dao.getSamplesByEntry(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return samples;
    }

    /**
     * Retrieve the {@link Sample}s associated with the given {@link Storage}.
     *
     * @param storage
     * @return ArrayList of {@link Sample}s.
     * @throws ControllerException
     */
    public ArrayList<Sample> getSamplesByStorage(Storage storage) throws ControllerException {
        try {
            return dao.getSamplesByStorage(storage);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Sample getSampleById(long id) throws ControllerException {
        try {
            return dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public boolean hasSample(Entry entry) throws ControllerException {
        try {
            return dao.hasSample(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    // mainly used by the api to create a strain sample record
    public Sample createStrainSample(Account account, String recordId, String rack, String location, String barcode,
            String label, String strainNamePrefix) throws ControllerException {
        //check for administrative privileges
        if (!ControllerFactory.getAccountController().isAdministrator(account)) {
            throw new ControllerException("Must be an administrator to perform this action");
        }

        // check if there is an existing sample with barcode
        SampleController sampleController = ControllerFactory.getSampleController();
        Storage existing = storageController.retrieveStorageTube(barcode.trim());
        if (existing != null) {
            ArrayList<Sample> samples = sampleController.getSamplesByStorage(existing);
            if (samples != null && !samples.isEmpty()) {
                Logger.error("Barcode \"" + barcode + "\" already has a sample associated with it");
                return null;
            }
        }

        // retrieve entry for record id
        Entry entry;
        EntryController entryController = ControllerFactory.getEntryController();
        try {
            entry = entryController.getByRecordId(account, recordId);
            if (entry == null)
                throw new ControllerException("Could not locate entry to associate sample with");
        } catch (PermissionException e) {
            throw new ControllerException(e);
        }

        Logger.info("Creating new strain sample [" + rack + ", " + location + ", " + barcode + ", " + label
                            + "] for entry \"" + entry.getId());
        // TODO : this is a hack till we migrate to a single strain default
        Storage strainScheme = null;
        List<Storage> schemes = storageController.retrieveAllStorageSchemes();
        for (Storage storage : schemes) {
            if (storage.getStorageType() == Storage.StorageType.SCHEME
                    && "Strain Storage Matrix Tubes".equals(storage.getName())) {
                strainScheme = storage;
                break;
            }
        }

        if (strainScheme == null) {
            String errMsg = "Could not locate default strain scheme (Strain Storage Matrix Tubes[Plate, Well, Tube])";
            Logger.error(errMsg);
            throw new ControllerException(errMsg);
        }

        Storage newLocation = storageController.getLocation(strainScheme, new String[]{rack, location, barcode});

        Sample sample = SampleCreator.createSampleObject(label, account.getEmail(), "");
        sample.setEntry(entry);
        sample.setStorage(newLocation);
        try {
            sample = saveSample(account, sample);
            String name = entry.getName();
            if (strainNamePrefix != null && name != null && !name.startsWith(strainNamePrefix)) {
                entryController.updateWithNextStrainName(strainNamePrefix, entry);
            }
        } catch (PermissionException e) {
            throw new ControllerException(e);
        }
        return sample;
    }

    public SampleStorage createSample(Account account, long entryId, SampleStorage sampleStorage) {
        EntryController controller = ControllerFactory.getEntryController();
        StorageController storageController = ControllerFactory.getStorageController();

        Entry entry;
        try {
            entry = controller.get(account, entryId);
            if (entry == null) {
                Logger.error("Could not retrieve entry with id " + entryId + ". Skipping sample creation");
                return null;
            }

            if (!ControllerFactory.getPermissionController().hasWritePermission(account, entry)) {
                Logger.error(account.getEmail() + ": no write permissions to create sample for " + entryId);
                return null;
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        PartSample partSample = sampleStorage.getPartSample();
        LinkedList<StorageInfo> locations = sampleStorage.getStorageList();

        Sample sample = SampleCreator.createSampleObject(partSample.getLabel(), account.getEmail(),
                                                         partSample.getNotes());
        sample.setEntry(entry);

        if (locations == null || locations.isEmpty()) {
            Logger.info("Creating sample without location");

            // create sample, but not location
            try {
                sample = dao.save(sample);
                sampleStorage.getPartSample().setSampleId(sample.getId() + "");
                sampleStorage.getPartSample().setDepositor(account.getEmail());
                return sampleStorage;
            } catch (DAOException e) {
                Logger.error(e);
            }
            return null;
        }

        // create sample and location
        String[] labels = new String[locations.size()];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.length; i++) {
            labels[i] = locations.get(i).getDisplay();
            sb.append(labels[i]);
            if (i - 1 < labels.length)
                sb.append("/");
        }

        Logger.info("Creating sample with locations " + sb.toString());
        try {
            Storage scheme = storageController.get(Long.parseLong(partSample.getLocationId()), false);
            Storage storage = storageController.getLocation(scheme, labels);
            storage = storageController.update(storage);
            sample.setStorage(storage);
            sample = dao.save(sample);
            sampleStorage.getStorageList().clear();

            List<Storage> storages = StorageDAO.getStoragesUptoScheme(storage);
            if (storages != null) {
                for (Storage storage1 : storages) {
                    StorageInfo info = new StorageInfo();
                    info.setDisplay(storage1.getIndex());
                    info.setId(storage1.getId());
                    info.setType(storage1.getStorageType().name());
                    sampleStorage.getStorageList().add(info);
                }
            }

            sampleStorage.getPartSample().setSampleId(sample.getId() + "");
            sampleStorage.getPartSample().setDepositor(account.getEmail());
            return sampleStorage;
        } catch (NumberFormatException | DAOException | ControllerException e) {
            Logger.error(e);
        }

        return null;
    }
}
