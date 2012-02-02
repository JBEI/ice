package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.dom.client.Style.Unit;

/**
 * Advanced search results table.
 * 
 * @author Hector Plahar
 */
public class AdvancedSearchResultsTable extends EntryDataTable<EntryInfo> {

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {

        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addSelectionColumn(20, Unit.PX));

        columns.add(super.addTypeColumn(true, 50, Unit.PX));
        columns.add(super.addPartIdColumn(true, 50, Unit.PX));
        columns.add(super.addNameColumn(50, Unit.PX));
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
