package org.jbei.ice.client.search;

import java.util.ArrayList;

import org.jbei.ice.client.component.table.EntryDataTable;
import org.jbei.ice.shared.EntryData;

/**
 * Advanced search results table.
 * 
 * @author Hector Plahar
 */
public class AdvancedSearchResultsTable extends EntryDataTable<EntryData> {

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {

        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addTypeColumn(true));
        columns.add(super.addPartIdColumn(true));

        super.addNameColumn();
        super.addSummaryColumn();
        super.addOwnerColumn();
        super.addStatusColumn();
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        super.addCreatedColumn();
        return columns;
    }
}
