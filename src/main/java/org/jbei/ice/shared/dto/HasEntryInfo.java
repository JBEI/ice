package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class HasEntryInfo implements IsSerializable {

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
