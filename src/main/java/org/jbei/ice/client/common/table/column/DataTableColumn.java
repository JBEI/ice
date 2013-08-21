package org.jbei.ice.client.common.table.column;

import org.jbei.ice.lib.shared.ColumnField;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

/**
 * Columns for use with the data table
 *
 * @param <C> column type
 * @author Hector Plahar
 */
public abstract class DataTableColumn<T, C> extends Column<T, C> {

    private final ColumnField field;

    public DataTableColumn(Cell<C> cell, ColumnField field) {
        super(cell);
        this.field = field;
    }

    public ColumnField getField() {
        return this.field;
    }
}
