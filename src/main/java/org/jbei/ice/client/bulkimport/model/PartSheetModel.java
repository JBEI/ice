package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.shared.dto.PartInfo;

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
