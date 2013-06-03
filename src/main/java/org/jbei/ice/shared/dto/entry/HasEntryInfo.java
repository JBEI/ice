package org.jbei.ice.shared.dto.entry;

import org.jbei.ice.shared.dto.IDTOModel;

public abstract class HasEntryInfo implements IDTOModel {

    private EntryInfo entryInfo;

    public HasEntryInfo() {
    }

    public void setEntryInfo(EntryInfo view) {
        this.entryInfo = view;
    }

    public EntryInfo getEntryInfo() {
        return this.entryInfo;
    }
}
