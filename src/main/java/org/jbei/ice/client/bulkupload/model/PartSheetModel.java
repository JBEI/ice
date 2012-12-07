package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.shared.dto.entry.PartInfo;

public class PartSheetModel extends SingleInfoSheetModel<PartInfo> {

    @Override
    public PartInfo createInfo() {
        return new PartInfo();
    }

    @Override
    public PartInfo setField(PartInfo info, SheetCellData datum) {
        // nothing part specific
        return info;
    }
}
