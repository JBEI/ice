package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;

public class ArabidopsisDataView extends EntryDataView<ArabidopsisSeedData> {

    public ArabidopsisDataView(ArabidopsisSeedData data) {
        super(data);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Plant Type", info.getPlantType().toString());
        addShortField("Generation", info.getGeneration().toString());
        addShortField("Homozygosity", info.getHomozygosity());
        addShortField("Ecotype", info.getEcotype());
        String harvestDate = DateUtilities.formatDate(info.getHarvestDate());
        addShortField("Harvested", harvestDate);
        addShortField("Parents", info.getParents());
        String sentValue = info.isSentToAbrc() ? "Yes" : "No";
        addShortField("Sent To ABRC", sentValue);
    }

    @Override
    protected void addLongFields() {
        addLongField("Selection Markers", info.getSelectionMarkers());
    }
}
