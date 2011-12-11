package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class HasEntryInfo implements IsSerializable {

    private EntryInfo dataView;

    public HasEntryInfo() {
    }

    public void setDataView(EntryInfo view) {
        this.dataView = view;
    }

    public EntryInfo getDataView() {
        return this.dataView;
    }
}
