package org.jbei.ice.client.bulkupload.model;

import java.util.Date;

import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.cell.BooleanSheetCell;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.i18n.client.DateTimeFormat;

public class ArabidopsisSheetModel extends SingleInfoSheetModel<ArabidopsisSeedInfo> {

    @Override
    public ArabidopsisSeedInfo setField(ArabidopsisSeedInfo info, SheetCellData datum) {
        if (datum == null)
            return info;

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null || value.isEmpty())
            return info;

        // arabidopsis seed specific fields
        switch (header) {
            case PLANT_TYPE:
                ArabidopsisSeedInfo.PlantType type = ArabidopsisSeedInfo.PlantType.valueOf(value);
                info.setPlantType(type);
                break;

            case GENERATION:
                ArabidopsisSeedInfo.Generation generation = ArabidopsisSeedInfo.Generation.valueOf(value);
                info.setGeneration(generation);
                break;

            case HARVEST_DATE:
                Date date = DateTimeFormat.getFormat("MM/dd/yyyy").parse(value);
                info.setHarvestDate(date);
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
