package org.jbei.ice.client.common;

import java.util.LinkedList;

import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.HasEntryData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public abstract class HasEntryDataViewDataProvider<T extends HasEntryData> extends AsyncDataProvider<T>
        implements IHasNavigableData {

    protected int resultSize;
    protected final LinkedList<T> results;
    protected final RegistryServiceAsync service;
    protected final HasEntryDataTable<T> dataTable;
    protected boolean lastSortAsc = false;
    protected ColumnField lastSortField;
    protected final ColumnField defaultSort;

    public HasEntryDataViewDataProvider(HasEntryDataTable<T> view, RegistryServiceAsync service,
            ColumnField defaultSort) {
        this.dataTable = view;
        this.service = service;
        results = new LinkedList<T>();

        // connect sorting to async handler
        AsyncHandler columnSortHandler = new AsyncHandler(dataTable);
        dataTable.addColumnSortHandler(columnSortHandler);

        DataTableColumn<T, ?> defaultSortField = this.dataTable.getColumn(defaultSort);
        lastSortField = defaultSort;
        this.defaultSort = defaultSort;

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, false);
            this.dataTable.getColumnSortList().push(info);
        }

        this.addDataDisplay(this.dataTable);
    }

    // when a user sorts a column, setVisibleRangeAndclearData is called which
    // triggers a rangeChangeEvent
    // valueIds contains
    @Override
    protected void onRangeChanged(final HasData<T> display) {
        if (resultSize == 0)
            return;

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > resultSize ? resultSize
                : (rangeStart + range.getLength());

        if (sortChanged(rangeEnd)) {
            fetchEntryData(lastSortField, lastSortAsc, 0, (range.getLength() * 2), true);
            return;
        }

        // sort did not change
        updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));

        if (rangeEnd == results.size()) { // or close enough within some delta, retrieve more
            cacheMore(lastSortField, lastSortAsc, rangeEnd, rangeEnd + range.getLength());
            GWT.log("Retrieving more; range (" + rangeEnd + ", " + (rangeEnd + range.getLength()) + ")");
        }
    }

    public void reset() {
        this.results.clear();
        resultSize = 0;
        this.dataTable.setVisibleRangeAndClearData(dataTable.getVisibleRange(), false);

        if (lastSortField == null) {
            // reset sort
            lastSortAsc = false;
            lastSortField = defaultSort;

            this.dataTable.getColumnSortList().clear();
            DataTableColumn<T, ?> defaultSortField = this.dataTable.getColumn(lastSortField);

            if (defaultSortField != null) {
                ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
                this.dataTable.getColumnSortList().push(info);
            }
        }
    }

    /**
     * Determines if the sort params have changed and therefore warrants a
     * call to retrieve new data based on those params.
     *
     * @param rangeEnd data range end
     * @return true if the data needs to be sorted
     */
    protected boolean sortChanged(int rangeEnd) {
        ColumnField sortField = lastSortField;
        ColumnSortList sortList = this.dataTable.getColumnSortList();
        if (sortList.size() == 0)
            return false;
        boolean sortAsc = sortList.get(0).isAscending();

        int colIndex = this.dataTable.getColumns().indexOf(sortList.get(0).getColumn());
        if (colIndex >= 0)
            sortField = this.dataTable.getColumns().get(colIndex).getField();

        // check whether we need to sort in order to determine whether we can use the cache or not
        // this is done because sort() is also called when we are paging (from onRangeChanged)
        if (lastSortAsc == sortAsc && lastSortField == sortField) {
            // make sure there is enough data in the cache for the callee to obtain what they need
            // based on range
            if (results.size() >= rangeEnd)
                return false;
        }

        lastSortAsc = sortAsc;
        lastSortField = sortField;
        return true;
    }

    protected void cacheMore(final ColumnField field, final boolean ascending, int rangeStart, int rangeEnd) {
        int factor = (rangeEnd - rangeStart) * 2;  //  pages in advance
        fetchEntryData(field, ascending, rangeStart, factor, false);
    }

    protected abstract void fetchEntryData(ColumnField field, boolean ascending, int start, int factor, boolean reset);
}
