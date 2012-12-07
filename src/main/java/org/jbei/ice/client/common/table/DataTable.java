package org.jbei.ice.client.common.table;

import java.util.ArrayList;

import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.shared.ColumnField;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;

/**
 * Generic data table
 *
 * @param <T> type whose fields are to be displayed
 * @author Hector Plahar
 */
public abstract class DataTable<T> extends CellTable<T> {

    protected interface EntryResources extends Resources {

        static EntryResources INSTANCE = GWT.create(EntryResources.class);

        /**
         * The styles used in this widget.
         */
        @Override
        @Source("org/jbei/ice/client/resource/css/EntryTable.css")
        DataTableStyle cellTableStyle();
    }

    public interface DataTableStyle extends Style {
    }

    // TODO : this list is also maintained in the parent class look for a way to merge them
    private ArrayList<DataTableColumn<T, ?>> columns;

    public DataTable() {
        super(15, EntryResources.INSTANCE);
        init();
        setStyleName("data_table");
        Label empty = new Label();
        empty.setText("No data available");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);
        columns = createColumns();
    }

    public ArrayList<DataTableColumn<T, ?>> getColumns() {
        return this.columns;
    }

    public DataTableColumn<T, ?> getColumn(ColumnField field) {
        // TODO : use appropriate data structure
        for (DataTableColumn<T, ?> column : this.columns) {
            if (column.getField() == field)
                return column;
        }
        return null;
    }

    /**
     * Adds columns to the table
     */
    protected abstract ArrayList<DataTableColumn<T, ?>> createColumns();

    // initialization that sub classes need to perform
    protected void init() {}
}
