package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SampleDAO;
import org.jbei.ice.storage.hibernate.dao.StorageDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for dealing with {@link Sample}s
 *
 * @author Hector Plahar
 */
public class SampleService {

    private final SampleDAO dao;
    private final StorageDAO storageDAO;
    private final EntryAuthorization entryAuthorization;
    private final SampleAuthorization sampleAuthorization;

    public SampleService() {
        dao = DAOFactory.getSampleDAO();
        storageDAO = DAOFactory.getStorageDAO();
        entryAuthorization = new EntryAuthorization();
        sampleAuthorization = new SampleAuthorization();
    }

    protected Storage createStorage(String userId, String name, SampleType sampleType) {
        Storage storage = new Storage();
        storage.setName(sampleType.name());
        storage.setIndex(name);
        Storage.StorageType storageType = Storage.StorageType.valueOf(sampleType.name());
        storage.setStorageType(storageType);
        storage.setOwnerEmail(userId);
        storage.setUuid(Utils.generateUUID());
        return storage;
    }

    public PartSample createSample(String userId, long entryId, PartSample partSample, String strainNamePrefix) {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        if (entry == null) {
            Logger.error("Could not retrieve entry with id " + entryId + ". Skipping sample creation");
            return null;
        }

        entryAuthorization.expectWrite(userId, entry);

        Sample sample = SampleCreator.createSampleObject(partSample.getLabel(), userId, "");
        sample.setEntry(entry);

        String depositor;
        if (partSample.getDepositor() == null) {
            depositor = userId;
        } else {
            depositor = partSample.getDepositor().getEmail();
        }
        StorageLocation mainLocation = partSample.getLocation();

        // check and create the storage locations
        if (mainLocation != null) {
            Storage currentStorage;
            switch (mainLocation.getType()) {
                case ADDGENE:
                    currentStorage = createStorage(depositor, mainLocation.getDisplay(), mainLocation.getType());
                    currentStorage = storageDAO.create(currentStorage);
                    break;

                case PLATE96:
                    currentStorage = createPlate96Location(depositor, mainLocation);
                    break;

                case SHELF:
                    currentStorage = createShelfStorage(depositor, mainLocation);
                    break;

                default:
                    currentStorage = storageDAO.get(mainLocation.getId());
                    if (currentStorage == null) {
                        currentStorage = createStorage(userId, mainLocation.getDisplay(), mainLocation.getType());
                        currentStorage = storageDAO.create(currentStorage);
                    }

                    currentStorage = createChildrenStorage(mainLocation, currentStorage, depositor);
            }
            if (currentStorage == null)
                return null;

            sample.setStorage(currentStorage);
        }

        // create sample. If main location is null then sample is created without location
        sample = dao.create(sample);
        String name = entry.getName();
        if (strainNamePrefix != null && name != null && !name.startsWith(strainNamePrefix)) {
            DAOFactory.getEntryDAO().generateNextStrainNameForEntry(entry, strainNamePrefix);
        }
        return sample.toDataTransferObject();
    }

    /**
     * Creates location records for a sample contained in a 96 well plate
     * Provides support for 2-D barcoded systems. Validates the storage hierarchy before creating.
     *
     * @param sampleDepositor userID - unique identifier for user performing action
     * @param mainLocation    96 well plate location
     * @return sample storage with a complete hierachy or null
     */
    protected Storage createPlate96Location(String sampleDepositor, StorageLocation mainLocation) {
        // validate: expected format is [PLATE96, WELL, (optional - TUBE)]
        StorageLocation well = mainLocation.getChild();
        StorageLocation tube;
        if (well != null) {
            tube = well.getChild();
            if (tube != null) {
                // just check the barcode
                String barcode = tube.getDisplay();
                Storage existing = storageDAO.retrieveStorageTube(barcode);
                if (existing != null) {
                    ArrayList<Sample> samples = dao.getSamplesByStorage(existing);
                    if (samples != null && !samples.isEmpty()) {
                        Logger.error("Barcode \"" + barcode + "\" already has a sample associated with it");
                        return null;
                    }
                }
            }
        } else {
            return null;
        }

        if (tube == null) {
            return null;
        }

        // create storage locations
        Storage currentStorage;
        List<Storage> storageList = storageDAO.retrieveStorageByIndex(mainLocation.getDisplay(), SampleType.PLATE96);
        if (storageList != null && storageList.size() > 0) {
            currentStorage = storageList.get(0);

            Set<Storage> wells = currentStorage.getChildren(); // check if there is a sample in that well
            for (Storage thisWell : wells) {
                if (thisWell.getIndex().equals(well.getDisplay()) && thisWell.getChildren() != null) {
                    Logger.error("Plate " + mainLocation.getDisplay()
                            + " already has a well storage at " + well.getDisplay());
                    return null;
                }
            }
        }  else {
            currentStorage = createStorage(sampleDepositor, mainLocation.getDisplay(), mainLocation.getType());
            currentStorage = storageDAO.create(currentStorage);
        }

        currentStorage = createChildrenStorage(mainLocation, currentStorage, sampleDepositor);

        return currentStorage;
    }

    protected Storage createShelfStorage(String depositor, StorageLocation shelf) {
        // expecting [SHELF, BOX, WELL, TUBE]. ultimately the children of the main location
        try {
            StorageLocation box = shelf.getChild();
            StorageLocation well = box.getChild();
            well.getChild();
        } catch (Exception e) {
            return null;
        }

        // should contain type and therefore allow for general hierarchy and more intelligence
        // where it checks if the location is already taken

        // create storage locations
        Storage currentStorage = createStorage(depositor, shelf.getDisplay(), shelf.getType());

        currentStorage = createChildrenStorage(shelf, storageDAO.create(currentStorage), depositor);

        return currentStorage;
    }

