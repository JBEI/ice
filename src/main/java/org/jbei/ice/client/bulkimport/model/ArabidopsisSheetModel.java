package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;

import com.google.gwt.user.client.Window;

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
                Window.alert("Harvest date not implemented: ArabidopsisSheetModel.java:33");
                // Need a string to date util
//                  info.setHarvestDate();
                break;

            case PARENTS:
                info.setParents(value);
                break;

            case ECOTYPE:
                info.setEcotype(value);
                break;
        }

        return info;
    }

    @Override
    public ArabidopsisSeedInfo createInfo() {
        return new ArabidopsisSeedInfo();
    }
}
