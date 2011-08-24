package org.jbei.ice.client.component;

import org.jbei.ice.shared.EntryDataView;

import com.google.gwt.user.cellview.client.Column;

/**
 * Column for rendering part Id cells.
 * 
 * @author Hector Plahar
 */

public class PartIDColumn extends Column<EntryDataView, EntryDataView> {

    public PartIDColumn() {
        super(new PartIDCell());
    }

    /**
     * Returns the column value from within the underlying data object.
     */
    @Override
    public EntryDataView getValue(EntryDataView entry) {
        return entry;
    }
}