    /**
     * Creates storage for all children of given parent storage
     *
     * @param currentLocation storage location
     * @param currentStorage
     * @param depositor       userID - unique identifier for user performing action
     * @return updated storage
     */
    protected Storage createChildrenStorage(StorageLocation currentLocation, Storage currentStorage, String depositor) {
        while (currentLocation.getChild() != null) {
            StorageLocation child = currentLocation.getChild();
            Storage childStorage = storageDAO.get(child.getId());
            if (childStorage == null) {
                childStorage = createStorage(depositor, child.getDisplay(), child.getType());
                childStorage.setParent(currentStorage);
                childStorage = storageDAO.create(childStorage);
            }

            currentStorage = childStorage;
            currentLocation = child;
        }

        return currentStorage;
    }


    public ArrayList<PartSample> retrieveEntrySamples(String userId, long entryId) {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        if (entry == null)
            return null;

        entryAuthorization.expectRead(userId, entry);

        // samples
        ArrayList<Sample> entrySamples = dao.getSamplesByEntry(entry);
        ArrayList<PartSample> samples = new ArrayList<>();
        if (entrySamples == null)
            return samples;

        boolean inCart = false;
        if (userId != null) {
            Account userAccount = DAOFactory.getAccountDAO().getByEmail(userId);
            inCart = DAOFactory.getRequestDAO().getSampleRequestInCart(userAccount, entry) != null;
        }

        for (Sample sample : entrySamples) {
            // convert sample to info
            Storage storage = sample.getStorage();
            if (storage == null) {
                // dealing with sample with no storage
                PartSample generic = sample.toDataTransferObject();
                StorageLocation location = new StorageLocation();
                location.setType(SampleType.GENERIC);
                location.setDisplay(sample.getLabel());
                generic.setLocation(location);
                generic = setAccountInfo(generic, sample.getDepositor());
                samples.add(generic);
                continue;
            }

            StorageLocation storageLocation = storage.toDataTransferObject();

            while (storage.getParent() != null) {
                storage = storage.getParent();
                StorageLocation parentLocation = storage.toDataTransferObject();
                parentLocation.setChild(storageLocation);
                storageLocation = parentLocation;

                boolean isParent = (storageLocation.getType() != null && storageLocation.getType().isTopLevel());
                if (isParent)
                    break;
            }

            // get specific sample type and details about it
            PartSample partSample = new PartSample();
            partSample.setId(sample.getId());
            partSample.setCreationTime(sample.getCreationTime().getTime());
            partSample.setLabel(sample.getLabel());
            partSample.setLocation(storageLocation);
            partSample.setInCart(inCart);
            partSample = setAccountInfo(partSample, sample.getDepositor());
            partSample.setCanEdit(sampleAuthorization.canWrite(userId, sample));

            if (sample.getComments() != null) {
                for (Comment comment : sample.getComments()) {
                    UserComment userComment = new UserComment();
                    userComment.setId(comment.getId());
                    userComment.setMessage(comment.getBody());
                    partSample.getComments().add(userComment);
                }
            }

            samples.add(partSample);
        }

        return samples;
    }

    protected PartSample setAccountInfo(PartSample partSample, String email) {
        Account account = DAOFactory.getAccountDAO().getByEmail(email);
        if (account != null)
            partSample.setDepositor(account.toDataTransferObject());
        else {
            AccountTransfer accountTransfer = new AccountTransfer();
            accountTransfer.setEmail(email);
            partSample.setDepositor(accountTransfer);
        }
        return partSample;
    }

    /**
     * Deletes specified sample for entry and all associated storage locations
     *
     * @param userId   unique identifier for user performing action
     * @param partId   unique identifier for part that is associated with this sample
     * @param sampleId unique identifier for sample being deleted
     * @return true is deletion successful, false otherwise
     */
    public boolean delete(String userId, long partId, long sampleId) {
        Sample sample = dao.get(sampleId);
        if (sample == null)
            return true;

        Entry entry = sample.getEntry();
        if (entry == null || partId != entry.getId())
            return false;

        sampleAuthorization.expectWrite(userId, sample);

        try {
            Storage storage = sample.getStorage();
            while (storage != null) {
                Storage parent = storage.getParent();

                if (storage.getChildren().size() == 0) {
                    DAOFactory.getStorageDAO().delete(storage);
                }

                if (parent != null) {
                    parent.getChildren().remove(storage);
                    storage = parent;
                } else {
                    break;
                }
            }

            sample.setStorage(null);
            dao.delete(sample);
            return true;
        } catch (DAOException de) {
            return false;
        }
    }

    public List<StorageLocation> getStorageLocations(String userId, String entryType) {
        List<Storage> storages = DAOFactory.getStorageDAO().getAllStorageSchemes();
        ArrayList<StorageLocation> locations = new ArrayList<>();
        for (Storage storage : storages) {
            locations.add(storage.toDataTransferObject());
        }

        return locations;
    }

    public ArrayList<PartSample> getSamplesByBarcode(String userId, String barcode) {
        Storage storage = storageDAO.retrieveStorageTube(barcode);
        if (storage == null)
            return null;

        List<Sample> samples = dao.getSamplesByStorage(storage);
        ArrayList<PartSample> partSamples = new ArrayList<>();
        for (Sample sample : samples) {
            Entry entry = sample.getEntry();
            if (entry == null)
                continue;

            if (!entryAuthorization.canRead(userId, entry))
                continue;

            partSamples.add(sample.toDataTransferObject());
        }
        return partSamples;
    }
}
