package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SampleDAO;
import org.jbei.ice.storage.hibernate.dao.StorageDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for dealing with {@link Sample}s
 *
 * @author Hector Plahar
 */
public class SampleService extends HasEntry {

    private final SampleDAO dao;
    private final StorageDAO storageDAO;
    private final AccountDAO accountDAO;
    private final EntryDAO entryDAO;
    private final EntryAuthorization entryAuthorization;
    private final SampleAuthorization sampleAuthorization;

    public SampleService() {
        dao = DAOFactory.getSampleDAO();
        accountDAO = DAOFactory.getAccountDAO();
        storageDAO = DAOFactory.getStorageDAO();
        entryDAO = DAOFactory.getEntryDAO();
        entryAuthorization = new EntryAuthorization();
        sampleAuthorization = new SampleAuthorization();
    }

    Storage createStorageObject(String userId, String name, SampleType sampleType) {
        Storage storage = new Storage();
        storage.setName(sampleType.name());
        storage.setIndex(name);
        Storage.StorageType storageType = Storage.StorageType.valueOf(sampleType.name());
        storage.setStorageType(storageType);
        storage.setOwnerEmail(userId);
        storage.setUuid(Utils.generateUUID());
        return storage;
    }

    public PartSample createSample(String userId, String entryId, PartSample partSample, String strainNamePrefix) {
        Entry entry = super.getEntry(entryId);
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
                    currentStorage = createStorageObject(depositor, mainLocation.getDisplay(), mainLocation.getType());
                    currentStorage = storageDAO.create(currentStorage);
                    break;

                case GENSCRIPT:
                    currentStorage = createStorageObject(depositor, mainLocation.getDisplay(), mainLocation.getType());
                    currentStorage.setName(mainLocation.getName());
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
                        currentStorage = createStorageObject(userId, mainLocation.getDisplay(), mainLocation.getType());
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
            entryDAO.generateNextStrainNameForEntry(entry, strainNamePrefix);
        }
        return sample.toDataTransferObject();
    }

    /**
     * Creates location records for a sample contained in a 96 well plate
     * Provides support for 2-D barcoded systems. Validates the storage hierarchy before creating.
     *
     * @param sampleDepositor userID - unique identifier for user performing action
     * @param mainLocation    96 well plate location
     * @return sample storage with a complete hierarchy or null
     */
    private Storage createPlate96Location(String sampleDepositor, StorageLocation mainLocation) {
        // validate: expected format is [PLATE96, WELL, (optional - TUBE)]
        StorageLocation well = mainLocation.getChild();
        StorageLocation tube;
        if (well != null) {
            tube = well.getChild();
            if (tube != null) {
                // just check the barcode
                String barcode = tube.getDisplay();
                List<Storage> existing = storageDAO.retrieveStorageTube(barcode);
                if (existing != null && !existing.isEmpty()) {
                    List<Sample> samples = new ArrayList<>();
                    for (Storage storage : existing) {
                        samples.addAll(dao.getSamplesByStorage(storage));
                    }

                    if (!samples.isEmpty()) {
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
        } else {
            currentStorage = createStorageObject(sampleDepositor, mainLocation.getDisplay(), mainLocation.getType());
            currentStorage = storageDAO.create(currentStorage);
        }

        currentStorage = createChildrenStorage(mainLocation, currentStorage, sampleDepositor);

        return currentStorage;
    }

    private Storage createShelfStorage(String depositor, StorageLocation shelf) {
        // expecting [SHELF, BOX, WELL, TUBE]. ultimately the children of the main location

        // should contain type and therefore allow for general hierarchy and more intelligence
        // where it checks if the location is already taken

        // create storage locations
        Storage currentStorage = createStorageObject(depositor, shelf.getDisplay(), shelf.getType());
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
    private Storage createChildrenStorage(StorageLocation currentLocation, Storage currentStorage, String depositor) {
        while (currentLocation.getChild() != null) {
            StorageLocation child = currentLocation.getChild();
            Storage childStorage = storageDAO.get(child.getId());
            if (childStorage == null) {
                childStorage = createStorageObject(depositor, child.getDisplay(), child.getType());
                childStorage.setParent(currentStorage);
                childStorage = storageDAO.create(childStorage);
            }

            currentStorage = childStorage;
            currentLocation = child;
        }

        return currentStorage;
    }

    /**
     * Retrieves the available samples for specified entry
     *
     * @param userId  identifier for user making request
     * @param entryId identifier for entry whose samples are being retrieved
     * @return list of found samples for the specified entry (including the hierarchy of the locations if applicable from the top down)
     * @throws org.jbei.ice.lib.access.PermissionException if the specified user doesn't have read privileges
     *                                                     on the specified entry
     */
    public List<PartSample> retrieveEntrySamples(String userId, String entryId) {
        Entry entry = super.getEntry(entryId);
        if (entry == null)
            return null;

        entryAuthorization.expectRead(userId, entry);

        // samples
        List<Sample> entrySamples = dao.getSamplesByEntry(entry);
        ArrayList<PartSample> samples = new ArrayList<>();
        if (entrySamples == null || entrySamples.isEmpty())
            return samples;

        // check if the sample is in the requesting user's cart
        boolean inCart = false;
        if (userId != null) {
            Account userAccount = accountDAO.getByEmail(userId);
            inCart = DAOFactory.getRequestDAO().getSampleRequestInCart(userAccount, entry) != null;
        }

        // convert sample to info
        for (Sample sample : entrySamples) {
            Storage storage = sample.getStorage();

            if (storage == null) {
                // dealing with sample with no storage so set generic storage
                PartSample generic = sample.toDataTransferObject();
                StorageLocation location = new StorageLocation();
                location.setType(SampleType.GENERIC);
                location.setDisplay(sample.getLabel());
                generic.setLocation(location);
                setAccountInfo(generic, sample.getDepositor());
                generic.setCanEdit(sampleAuthorization.canWrite(userId, sample));
                samples.add(generic);
                continue;
            }

            StorageLocation storageLocation = storage.toDataTransferObject();

            // storage starts at the leaf level (e.g. tube or well for 96 well plate) so
            // walk up parents to get to top level for sample location
            while (storage.getParent() != null) {
                storage = storage.getParent();
                StorageLocation parentLocation = storage.toDataTransferObject();
                parentLocation.setChild(storageLocation);
                storageLocation = parentLocation;

                if (storageLocation.getType() != null && storageLocation.getType().isTopLevel())
                    break;
            }

            // get specific sample type and details about it
            PartSample partSample = new PartSample();
            partSample.setId(sample.getId());
            partSample.setPartId(sample.getEntry().getId());
            partSample.setLocation(storageLocation);
            partSample.setPartName(sample.getEntry().getName());
            partSample.setLabel(sample.getLabel());

            if (sample.getEntry().getId() == entry.getId()) {
                partSample.setCreationTime(sample.getCreationTime().getTime());
                partSample.setInCart(inCart);
                partSample.setCanEdit(sampleAuthorization.canWrite(userId, sample));

                if (sample.getComments() != null) {
                    for (Comment comment : sample.getComments()) {
                        UserComment userComment = new UserComment();
                        userComment.setId(comment.getId());
                        userComment.setMessage(comment.getBody());
                        partSample.getComments().add(userComment);
                    }
                }
            } else {
                partSample.setCanEdit(false);
            }

            setAccountInfo(partSample, sample.getDepositor());
            samples.add(partSample);
        }

        return samples;
    }

    public PlateSamples retrievePlate(String userId, long locationId, SampleType sampleType) {
        Storage storage = storageDAO.get(locationId);
        if (storage == null)
            return null;

        if (!storage.getStorageType().name().equalsIgnoreCase(sampleType.name()))
            return null; // todo : throw an appropriate exception

        PlateSamples plateSamples = new PlateSamples();
        plateSamples.setSample(userId, locationId);
        return plateSamples;
    }

    private void setAccountInfo(PartSample partSample, String email) {
        Account account = accountDAO.getByEmail(email);
        if (account != null)
            partSample.setDepositor(account.toDataTransferObject());
        else {
            AccountTransfer accountTransfer = new AccountTransfer();
            accountTransfer.setEmail(email);
            partSample.setDepositor(accountTransfer);
        }
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
        Storage storage = sample.getStorage();
        dao.delete(sample);

        while (storage != null) {
            Storage parent = storage.getParent();

            if (storage.getChildren().size() == 0) {
                storageDAO.delete(storage);
            } else {
                break;
            }

            if (parent != null) {
                parent.getChildren().remove(storage);
                storage = parent;
            } else {
                break;
            }
        }

        return true;
    }

    public List<PartSample> getSamplesByBarcode(String userId, String barcode) {
        List<Storage> storage = storageDAO.retrieveStorageTube(barcode);
        if (storage == null)
            return null;

        List<PartSample> partSamples = new ArrayList<>();

        if (storage.isEmpty())
            return partSamples;

        List<Sample> samples = new ArrayList<>();
        for (Storage item : storage) {
            List<Sample> result = dao.getSamplesByStorage(item);
            if (result == null)
                continue;

            samples.addAll(result);
        }

        for (Sample sample : samples) {
            Entry entry = sample.getEntry();
            if (entry == null)
                continue;
            Logger.info(entry.getName());
            if (!entryAuthorization.canRead(userId, entry))
                continue;

            partSamples.add(sample.toDataTransferObject());
        }
        return partSamples;
    }

    public List<StorageLocation> getStorageLocations(String userId, SampleType type, int offset, int limit) {
        sampleAuthorization.expectAdmin(userId);
        List<Storage> storages = storageDAO.get(type, offset, limit);
        return storages.stream().map(Storage::toDataTransferObject).collect(Collectors.toList());
    }
}
