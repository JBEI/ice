package org.jbei.ice.client.common.table.column;

import org.jbei.ice.client.common.table.cell.HasEntryPartIDCell;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.entry.HasEntryInfo;

/**
 * Part id column for tables that display data that {@link HasEntryInfo}
 *
 * @author Hector Plahar
 */
public class HasEntryPartIdColumn<T extends HasEntryInfo> extends DataTableColumn<T, T> {

    public HasEntryPartIdColumn(HasEntryPartIDCell<T> cell) {
        super(cell, ColumnField.PART_ID);
    }

    @Override
    public T getValue(T object) {
        return object;
    }
}
