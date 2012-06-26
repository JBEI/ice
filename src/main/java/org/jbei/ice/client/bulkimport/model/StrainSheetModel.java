package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.shared.dto.StrainInfo;

public class StrainSheetModel extends SingleInfoSheetModel<StrainInfo> {

    public StrainInfo setField(StrainInfo info, SheetFieldData datum) {
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

            case PARENTAL_STRAIN:
                info.setHost(value);
                break;

            case GEN_PHEN:
                info.setGenotypePhenotype(value);
                break;

            case PLASMIDS:
                info.setPlasmids(value);
                break;
        }

        return info;
    }

    @Override
    protected StrainInfo createInfo() {
        return new StrainInfo();
    }
}
