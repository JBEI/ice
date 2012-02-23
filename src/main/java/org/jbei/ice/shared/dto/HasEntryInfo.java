package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class HasEntryInfo implements IsSerializable {

    private EntryInfo dataView;

    public HasEntryInfo() {
    }

    public void setEntryInfo(EntryInfo view) {
        this.dataView = view;
    }

    public EntryInfo getEntryInfo() {
        return this.dataView;
    }
}
