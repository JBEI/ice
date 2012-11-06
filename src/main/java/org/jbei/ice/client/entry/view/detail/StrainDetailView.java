package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.shared.dto.StrainInfo;

/**
 * View for displaying strain specific entries
 *
 * @author Hector Plahar
 */

public class StrainDetailView extends EntryDetailView<StrainInfo> {

    public StrainDetailView(StrainInfo info) {
        super(info);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Genotype/Phenotype", info.getGenotypePhenotype());
        addShortField("Host", info.getHost());
        addShortField("Plasmids", info.getLinkifiedPlasmids());
    }

    @Override
    protected void addLongFields() {
        addLongField("Selection Markers", info.getSelectionMarkers());
    }
}
