package org.jbei.ice.client.common.table.column;

import org.jbei.ice.client.common.table.cell.HasEntryPartIDCell;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.HasEntryData;

/**
 * Part id column for tables that display data that {@link org.jbei.ice.lib.shared.dto.entry.HasEntryData}
 *
 * @author Hector Plahar
 */
public class HasEntryPartIdColumn<T extends HasEntryData> extends DataTableColumn<T, T> {

    public HasEntryPartIdColumn(HasEntryPartIDCell<T> cell) {
        super(cell, ColumnField.PART_ID);
    }

    @Override
    public T getValue(T object) {
        return object;
    }
}
