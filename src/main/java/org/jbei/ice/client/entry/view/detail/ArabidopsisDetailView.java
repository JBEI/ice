package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;

public class ArabidopsisDetailView extends EntryDetailView<ArabidopsisSeedInfo> {

    public ArabidopsisDetailView(ArabidopsisSeedInfo info) {
        super(info);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Plant Type", info.getPlantType().toString(), ValueType.SHORT_TEXT);
        addShortField("Generation", info.getGeneration().toString(), ValueType.SHORT_TEXT);
        addShortField("Homozygosity", info.getHomozygosity(), ValueType.SHORT_TEXT);
        addShortField("Ecotype", info.getEcotype(), ValueType.SHORT_TEXT);
        addShortField("Harvested", info.getHarvestDate() + "", ValueType.SHORT_TEXT);
        addShortField("Parents", info.getParents(), ValueType.SHORT_TEXT);
    }

    @Override
    protected void addLongFields() {
    }
}
