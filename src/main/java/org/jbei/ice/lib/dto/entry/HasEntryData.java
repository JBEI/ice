package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.dao.IDataTransferModel;

public abstract class HasEntryData implements IDataTransferModel {

    private PartData entryInfo;

    public HasEntryData() {
    }

    public void setEntryInfo(PartData view) {
        this.entryInfo = view;
    }

    public PartData getEntryInfo() {
        return this.entryInfo;
    }
}
