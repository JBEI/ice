package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.lib.shared.dto.entry.PlasmidData;

/**
 * Details view for entries of type plasmid
 *
 * @author Hector Plahar
 */

public class PlasmidDataView extends EntryDataView<PlasmidData> {

    public PlasmidDataView(PlasmidData data) {
        super(data);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Strains", new LinkedEntriesWidget(info.getLinkedParts()));
    }

    @Override
    protected void addLongFields() {
        addLongField("Backbone", info.getBackbone());
        addLongField("Origin Of Replication", info.getOriginOfReplication());
        addLongField("Selection Markers", info.getSelectionMarkers());
        addLongField("Promoters", info.getPromoters());
        addLongField("Replicates In", info.getReplicatesIn());
    }
}
