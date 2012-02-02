package org.jbei.ice.client.collection.table;

import java.util.ArrayList;

import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;

public class RecentlyViewedDataTable extends EntryDataTable<EntryInfo> {

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addTypeColumn(true, 60, Unit.PX));
        columns.add(super.addPartIdColumn(true, 120, Unit.PX));
        columns.add(super.addNameColumn(120, Unit.PX));
        columns.add(super.addSummaryColumn());
        columns.add(this.addLastAddedColumn());
        columns.add(this.addLastVisitedColumn());

        return columns;
    }

    private DataTableColumn<String> addLastVisitedColumn() {
        DataTableColumn<String> createdColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.LAST_VISITED) {

            @Override
            public String getValue(EntryInfo object) {

                DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
                String value = format.format(object.getCreationTime());
                if (value.length() >= 13)
                    value = (value.substring(0, 9) + "...");
                return value;
            }
        };

        createdColumn.setSortable(true);
        this.addColumn(createdColumn, "Created");
        this.setColumnWidth(createdColumn, 120, Unit.PX);
        return createdColumn;

    }

    private DataTableColumn<String> addLastAddedColumn() {
        DataTableColumn<String> createdColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.LAST_ADDED) {

            @Override
            public String getValue(EntryInfo object) {

                DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
                String value = format.format(object.getCreationTime());
                if (value.length() >= 13)
                    value = (value.substring(0, 9) + "...");
                return value;
            }
        };

        createdColumn.setSortable(true);
        this.addColumn(createdColumn, "Created");
        this.setColumnWidth(createdColumn, 120, Unit.PX);
        return createdColumn;

    }

}
