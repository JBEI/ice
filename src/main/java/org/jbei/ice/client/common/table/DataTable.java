package org.jbei.ice.client.common.table;

import java.util.ArrayList;

import org.jbei.ice.shared.ColumnField;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;

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

    private final ArrayList<DataTableColumn<?>> columns; // TODO : this list is also maintained in the parent class. look for a way to merge them

    public DataTable() {

        super(15, EntryResources.INSTANCE);
        setStyleName("data_table");
        Label empty = new Label();
        empty.setText("Empty");
        this.setEmptyTableWidget(empty);

        columns = createColumns();
    }

    public ArrayList<DataTableColumn<?>> getColumns() {
        return this.columns;
    }

    public DataTableColumn<?> getColumn(ColumnField field) {
        // TODO : use appropriate data structure
        for (DataTableColumn<?> column : this.columns) {
            if (column.getField() == field)
                return column;
        }
        return null;
    }

    /**
     * Adds columns to the table
     */
    protected abstract ArrayList<DataTableColumn<?>> createColumns();

    //
    // inner classes
    //

    /**
     * Columns for use with the data table
     * 
     * @param <C>
     *            column type
     */
    public abstract class DataTableColumn<C> extends Column<T, C> {

        private final ColumnField field;

        public DataTableColumn(Cell<C> cell, ColumnField field) {
            super(cell);
            this.field = field;
        }

        public ColumnField getField() {
            return this.field;
        }
    }
}
