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
import org.jbei.ice.lib.models.Location;
import org.jbei.ice.lib.models.Sample;
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

    public Location createLocation(String location, String barcode, String notes, String wells,
            int nRows, int nColumns) {
        return createLocation(location, barcode, notes, wells, nRows, nColumns, Calendar
                .getInstance().getTime(), null);
    }

    public Location createLocation(String location, String barcode, String notes, String wells,
            int nRows, int nColumns, Date creationTime, Date modificationTime) {
        Location sampleLocation = new Location();

        sampleLocation.setLocation(location);
        sampleLocation.setBarcode(barcode);
        sampleLocation.setNotes(notes);
        sampleLocation.setWells(wells);
        sampleLocation.setnRows(nRows);
        sampleLocation.setnColumns(nColumns);
        sampleLocation.setCreationTime(creationTime);
        sampleLocation.setModificationTime(modificationTime);

        return sampleLocation;
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

    public boolean hasLocationReadPermission(Location location) throws ControllerException {
        if (location == null) {
            throw new ControllerException("Failed to check read permissions for null location!");
        }

        return getSamplePermissionVerifier().hasReadPermissions(location, getAccount());
    }

    public boolean hasLocationWritePermission(Location location) throws ControllerException {
        if (location == null) {
            throw new ControllerException("Failed to check write permissions for null location!");
        }

        return getSamplePermissionVerifier().hasWritePermissions(location, getAccount());
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

    public int getNumberOfSamples(Entry entry) throws ControllerException {
        int result = 0;
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

    public Location saveLocation(Location location) throws ControllerException, PermissionException {
        return saveLocation(location, true);
    }

    public Location saveLocation(Location location, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (!hasLocationWritePermission(location)) {
            throw new PermissionException("No permissions to save location!");
        }

        try {
            Sample sample = location.getSample();

            sample.getLocations().add(location);

            sample = SampleManager.saveSample(sample);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return location;
    }

    public void deleteLocation(Location location) throws ControllerException, PermissionException {
        deleteLocation(location, true);
    }

    public void deleteLocation(Location location, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (!hasLocationWritePermission(location)) {
            throw new PermissionException("No permissions to delete location!");
        }

        try {
            Sample sample = location.getSample();

            sample.getLocations().remove(location);

            SampleManager.saveSample(sample);

            if (scheduleIndexRebuild) {
                ApplicationContoller.scheduleSearchIndexRebuildJob();
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    protected SamplePermissionVerifier getSamplePermissionVerifier() {
        return (SamplePermissionVerifier) getPermissionVerifier();
    }
}
