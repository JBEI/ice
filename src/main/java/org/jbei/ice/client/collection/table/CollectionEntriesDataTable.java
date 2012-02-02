package org.jbei.ice.client.collection.table;

import java.util.ArrayList;

import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.dom.client.Style.Unit;

/**
 * Data table for displaying the details of entries in a specified collection
 * 
 * @author Hector Plahar
 */

public class CollectionEntriesDataTable extends EntryDataTable<EntryInfo> {

    private final EntryTablePager pager;

    public CollectionEntriesDataTable(EntryTablePager pager) {
        this.pager = pager;
        if (pager != null)
            pager.setDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addSelectionColumn(10, Unit.PX));
        columns.add(super.addTypeColumn(true, 50, Unit.PX));
        columns.add(super.addPartIdColumn(true, 120, Unit.PX));
        columns.add(super.addNameColumn(120, Unit.PX));
        columns.add(super.addSummaryColumn());
        columns.add(super.addStatusColumn());
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn());

        return columns;
    }

    public EntryTablePager getPager() {
        return this.pager;
    }
}
