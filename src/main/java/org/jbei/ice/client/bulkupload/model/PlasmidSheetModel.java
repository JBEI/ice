package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;

public class PlasmidSheetModel extends SingleInfoSheetModel<PlasmidData> {

    public PlasmidData setField(PlasmidData data, SheetCellData datum) {
        if (datum == null)
            return data;

        EntryField header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null)
            return data;

        switch (header) {
            case SELECTION_MARKERS:
                data.setSelectionMarkers(value);
                break;

            case CIRCULAR:
                if (value.isEmpty() || (!"Yes".equalsIgnoreCase(value)
                        && !"True".equalsIgnoreCase(value)
                        && !"False".equalsIgnoreCase(value)
                        && !"No".equalsIgnoreCase(value))) {
                    data.setCircular(null);
                    break;
                }

                boolean circular = "Yes".equalsIgnoreCase(value) || "True".equalsIgnoreCase(value);
                data.setCircular(circular);
                break;

            case BACKBONE:
                data.setBackbone(value);
                break;

            case PROMOTERS:
                data.setPromoters(value);
                break;

            case ORIGIN_OF_REPLICATION:
                data.setOriginOfReplication(value);
                break;
        }

        return data;
    }

    @Override
    public PlasmidData createInfo() {
        return new PlasmidData();
    }
}
