package org.jbei.ice.client.common.table;

import org.jbei.ice.shared.dto.HasEntryInfo;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

// TODO : combine this with EntrySelectionModel
public class HasEntrySelectionModel<T extends HasEntryInfo> extends MultiSelectionModel<T> {
    private boolean allSelected;

    public HasEntrySelectionModel() {
        super(new ProvidesKey<T>() {

            @Override
            public Long getKey(T item) {
                return item.getEntryInfo().getId();
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
