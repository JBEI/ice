package org.jbei.ice.lib.shared.dto.entry;

import org.jbei.ice.lib.shared.dto.IDTOModel;

public abstract class HasEntryData implements IDTOModel {

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
