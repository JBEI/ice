package org.jbei.ice.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.SamplePermissionVerifier;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Utils;

public class SampleController extends Controller {
    public SampleController(Account account) {
        super(account, new SamplePermissionVerifier());
    }

    public Sample createSample(String label, String depositor, String notes) {
        return createSample(label, depositor, notes, Utils.generateUUID(), Calendar.getInstance()
                .getTime(), null);
    }

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

    public boolean hasReadPermission(Sample sample) throws ControllerException {
        if (sample == null) {
            throw new ControllerException("Failed to check read permissions for null sample!");
        }

        return getSamplePermissionVerifier().hasReadPermissions(sample, getAccount());
    }

    public boolean hasWritePermission(Sample sample) throws ControllerException {
        if (sample == null) {
            throw new ControllerException("Failed to check write permissions for null sample!");
        }

        return getSamplePermissionVerifier().hasWritePermissions(sample, getAccount());
    }

    public Sample saveSample(Sample sample) throws ControllerException, PermissionException {
        return saveSample(sample, true);
    }

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

    public void deleteSample(Sample sample) throws ControllerException, PermissionException {
        deleteSample(sample, true);
    }

    public void deleteSample(Sample sample, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(sample)) {
            throw new PermissionException("No permissions to delete sample!");
        }

        try {
            SampleManager.deleteSample(sample);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

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

    public ArrayList<Sample> getSamples(Entry entry) throws ControllerException {
        ArrayList<Sample> samples = null;

        try {
            samples = SampleManager.getSamplesByEntry(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return samples;
    }

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

    public ArrayList<Sample> getSamplesByStorage(Storage storage) throws ControllerException {

        try {
            return SampleManager.getSamplesByStorage(storage);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public int getNumberOfSamplesByDepositor(String depositorEmail) throws ControllerException {
        int numberOfSamples = 0;

        try {
            ArrayList<Sample> samples = SampleManager.getSamplesByDepositor(depositorEmail, 0, -1);

            if (samples != null) {
                numberOfSamples = samples.size();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return numberOfSamples;
    }

    protected SamplePermissionVerifier getSamplePermissionVerifier() {
        return (SamplePermissionVerifier) getPermissionVerifier();
    }
}
