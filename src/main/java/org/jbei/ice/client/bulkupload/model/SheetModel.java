package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.shared.dto.entry.EntryInfo;

// cell data to entry info
public abstract class SheetModel<T extends EntryInfo> {

    public abstract T setInfoField(SheetCellData datum, EntryInfo info);

    public abstract T createInfo();
}
