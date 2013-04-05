package org.jbei.ice.lib.entry.sample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.utils.Utils;

/**
 * ABI to manipulate {@link Sample}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class SampleController {
    private final SampleDAO dao;
    private final PermissionsController permissionsController;
    private final StorageController storageController;

    public SampleController() {
        dao = new SampleDAO();
        permissionsController = ControllerFactory.getPermissionController();
        storageController = ControllerFactory.getStorageController();
    }

    /**
     * Create a {@link Sample} object.
     * <p/>
     * Generates the UUID and the time stamps.
     *
     * @param label     display label for sample
     * @param depositor name of the depositor
     * @param notes     associated notes
     * @return {@link Sample}
     */
    public Sample createSample(String label, String depositor, String notes) {
        String uuid = Utils.generateUUID();
        Date creationTime = Calendar.getInstance().getTime();

        Sample sample = new Sample();
        sample.setLabel(label);
        sample.setDepositor(depositor);
        sample.setNotes(notes);
        sample.setUuid(uuid);
        sample.setCreationTime(creationTime);
        sample.setModificationTime(null);
        return sample;
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

        return permissionsController.hasWritePermission(account, sample.getEntry());
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

    public ArrayList<Sample> getSamplesByEntry(Entry entry) throws ControllerException {
        try {
            return dao.getSamplesByEntry(entry);
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
}
