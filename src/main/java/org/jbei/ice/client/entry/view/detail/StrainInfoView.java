package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.lib.shared.dto.entry.StrainInfo;

/**
 * View for displaying strain specific entries
 *
 * @author Hector Plahar
 */

public class StrainInfoView extends EntryInfoView<StrainInfo> {

    public StrainInfoView(StrainInfo info) {
        super(info);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Genotype/Phenotype", info.getGenotypePhenotype());
        addShortField("Host", info.getLinkifiedHost());
        addShortField("Plasmids", info.getLinkifiedPlasmids());
    }

    @Override
    protected void addLongFields() {
        addLongField("Selection Markers", info.getSelectionMarkers());
    }
}
