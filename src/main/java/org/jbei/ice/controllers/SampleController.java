package org.jbei.ice.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.SamplePermissionVerifier;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.ColumnField;

/**
 * ABI to manipulate {@link Sample}s.
 * 
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 * 
 */
public class SampleController extends Controller {
    public SampleController(Account account) {
        super(account, new SamplePermissionVerifier());
    }

    /**
     * Create a {@link Sample} object.
     * <p>
     * Generates the UUID and the time stamps.
     * 
     * @param label
     * @param depositor
     * @param notes
     * @return {@link Sample}
     */
    public Sample createSample(String label, String depositor, String notes) {
        return createSample(label, depositor, notes, Utils.generateUUID(), Calendar.getInstance()
                .getTime(), null);
    }

    /**
     * Create a {@link Sample} object.
     * 
     * @param label
     * @param depositor
     * @param notes
     * @param uuid
     * @param creationTime
     * @param modificationTime
     * @return {@link Sample}
     */
    public Sample createSample(String label, String depositor, String notes, String uuid,
            Date creationTime, Date modificationTime) {
        Sample sample = new Sample();

        sample.setLabel(label);
        sample.setDepositor(depositor);
        sample.setNotes(notes);
        sample.setUuid(uuid);
        sample.setCreationTime(creationTime);
        sample.setModificationTime(modificationTime);

        return sample;
    }

    /**
     * Checks if the user has read permission of the {@link Sample}.
     * 
     * @param sample
     * @return True if user has read permission.
     * @throws ControllerException
     */
    public boolean hasReadPermission(Sample sample) throws ControllerException {
        if (sample == null) {
            throw new ControllerException("Failed to check read permissions for null sample!");
        }

        return getSamplePermissionVerifier().hasReadPermissions(sample, getAccount());
    }

    /**
     * Checks if the user has write permission of the {@link Sample}.
     * 
     * @param sample
     * @return True if user has write permission.
     * @throws ControllerException
     */
    public boolean hasWritePermission(Sample sample) throws ControllerException {
        if (sample == null) {
            throw new ControllerException("Failed to check write permissions for null sample!");
        }

        return getSamplePermissionVerifier().hasWritePermissions(sample, getAccount());
    }

    /**
     * Save the {@link Sample} into the database, then rebuilds the search index.
     * 
     * @param sample
     * @return Saved sample.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sample saveSample(Sample sample) throws ControllerException, PermissionException {
        return saveSample(sample, true);
    }

    /**
     * Save the {@link Sample} into the database, with the option to rebuild the search index.
     * 
     * @param sample
     * @param scheduleIndexRebuild
     * @return saved sample.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Sample saveSample(Sample sample, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(sample)) {
            throw new PermissionException("No permissions to save sample!");
        }

        Sample savedSample = null;

        try {
            savedSample = SampleManager.saveSample(sample);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return savedSample;
    }

    /**
     * Delete the {@link Sample} in the database, then rebuild the search index. Also deletes the
     * associated {@link Storage}, if it is a tube.
     * 
     * @param sample
     * @throws ControllerException
     * @throws PermissionException
     */
    public void deleteSample(Sample sample) throws ControllerException, PermissionException {
        deleteSample(sample, true);
    }

    /**
     * Delete the {@link Sample} in the database, with the option to rebuild the search index. Also
     * deletes the associated {@link Storage}, if it is a tube.
     * 
     * @param sample
     * @param scheduleIndexRebuild
     * @throws ControllerException
     * @throws PermissionException
     */
    public void deleteSample(Sample sample, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(sample)) {
            throw new PermissionException("No permissions to delete sample!");
        }

        try {
            Storage storage = sample.getStorage();

            SampleManager.deleteSample(sample);

            if (storage.getStorageType() == Storage.StorageType.TUBE) {
                StorageManager.delete(storage);
            }

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the number of {@link Sample}s associated with the {@link Entry}.
     * 
     * @param entry
     * @return Number of samples associated with the entry.
     * @throws ControllerException
     */
    public long getNumberOfSamples(Entry entry) throws ControllerException {
        long result = 0;
        try {
            ArrayList<Sample> samples = SampleManager.getSamplesByEntry(entry);

            result = (samples == null) ? 0 : samples.size();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Retrieve the {@link Sample}s associated with the {@link Entry}.
     * 
     * @param entry
     * @return ArrayList of {@link Sample}s.
     * @throws ControllerException
     */
    public ArrayList<Sample> getSamples(Entry entry) throws ControllerException {
        ArrayList<Sample> samples = null;

        try {
            samples = SampleManager.getSamplesByEntry(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return samples;
    }

    /**
     * Retrieve the {@link Sample}s associated with the given depositor's email.
     * 
     * @param depositorEmail
     * @param offset
     * @param limit
     * @return ArrayList of {@link Sample}s.
     * @throws ControllerException
     */
    public ArrayList<Sample> getSamplesByDepositor(String depositorEmail, int offset, int limit)
            throws ControllerException {
        ArrayList<Sample> samples = null;

        try {
            samples = SampleManager.getSamplesByDepositor(depositorEmail, offset, limit);
        } catch (ManagerException e) {
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
            return SampleManager.getSamplesByStorage(storage);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the number of {@link Sample}s by the given depositor's email.
     * 
     * @param depositorEmail
     * @return Number of {@link Sample}s.
     * @throws ControllerException
     */
    public int getNumberOfSamplesByDepositor(String depositorEmail) throws ControllerException {

        try {
            return SampleManager.getSampleCountBy(depositorEmail);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public LinkedList<Long> retrieveSamplesByDepositor(String email, ColumnField field, boolean asc)
            throws ControllerException {

        LinkedList<Long> results = null;
        try {
            switch (field) {

            default:
            case CREATED:
                results = SampleManager.retrieveSamplesByDepositorSortByCreated(email, asc);
                //                getSamplePermissionVerifier().hasReadPermissions(model, account) // TODO 
                break;
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return results;
    }

    public LinkedList<Sample> retrieveSamplesByIdSet(LinkedList<Long> ids, boolean asc)
            throws ControllerException {
        try {
            return SampleManager.getSamplesByIdSet(ids, asc);
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Return the {@link SamplePermissionVerifier}.
     * 
     * @return samplePermissionVerifier.
     */
    protected SamplePermissionVerifier getSamplePermissionVerifier() {
        return (SamplePermissionVerifier) getPermissionVerifier();
    }
}
