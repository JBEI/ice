package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;

public class ArabidopsisDetailView extends EntryDetailView<ArabidopsisSeedInfo> {

    public ArabidopsisDetailView(ArabidopsisSeedInfo info) {
        super(info);
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
    }

    @Override
    protected void addLongFields() {
    }
}
