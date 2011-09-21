package org.jbei.ice.client.collection;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.client.component.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.TextColumn;

public class SamplesDataTable extends HasEntryDataTable<SampleInfo> {

    final int WIDTH = 120;

    public SamplesDataTable() {
        super();
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {

        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addTypeColumn(true));
        columns.add(super.addPartIdColumn(true));
        super.addNameColumn();
        this.addLabelColumn();
        this.addNotesColumn();
        this.addLocationColumn();
        columns.add(this.addCreatedColumn());
        return columns;
    }

    @Override
    protected DataTableColumn<String> addCreatedColumn() {
        DataTableColumn<String> createdColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.CREATED) {

            @Override
            public String getValue(SampleInfo object) {

                DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
                Date date = object.getCreationTime();
                String value = format.format(date);
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

    protected void addLabelColumn() {
        TextColumn<SampleInfo> created = new TextColumn<SampleInfo>() {

            @Override
            public String getValue(SampleInfo info) {
                return info.getLabel();
            }
        };

        this.addColumn(created, "Label");
        this.setColumnWidth(created, WIDTH, Unit.PX);
    }

    protected void addNotesColumn() {
        TextColumn<SampleInfo> created = new TextColumn<SampleInfo>() {

            @Override
            public String getValue(SampleInfo info) {
                return info.getNotes();
            }
        };

        this.addColumn(created, "Notes");
        this.setColumnWidth(created, WIDTH, Unit.PX);
    }

    protected void addLocationColumn() {
        TextColumn<SampleInfo> created = new TextColumn<SampleInfo>() {

            @Override
            public String getValue(SampleInfo info) {
                return info.getLocation();
            }
        };

        this.addColumn(created, "Location");
        this.setColumnWidth(created, WIDTH, Unit.PX);
    }
}
