package org.jbei.ice.client.common.table.column;

import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * @author Hector Plahar
 */
public class EntryPartIdColumn<T extends PartData> extends DataTableColumn<T, PartData> {

    public EntryPartIdColumn(PartIDCell<PartData> cell) {
        super(cell, ColumnField.PART_ID);
    }

    @Override
    public PartData getValue(PartData object) {
        return object;
    }
}

