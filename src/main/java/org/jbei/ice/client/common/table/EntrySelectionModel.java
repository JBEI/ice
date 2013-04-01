package org.jbei.ice.client.common.table;

import org.jbei.ice.shared.dto.entry.EntryInfo;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

public class EntrySelectionModel<T extends EntryInfo> extends MultiSelectionModel<T> {

    private boolean allSelected;

    public EntrySelectionModel() {
        super(new ProvidesKey<T>() {

            @Override
            public Long getKey(T item) {
                return item.getId();
            }
        });
    }

    public void setAllSelected(boolean b) {
        allSelected = b;
    }

    public boolean isAllSelected() {
        return this.allSelected;
    }

    @Override
    public boolean isSelected(T object) {
        if (allSelected) {
            setSelected(object, true);
        }

        return super.isSelected(object);
    }
}
