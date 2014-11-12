package org.jbei.ice.lib.entry.sample;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.SampleDAO;
import org.jbei.ice.lib.dao.hibernate.StorageDAO;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryEditor;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.utils.Utils;

/**
 * ABI to manipulate {@link Sample}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class SampleController {

    private final SampleDAO dao;
    private final StorageDAO storageDAO;
    private final StorageController storageController;
    private final EntryAuthorization entryAuthorization;

    public SampleController() {
        dao = DAOFactory.getSampleDAO();
        storageDAO = DAOFactory.getStorageDAO();
        storageController = new StorageController();
        entryAuthorization = new EntryAuthorization();
    }

    public boolean hasSample(Entry entry) {
        return dao.hasSample(entry);
    }

    // mainly used by the api to create a strain sample record
    public Sample createStrainSample(Account account, String recordId, String rack, String location, String barcode,
            String label, String strainNamePrefix) {
        entryAuthorization.expectAdmin(account.getEmail());

        // check if there is an existing sample with barcode
        Storage existing = storageDAO.retrieveStorageTube(barcode.trim());
        if (existing != null) {
            ArrayList<Sample> samples = dao.getSamplesByStorage(existing);
            if (samples != null && !samples.isEmpty()) {
                Logger.error("Barcode \"" + barcode + "\" already has a sample associated with it");
                return null;
            }
        }

        // retrieve entry for record id
        Entry entry = DAOFactory.getEntryDAO().getByRecordId(recordId);
        if (entry == null)
            return null;

        Logger.info("Creating new strain sample [" + rack + ", " + location + ", " + barcode + ", " + label
                            + "] for entry \"" + entry.getId());
        // TODO : this is a hack till we migrate to a single strain default
        Storage strainScheme = null;
        List<Storage> schemes = storageDAO.getAllStorageSchemes();
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
            return null;
        }

        Storage newLocation = storageDAO.getLocation(strainScheme, new String[]{rack, location, barcode});

        Sample sample = SampleCreator.createSampleObject(label, account.getEmail(), "");
        sample.setEntry(entry);
        sample.setStorage(newLocation);
        sample = dao.create(sample);
        String name = entry.getName();
        if (strainNamePrefix != null && name != null && !name.startsWith(strainNamePrefix)) {
            new EntryEditor().updateWithNextStrainName(strainNamePrefix, entry);
        }
        return sample;
    }

    protected Storage createStorage(String userId, String name, SampleType sampleType) {
        Storage storage = new Storage();
        storage.setName(sampleType.name()); // "Tube"(tube), "Well"(well), "Plate"(plate)
        storage.setIndex(name); // barcode(tube), location-A01(well), plateNumber(plate)
//                        result.setParent(parent); // todo : parent is scheme
        Storage.StorageType storageType = Storage.StorageType.valueOf(sampleType.name());
        storage.setStorageType(storageType);
        storage.setOwnerEmail(userId);
        storage.setUuid(Utils.generateUUID());
        return storage;
    }

    public PartSample createSample(String userId, long entryId, PartSample partSample) {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        if (entry == null) {
            Logger.error("Could not retrieve entry with id " + entryId + ". Skipping sample creation");
            return null;
        }

        entryAuthorization.expectWrite(userId, entry);

        Sample sample = SampleCreator.createSampleObject(partSample.getLabel(), userId, "");
        sample.setEntry(entry);

        String depositor = partSample.getDepositor().getEmail();
        StorageLocation mainLocation = partSample.getMain();

        // check and create the storage locations
        if (mainLocation != null) {
            Storage currentStorage = storageDAO.get(mainLocation.getId());
            if (currentStorage == null) {
                currentStorage = createStorage(userId, mainLocation.getDisplay(), mainLocation.getType());
                currentStorage = storageDAO.create(currentStorage);
            }

            while (mainLocation.getChild() != null) {
                StorageLocation child = mainLocation.getChild();
                Storage childStorage = storageDAO.get(child.getId());
                if (childStorage == null) {
                    childStorage = createStorage(depositor, child.getDisplay(), child.getType());
                    childStorage.setParent(currentStorage);
                    childStorage = storageDAO.create(childStorage);
                }

                currentStorage = childStorage;
                mainLocation = child;
            }

            sample.setStorage(currentStorage);
        }

        sample = dao.create(sample);
        return sample.toDataTransferObject();
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
                generic.setMain(location);
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
            partSample.setCreationTime(sample.getCreationTime().getTime());
            partSample.setLabel(sample.getLabel());
            partSample.setMain(storageLocation);
            partSample.setInCart(inCart);

            Account account = DAOFactory.getAccountDAO().getByEmail(sample.getDepositor());
            if (account != null)
                partSample.setDepositor(account.toDataTransferObject());
            else {
                AccountTransfer accountTransfer = new AccountTransfer();
                accountTransfer.setEmail(sample.getDepositor());
                partSample.setDepositor(accountTransfer);
            }

            samples.add(partSample);
        }

        return samples;
    }
}
