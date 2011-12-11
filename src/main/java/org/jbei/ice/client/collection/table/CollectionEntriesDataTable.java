package org.jbei.ice.client.collection.table;

import java.util.ArrayList;

import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * Data table for displaying the details of entries in a specified collection
 * 
 * @author Hector Plahar
 */

public class CollectionEntriesDataTable extends EntryDataTable<EntryInfo> {

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addSelectionColumn());
        columns.add(super.addTypeColumn(true));
        columns.add(super.addPartIdColumn(true));
        columns.add(super.addNameColumn());
        columns.add(super.addSummaryColumn());
        columns.add(super.addStatusColumn());
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn());

        return columns;
    }
}
