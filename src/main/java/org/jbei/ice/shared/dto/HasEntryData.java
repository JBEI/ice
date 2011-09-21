package org.jbei.ice.shared.dto;

import java.io.Serializable;

import org.jbei.ice.shared.EntryData;

public abstract class HasEntryData implements Serializable {

    private static final long serialVersionUID = 1L;
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
