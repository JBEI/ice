package org.jbei.ice.lib.shared.dto.entry;

import org.jbei.ice.lib.shared.dto.IDTOModel;

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
