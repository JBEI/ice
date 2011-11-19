package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.shared.dto.PlasmidInfo;

public class PlasmidDetailView extends EntryDetailView<PlasmidInfo> {

    public PlasmidDetailView(PlasmidInfo info) {
        super(info);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Strains", "", null);
    }

    @Override
    protected void addLongFields() {
        //        PlasmidInfo info = getInfo();
        addLongField("Backbone", info.getBackbone());
        addLongField("Origin Of Replication", info.getOriginOfReplication());
        addLongField("Selection Markers", info.getSelectionMarkers());
        addLongField("Promoters", info.getPromoters());
    }
}
