package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.common.table.EntryDataTable;
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

        columns.add(super.addSelectionColumn());
        columns.add(super.addTypeColumn(true));
        columns.add(super.addPartIdColumn(true));
        columns.add(super.addNameColumn());
        columns.add(super.addSummaryColumn());
        columns.add(super.addOwnerColumn());
        columns.add(super.addStatusColumn());
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn());

        return columns;
    }
}
