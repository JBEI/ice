package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.shared.dto.StrainInfo;

public class StrainSheetModel extends SingleInfoSheetModel<StrainInfo> {

    public StrainInfo setField(StrainInfo strain, SheetCellData datum) {

        if (datum == null)
            return strain;

        Header header = datum.getTypeHeader();
        String value = datum.getValue();

        if (header == null || value == null)
            return strain;

        switch (header) {
            case SELECTION_MARKERS:
                strain.setSelectionMarkers(value);
                break;

            case PARENTAL_STRAIN:
                strain.setHost(value);
                break;

            case GEN_PHEN:
                strain.setGenotypePhenotype(value);
                break;

            case PLASMIDS:
                strain.setPlasmids(value);
                break;

            // todo : samples
        }

        return strain;
    }

    @Override
    public StrainInfo createInfo() {
        return new StrainInfo();
    }
}
