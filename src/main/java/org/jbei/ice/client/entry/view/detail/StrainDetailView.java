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
        addShortField("Genotype/Phenotype", info.getGenotypePhenotype(), ValueType.SHORT_TEXT);
        addShortField("Host", info.getHost(), ValueType.SHORT_TEXT);
        addShortField("Plasmids", info.getPlasmids(), ValueType.SHORT_TEXT);
    }

    @Override
    protected void addLongFields() {
    }
}
