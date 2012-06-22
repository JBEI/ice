package org.jbei.ice.client.collection.table;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import org.jbei.ice.client.collection.menu.IHasEntryHandlers;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;

import java.util.ArrayList;

/**
 * Data table for displaying the details of entries in a specified collection
 *
 * @author Hector Plahar
 */

public abstract class CollectionDataTable extends EntryDataTable<EntryInfo> {

    private final EntryTablePager pager;

    public CollectionDataTable(EntryTablePager pager) {
        this.pager = pager;
        if (pager != null)
            pager.setDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addSelectionColumn());
        columns.add(super.addTypeColumn(true, 60, Unit.PX));

        DataTableColumn<EntryInfo> partIdCol = addPartIdColumn(false, 120, Unit.PX);
        columns.add(partIdCol);
        columns.add(super.addNameColumn(120, Unit.PX));
        columns.add(super.addSummaryColumn());
        columns.add(super.addStatusColumn());
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn());

        return columns;
    }

    protected DataTableColumn<EntryInfo> addPartIdColumn(boolean sortable, double width, Unit unit) {

        PartIDCell<EntryInfo> cell = new PartIDCell<EntryInfo>(EntryContext.Type.COLLECTION);
        cell.addEntryHandler(getHandler());
        DataTableColumn<EntryInfo> partIdColumn = new PartIdColumn(cell);
        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    protected abstract EntryViewEventHandler getHandler();

    public class PartIdColumn extends DataTable<EntryInfo>.DataTableColumn<EntryInfo> implements
                                                                                      IHasEntryHandlers {

        private HandlerManager handlerManager;

        public PartIdColumn(PartIDCell<EntryInfo> cell) {
            super(cell, ColumnField.PART_ID);
        }

        @Override
        public EntryInfo getValue(EntryInfo object) {
            return object;
        }

        @Override
        public HandlerRegistration addEntryHandler(EntryViewEventHandler handler) {
            if (handlerManager == null)
                handlerManager = new HandlerManager(this);
            return handlerManager.addHandler(EntryViewEvent.getType(), handler);
        }

        @Override
        public void fireEvent(GwtEvent<?> event) {
            if (handlerManager != null)
                handlerManager.fireEvent(event);
        }
    }

    public EntryTablePager getPager() {
        return this.pager;
    }

    @Override
    public void clearSelection() {
        super.clearSelection();
        this.pager.goToFirstPage();
    }
}
