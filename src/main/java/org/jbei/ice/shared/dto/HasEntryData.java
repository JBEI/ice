package org.jbei.ice.shared.dto;

import org.jbei.ice.shared.EntryData;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class HasEntryData implements IsSerializable {

    private EntryData dataView;

    public HasEntryData() {
    }

    public void setDataView(EntryData view) {
        this.dataView = view;
    }

    public EntryData getDataView() {
        return this.dataView;
    }
}
