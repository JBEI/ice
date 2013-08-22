package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.lib.shared.dto.entry.StrainData;

/**
 * View for displaying strain specific entries
 *
 * @author Hector Plahar
 */

public class StrainDataView extends EntryDataView<StrainData> {

    public StrainDataView(StrainData data) {
        super(data);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Genotype/Phenotype", info.getGenotypePhenotype());
        addShortField("Host", info.getHost());
        addShortField("Plasmids", new LinkedEntriesWidget(info.getLinkedParts()));
    }

    @Override
    protected void addLongFields() {
        addLongField("Selection Markers", info.getSelectionMarkers());
    }
}
