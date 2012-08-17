package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.Date;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * @author Hector Plahar
 */
public class ArabidopsisSeedHeaders extends PartHeader {

    @Override
    public SheetCellData extractValue(Header header, EntryInfo info) {
        SheetCellData data = extractCommon(header, info);
        if (data != null)
            return data;

        ArabidopsisSeedInfo seed = (ArabidopsisSeedInfo) info;
        String value = null;

        switch (header) {

            case HOMOZYGOSITY:
                value = seed.getHomozygosity();
                break;

            case ECOTYPE:
                value = seed.getEcotype();
                break;

            case HARVEST_DATE:
                Date harvestDate = seed.getHarvestDate();
                if (harvestDate == null)
                    value = "";
                else
                    value = DateUtilities.formatDate(harvestDate);
                break;

            case PARENTS:
                value = seed.getParents();
                break;

            case GENERATION:
                ArabidopsisSeedInfo.Generation generation = seed.getGeneration();
                if (generation == null)
                    value = "";
                else
                    value = seed.getGeneration().name();
                break;

            case PLANT_TYPE:
                ArabidopsisSeedInfo.PlantType plantType = seed.getPlantType();
                if (plantType == null)
                    value = "";
                else
                    value = seed.getPlantType().toString();
                break;
        }

        if (value == null)
            return null;

        data = new SheetCellData();
        data.setValue(value);
        data.setId(value);
        return data;
    }
}
