package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.lib.shared.dto.entry.PartData;

public class PartSheetModel extends SingleInfoSheetModel<PartData> {

    @Override
    public PartData createInfo() {
        return new PartData();
    }

    @Override
    public PartData setField(PartData info, SheetCellData datum) {
        // nothing part specific
        return info;
    }
}
