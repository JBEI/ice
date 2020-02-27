package org.jbei.ice.lib.entry.sample;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.Plate;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.lib.dto.sample.Tube;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SampleDAO;
import org.jbei.ice.storage.hibernate.dao.StorageDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sample;
import org.jbei.ice.storage.model.Storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 96 Well Plate storage
 */
public class PlateStorage {

    private final StorageDAO dao;
    private final String userId;
    private final HasEntry hasEntry;
    private final SampleDAO sampleDAO;
    private String prefix;

    public PlateStorage(String userId) {
        this.dao = DAOFactory.getStorageDAO();
        this.sampleDAO = DAOFactory.getSampleDAO();
        this.userId = userId;
        this.hasEntry = new HasEntry();
    }

    public void create(Plate plate) {
        // todo : expect user to be an admin
        EntryAuthorization authorization = new EntryAuthorization();
        authorization.expectAdmin(userId);
        List<Storage> parent = dao.retrieveStorageByIndex(plate.getName(), SampleType.PLATE96);

        Storage plateStorage;
        // if no parent create one
        if (parent == null || parent.isEmpty()) {
            plateStorage = new Storage();
            plateStorage.setName(SampleType.PLATE96.name());
            plateStorage.setOwnerEmail(userId);
            plateStorage.setStorageType(Storage.StorageType.PLATE96);
            plateStorage.setUuid(UUID.randomUUID().toString());
            plateStorage.setIndex(plate.getName());
            plateStorage = dao.create(plateStorage);
        } else {
            plateStorage = parent.get(0);
        }

        Map<String, Tube> map = plate.getLocationBarcodes();    // map of [well -> tube]
        for (Map.Entry<String, Tube> entryMap : map.entrySet()) {
            String wellLocation = entryMap.getKey();

            // check tube with barcode doesn't exist
            Tube tube = entryMap.getValue();
            List<Storage> result = dao.retrieveStorageByIndex(tube.getBarcode(), SampleType.TUBE);
            if (result != null && !result.isEmpty()) {
                Logger.error("Tube with barcode " + tube.getBarcode() + " already exists. Skipping");
                continue;
            }

            // check that part Id is available
            Entry entry = hasEntry.getEntry(tube.getPartId());
            if (entry == null) {
                Logger.error("No part found for entryId " + tube.getPartId() + ". Skipping sample creation");
                continue;
            }

            // get well
            Storage well = dao.getPlateWell(wellLocation, plateStorage.getId());
            if (well == null) {
                well = new Storage();
                well.setIndex(wellLocation);
                well.setStorageType(Storage.StorageType.WELL);
                well.setName(Storage.StorageType.WELL.name());
                well.setOwnerEmail(userId);
                well.setUuid(UUID.randomUUID().toString());
                well.setParent(plateStorage);
                well = dao.create(well);
            } else {
                if (!well.getChildren().isEmpty()) {
                    Logger.error("Well " + wellLocation + " already has a tube. Skipping");
                    continue;
                }
            }

            // create tube
            Storage tubeStorage = new Storage();
            tubeStorage.setParent(well);
            tubeStorage.setUuid(UUID.randomUUID().toString());
            tubeStorage.setOwnerEmail(userId);
            tubeStorage.setStorageType(Storage.StorageType.TUBE);
            tubeStorage.setName(Storage.StorageType.TUBE.name());
            tubeStorage.setIndex(tube.getBarcode());
            tubeStorage = dao.create(tubeStorage);

            // create sample
            createEntrySample(entry, tubeStorage);
        }
    }

    public void setStrainNamePrefix(String prefix) {
        this.prefix = prefix;
    }

    private void createEntrySample(Entry entry, Storage tube) {
        // determine label; use number of existing samples
        int sampleCount = sampleDAO.getSampleCount(entry);
        String label = entry.getPartNumber();
        if (sampleCount == 0)
            label += " working copy";
        else
            label += " backup " + sampleCount;

        Sample sample = SampleCreator.createSampleObject(label, userId, "");
        sample.setEntry(entry);
        sample.setStorage(tube);

        // create sample. If main location is null then sample is created without location
        sampleDAO.create(sample);

        String name = entry.getName();
        if (StringUtils.isNotBlank(prefix) && name != null && !name.startsWith(prefix)) {
            DAOFactory.getEntryDAO().generateNextStrainNameForEntry(entry, prefix);
        }
    }
}
