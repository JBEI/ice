package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;

public class PlasmidSheetModel extends SingleInfoSheetModel<PlasmidInfo> {

    public PlasmidInfo setField(PlasmidInfo info, SheetCellData datum) {
        if (datum == null)
            return info;

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null)
            return info;

        switch (header) {
            case SELECTION_MARKERS:
                info.setSelectionMarkers(value);
                break;

            case CIRCULAR:
                if (value.isEmpty() || (!"Yes".equalsIgnoreCase(value)
                        && !"True".equalsIgnoreCase(value)
                        && !"False".equalsIgnoreCase(value)
                        && !"No".equalsIgnoreCase(value))) {
                    info.setCircular(null);
                    break;
                }

                boolean circular = "Yes".equalsIgnoreCase(value) || "True".equalsIgnoreCase(value);
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
    public PlasmidInfo createInfo() {
        return new PlasmidInfo();
    }
}
