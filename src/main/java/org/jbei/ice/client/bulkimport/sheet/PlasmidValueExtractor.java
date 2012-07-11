package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;

public class PlasmidValueExtractor extends InfoValueExtractor {

    public String extractValue(Header header, EntryInfo info, int index) {

        String value = super.extractCommon(header, info, index);
        if (value != null)
            return value;

        PlasmidInfo plasmid = (PlasmidInfo) info;
        switch (header) {
        // plasmid specific
        case BACKBONE:
            return plasmid.getBackbone();

        case ORIGIN_OF_REPLICATION:
            return plasmid.getOriginOfReplication();

        case PROMOTERS:
            return plasmid.getPromoters();

        case CIRCULAR:
            if (plasmid.getCircular() == null)
                return "";
            return Boolean.toString(plasmid.getCircular());
        }

        return null;
    }
}
