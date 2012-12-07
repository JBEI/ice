package org.jbei.ice.client.collection.table;

import java.util.ArrayList;

import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.table.column.EntryPartIdColumn;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.shared.dto.entry.EntryInfo;

import com.google.gwt.dom.client.Style.Unit;

/**
 * Data table for displaying the details of entries in a specified collection
 *
 * @author Hector Plahar
 */

public abstract class CollectionDataTable extends EntryDataTable<EntryInfo> {

    private final EntryTablePager pager;

    public CollectionDataTable(EntryTablePager pager) {
        this.pager = pager;
        if (pager != null)
            pager.setDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<EntryInfo, ?>> createColumns() {
        ArrayList<DataTableColumn<EntryInfo, ?>> columns = new ArrayList<DataTableColumn<EntryInfo, ?>>();

        columns.add(super.addSelectionColumn());
        columns.add(super.addTypeColumn(true, 60, Unit.PX));

        DataTableColumn<EntryInfo, EntryInfo> partIdCol = addPartIdColumn(false, 120, Unit.PX);
        columns.add(partIdCol);
        columns.add(super.addNameColumn(120, Unit.PX));
        columns.add(super.addSummaryColumn());
        columns.add(super.addStatusColumn());
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn());

        return columns;
    }

    protected DataTableColumn<EntryInfo, EntryInfo> addPartIdColumn(boolean sortable, double width, Unit unit) {
        PartIDCell<EntryInfo> cell = new PartIDCell<EntryInfo>(EntryContext.Type.COLLECTION);
        cell.addEntryHandler(getHandler());
        DataTableColumn<EntryInfo, EntryInfo> partIdColumn = new EntryPartIdColumn<EntryInfo>(cell);
        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    protected abstract EntryViewEventHandler getHandler();

    public EntryTablePager getPager() {
        return this.pager;
    }

    @Override
    public void clearSelection() {
        super.clearSelection();
        this.pager.goToFirstPage();
    }
}
