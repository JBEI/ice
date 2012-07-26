package org.jbei.ice.client.bulkupload.sheet;

import java.util.Date;

import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.PlantType;
import org.jbei.ice.shared.dto.EntryInfo;

public class ArabidopsisExtractor extends InfoValueExtractor {

    public String extractValue(Header header, EntryInfo info, int index) {
        ArabidopsisSeedInfo seed = (ArabidopsisSeedInfo) info;
        switch (header) {

            case HOMOZYGOSITY:
                return seed.getHomozygosity();

            case ECOTYPE:
                return seed.getEcotype();

            case HARVEST_DATE:
                Date harvestDate = seed.getHarvestDate();
                if (harvestDate == null)
                    return "";
                return DateUtilities.formatDate(harvestDate);

            case PARENTS:
                return seed.getParents();

            case GENERATION:
                Generation generation = seed.getGeneration();
                if (generation == null)
                    return "";
                return seed.getGeneration().name();

            case PLANT_TYPE:
                PlantType plantType = seed.getPlantType();
                if (plantType == null)
                    return "";
                return seed.getPlantType().toString();
        }
        return null;
    }

}
