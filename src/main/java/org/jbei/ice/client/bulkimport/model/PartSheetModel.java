package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.shared.dto.PartInfo;

public class PartSheetModel extends SingleInfoSheetModel<PartInfo> {

    @Override
    protected PartInfo createInfo() {
        return new PartInfo();
    }

    @Override
    public PartInfo setField(PartInfo info, SheetFieldData datum) {
        // nothing part specific
        return info;
    }
}
