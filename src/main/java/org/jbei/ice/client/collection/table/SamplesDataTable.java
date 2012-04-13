package org.jbei.ice.client.collection.table;

import java.util.ArrayList;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.client.common.table.cell.UrlCell;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.History;

public class SamplesDataTable extends HasEntryDataTable<SampleInfo> {

    final int WIDTH = 120;

    public SamplesDataTable() {
        super();
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {

        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addTypeColumn(true));
        columns.add(super.addPartIdColumn(true, null, EntryContext.Type.SAMPLES));
        columns.add(super.addNameColumn());
        columns.add(this.addLabelColumn());
        columns.add(this.addNotesColumn());
        columns.add(this.addLocationColumn());
        columns.add(this.addCreatedColumn());

        return columns;
    }

    @Override
    protected DataTableColumn<String> addCreatedColumn() {
        DataTableColumn<String> createdColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.CREATED) {

            @Override
            public String getValue(SampleInfo object) {
                return DateUtilities.formatDate(object.getCreationTime());
            }
        };

        createdColumn.setSortable(true);
        this.addColumn(createdColumn, "Created");
        this.setColumnWidth(createdColumn, 120, Unit.PX);
        return createdColumn;
    }

    protected DataTableColumn<String> addLabelColumn() {
        DataTableColumn<String> labelColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.LABEL) {

            @Override
            public String getValue(SampleInfo object) {
                return object.getLabel();
            }
        };

        this.addColumn(labelColumn, "Label");
        labelColumn.setSortable(true);
        this.setColumnWidth(labelColumn, WIDTH, Unit.PX);
        return labelColumn;
    }

    protected DataTableColumn<String> addNotesColumn() {
        DataTableColumn<String> notesCol = new DataTableColumn<String>(new TextCell(),
                ColumnField.NOTES) {

            @Override
            public String getValue(SampleInfo info) {
                return info.getNotes();
            }
        };

        this.addColumn(notesCol, ColumnField.NOTES.getName());
        this.setColumnWidth(notesCol, WIDTH, Unit.PX);
        return notesCol;
    }

    protected DataTableColumn<SampleInfo> addLocationColumn() {
        UrlCell<SampleInfo> cell = new UrlCell<SampleInfo>() {

            @Override
            protected String getCellValue(SampleInfo info) {
                return info.getLocation();
            }

            @Override
            protected void onClick(SampleInfo info) {
                History.newItem(Page.STORAGE.getLink() + ";id=" + info.getLocationId());
            }
        };

        DataTableColumn<SampleInfo> locationColumn = new DataTableColumn<SampleInfo>(cell,
                ColumnField.LOCATION) {

            @Override
            public SampleInfo getValue(SampleInfo object) {
                return object;
            }
        };

        locationColumn.setSortable(true);
        this.setColumnWidth(locationColumn, WIDTH, Unit.PX);
        this.addColumn(locationColumn, "Location");
        return locationColumn;
    }
}
