package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.shared.dto.PlasmidInfo;

public class PlasmidSheetModel extends SingleInfoSheetModel<PlasmidInfo> {

    public PlasmidInfo setField(PlasmidInfo info, SheetFieldData datum) {
        if (datum == null)
            return info;

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null || value.isEmpty())
            return info;

        switch (header) {
            case SELECTION_MARKERS:
                info.setSelectionMarkers(value);
                break;

            case CIRCULAR:
                boolean circular = Boolean.parseBoolean(value);
                info.setCircular(circular);
                break;

            case BACKBONE:
                info.setBackbone(value);
                break;

            case PROMOTERS:
                info.setPromoters(value);
                break;

            case ORIGIN_OF_REPLICATION:
                info.setOriginOfReplication(value);
                break;
        }

        return info;
    }

    @Override
    protected PlasmidInfo createInfo() {
        return new PlasmidInfo();
    }
}
