package org.jbei.ice.client.common.table.column;

import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;

/**
 * @author Hector Plahar
 */
public class EntryPartIdColumn<T extends EntryInfo> extends DataTableColumn<T, EntryInfo> {

    public EntryPartIdColumn(PartIDCell<EntryInfo> cell) {
        super(cell, ColumnField.PART_ID);
    }

    @Override
    public EntryInfo getValue(EntryInfo object) {
        return object;
    }
}

