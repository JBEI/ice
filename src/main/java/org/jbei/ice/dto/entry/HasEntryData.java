package org.jbei.ice.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

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
