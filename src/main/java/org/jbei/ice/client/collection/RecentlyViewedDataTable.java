package org.jbei.ice.client.collection;

import java.util.ArrayList;

import org.jbei.ice.client.component.table.EntryDataTable;
import org.jbei.ice.shared.EntryData;

public class RecentlyViewedDataTable extends EntryDataTable<EntryData> {

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        this.addStarColumn();

        columns.add(super.addTypeColumn(true));
        columns.add(super.addPartIdColumn(true));
        super.addNameColumn();
        super.addSummaryColumn();

        this.addLastAddedColumn();
        this.addLastVisitedColumn();

        return columns;
    }

    private void addLastVisitedColumn() {
        // TODO Auto-generated method stub

    }

    private void addLastAddedColumn() {
        // TODO Auto-generated method stub

    }

    private void addStarColumn() {
        // TODO Auto-generated method stub

    }
}
