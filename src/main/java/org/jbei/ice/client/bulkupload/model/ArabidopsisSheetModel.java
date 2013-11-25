package org.jbei.ice.client.bulkupload.model;

import java.util.Date;

import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.lib.shared.dto.StorageInfo;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.Generation;
import org.jbei.ice.lib.shared.dto.entry.PlantType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ArabidopsisSheetModel extends SingleInfoSheetModel<ArabidopsisSeedData> {

    @Override
    public ArabidopsisSeedData setField(ArabidopsisSeedData data, SheetCellData datum) {
        if (datum == null)
            return data;

        EntryField header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null)
            return data;

        // arabidopsis seed specific fields
        switch (header) {
            case PLANT_TYPE:
                if (value.isEmpty())
                    break;

                PlantType type = PlantType.fromString(value);
                data.setPlantType(type);
                break;

            case GENERATION:
                if (value.isEmpty())
                    break;

                Generation generation = Generation.fromString(value);
                data.setGeneration(generation);
                break;

            case HARVEST_DATE:
                if (value.isEmpty())
                    break;

                try {
                    Date date = DateTimeFormat.getFormat("MM/dd/yyyy").parse(value);
                    data.setHarvestDate(date);
                } catch (IllegalArgumentException ia) {
                    GWT.log("Could not parse date " + value);
                }
                break;

            case PARENTS:
                data.setParents(value);
                break;

            case ECOTYPE:
                data.setEcotype(value);
                break;

            case SENT_TO_ABRC:
                data.setSentToAbrc(BooleanSheetCell.getBooleanValue(value));
                break;

            case SAMPLE_DRAWER: {
                SampleStorage sampleStorage = data.getOneSampleStorage();
                for (StorageInfo storageInfo : sampleStorage.getStorageList()) {
                    if (storageInfo.getType().equalsIgnoreCase("shelf")) {
                        storageInfo.setDisplay(value);
                        return data;
                    }
                }
                StorageInfo storageInfo = new StorageInfo();
                storageInfo.setType("shelf");
                storageInfo.setDisplay(value);
                sampleStorage.getStorageList().add(storageInfo);
                break;
            }

            case SAMPLE_BOX: {
                SampleStorage sampleStorage = data.getOneSampleStorage();
                for (StorageInfo storageInfo : sampleStorage.getStorageList()) {
                    if (storageInfo.getType().equalsIgnoreCase("box_unindexed")) {
                        storageInfo.setDisplay(value);
                        return data;
                    }
                }

                StorageInfo storageInfo = new StorageInfo();
                storageInfo.setType("box_unindexed");
                storageInfo.setDisplay(value);
                sampleStorage.getStorageList().add(storageInfo);
                break;
            }

            case SAMPLE_TUBE:
                SampleStorage sampleStorage = data.getOneSampleStorage();
                for (StorageInfo storageInfo : sampleStorage.getStorageList()) {
                    if (storageInfo.getType().equalsIgnoreCase("tube")) {
                        storageInfo.setDisplay(value);
                        return data;
                    }
                }
                StorageInfo storageInfo = new StorageInfo();
                storageInfo.setType("tube");
                storageInfo.setDisplay(value);
                sampleStorage.getStorageList().add(storageInfo);
                break;

            // TODO : abrc
        }

        return data;
    }

    @Override
    public ArabidopsisSeedData createInfo() {
        return new ArabidopsisSeedData();
    }
}
