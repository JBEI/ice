package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.lib.shared.dto.entry.PartData;

// cell data to entry info
public abstract class SheetModel<T extends PartData> {

    public abstract T setInfoField(SheetCellData datum, PartData info);

    public abstract T createInfo();
}
