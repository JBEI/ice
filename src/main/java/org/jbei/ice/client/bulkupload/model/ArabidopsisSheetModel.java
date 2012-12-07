package org.jbei.ice.client.bulkupload.model;

import java.util.Date;

import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.entry.ArabidopsisSeedInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ArabidopsisSheetModel extends SingleInfoSheetModel<ArabidopsisSeedInfo> {

    @Override
    public ArabidopsisSeedInfo setField(ArabidopsisSeedInfo info, SheetCellData datum) {
        if (datum == null)
            return info;

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null)
            return info;

        // arabidopsis seed specific fields
        switch (header) {
            case PLANT_TYPE:
                if (value.isEmpty())
                    break;

                try {
                    ArabidopsisSeedInfo.PlantType type = ArabidopsisSeedInfo.PlantType.valueOf(value);
                    info.setPlantType(type);
                } catch (IllegalArgumentException iae) {
                    GWT.log(iae.getMessage());
                }
                break;

            case GENERATION:
                if (value.isEmpty())
                    break;

                try {
                    ArabidopsisSeedInfo.Generation generation = ArabidopsisSeedInfo.Generation.valueOf(value);
                    info.setGeneration(generation);
                } catch (IllegalArgumentException iae) {
                    GWT.log(iae.getMessage());
                }
                break;

            case HARVEST_DATE:
                if (value.isEmpty())
                    break;

                try {
                    Date date = DateTimeFormat.getFormat("MM/dd/yyyy").parse(value);
                    info.setHarvestDate(date);
                } catch (IllegalArgumentException ia) {
                    GWT.log("Could not parse date " + value);
                }
                break;

            case PARENTS:
                info.setParents(value);
                break;

            case ECOTYPE:
                info.setEcotype(value);
                break;

            case SENT_TO_ABRC:
                info.setSentToAbrc(BooleanSheetCell.getBooleanValue(value));
                break;

            case SAMPLE_DRAWER: {
                SampleStorage sampleStorage = info.getOneSampleStorage();
                for (StorageInfo storageInfo : sampleStorage.getStorageList()) {
                    if (storageInfo.getType().equalsIgnoreCase("shelf")) {
                        storageInfo.setDisplay(value);
                        return info;
                    }
                }
                StorageInfo storageInfo = new StorageInfo();
                storageInfo.setType("shelf");
                storageInfo.setDisplay(value);
                sampleStorage.getStorageList().add(storageInfo);
                break;
            }

            case SAMPLE_BOX: {
                SampleStorage sampleStorage = info.getOneSampleStorage();
                for (StorageInfo storageInfo : sampleStorage.getStorageList()) {
                    if (storageInfo.getType().equalsIgnoreCase("box_unindexed")) {
                        storageInfo.setDisplay(value);
                        return info;
                    }
                }

                StorageInfo storageInfo = new StorageInfo();
                storageInfo.setType("box_unindexed");
                storageInfo.setDisplay(value);
                sampleStorage.getStorageList().add(storageInfo);
                break;
            }

            case SAMPLE_TUBE:
                SampleStorage sampleStorage = info.getOneSampleStorage();
                for (StorageInfo storageInfo : sampleStorage.getStorageList()) {
                    if (storageInfo.getType().equalsIgnoreCase("tube")) {
                        storageInfo.setDisplay(value);
                        return info;
                    }
                }
                StorageInfo storageInfo = new StorageInfo();
                storageInfo.setType("tube");
                storageInfo.setDisplay(value);
                sampleStorage.getStorageList().add(storageInfo);
                break;

            // TODO : abrc
        }

        return info;
    }

    @Override
    public ArabidopsisSeedInfo createInfo() {
        return new ArabidopsisSeedInfo();
    }
}
