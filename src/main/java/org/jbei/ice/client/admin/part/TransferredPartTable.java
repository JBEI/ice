package org.jbei.ice.client.admin.part;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.table.column.EntryPartIdColumn;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.dom.client.Style.Unit;

/**
 * Table for showing list of parts that have been transferred and are awaiting approval/rejection
 * by this site's administrator
 *
 * @author Hector Plahar
 */
public class TransferredPartTable extends EntryDataTable<PartData> {

    private final EntryTablePager pager;

    public TransferredPartTable(ServiceDelegate<PartData> delegate) {
        super(delegate);
        this.pager = new EntryTablePager();
        pager.setDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<PartData, ?>> createColumns(ServiceDelegate<PartData> serviceDelegate) {
        ArrayList<DataTableColumn<PartData, ?>> columns = new ArrayList<DataTableColumn<PartData, ?>>();

        columns.add(super.addSelectionColumn());
        columns.add(super.addTypeColumn(false, 60, Unit.PX));
        DataTableColumn<PartData, PartData> partIdCol = addPartIdColumn(serviceDelegate, false, 120, Unit.PX);
        columns.add(partIdCol);
        columns.add(super.addNameColumn(120, Unit.PX, false));
        columns.add(super.addSummaryColumn());
        columns.add(super.addStatusColumn(false));
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn(false));

        return columns;
    }

    protected DataTableColumn<PartData, PartData> addPartIdColumn(ServiceDelegate<PartData> delegate,
            boolean sortable, double width, Unit unit) {
        PartIDCell<PartData> cell = new PartIDCell<PartData>(delegate);
        DataTableColumn<PartData, PartData> partIdColumn = new EntryPartIdColumn<PartData>(cell);
        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    public EntryTablePager getPager() {
        return this.pager;
    }
}
