package org.jbei.ice.client.common;

import java.util.LinkedList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

// Takes care of retrieving all data page, by page
public abstract class EntryDataViewDataProvider extends AsyncDataProvider<EntryInfo> implements IHasNavigableData {

    protected int resultSize;
    protected LinkedList<EntryInfo> cachedEntries;
    protected final RegistryServiceAsync service;
    protected final DataTable<EntryInfo> table;
    protected ColumnField lastSortField;
    protected boolean lastSortAsc = false;

    public EntryDataViewDataProvider(DataTable<EntryInfo> view, RegistryServiceAsync service) {
        this.table = view;
        this.service = service;
        cachedEntries = new LinkedList<EntryInfo>();

        // connect sorting to async handler
        AsyncHandler columnSortHandler = new AsyncHandler(table);
        table.addColumnSortHandler(columnSortHandler);

        DataTable<EntryInfo>.DataTableColumn<?> defaultSortField = this.table.getColumn(ColumnField.CREATED);

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
            this.table.getColumnSortList().push(info);
        }

        this.addDataDisplay(this.table);
    }

    @Override
    public EntryInfo getCachedData(long entryId) {
        for (EntryInfo result : cachedEntries) {
            if (result.getId() == entryId)
                return result;
        }
        return null;
    }

    @Override
    public int indexOfCached(EntryInfo info) {
        return cachedEntries.indexOf(info);
    }

    @Override
    public EntryInfo getNext(EntryInfo info) {
        int idx = cachedEntries.indexOf(info);
        int size = cachedEntries.size();
        if (size == idx + 1)
            return null;
        return cachedEntries.get(idx + 1);
    }

    @Override
    public EntryInfo getPrev(EntryInfo info) {
        int idx = cachedEntries.indexOf(info);
        if (idx == -1)
            return null;

        return cachedEntries.get(idx - 1);
    }

    @Override
    public int getSize() {
        return resultSize;
    }

    public void reset() {
        this.cachedEntries.clear();
        resultSize = 0;
        this.table.setVisibleRangeAndClearData(table.getVisibleRange(), false);

        // reset sort 
        lastSortAsc = false;
        lastSortField = ColumnField.CREATED;

        this.table.getColumnSortList().clear();
        DataTable<EntryInfo>.DataTableColumn<?> defaultSortField = this.table.getColumn(ColumnField.CREATED);

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
            this.table.getColumnSortList().push(info);
        }
    }

    @Override
    protected void onRangeChanged(HasData<EntryInfo> display) {
        if (resultSize == 0)   // display changed its range of interest but no data
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
        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));

        if (rangeEnd == cachedEntries.size()) { // or close enough within some delta, retrieve more
            cacheMore(lastSortField, lastSortAsc, rangeEnd, rangeEnd + range.getLength());
            GWT.log("Retrieving more; range (" + rangeEnd + ", " + (rangeEnd + range.getLength()) + ")");
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
        ColumnSortList sortList = this.table.getColumnSortList();
        boolean sortAsc = sortList.get(0).isAscending();

        int colIndex = this.table.getColumns().indexOf(sortList.get(0).getColumn());
        if (colIndex >= 0)
            sortField = this.table.getColumns().get(colIndex).getField();

        // check whether we need to sort in order to determine whether we can use the cache or not
        // this is done because sort() is also called when we are paging (from onRangeChanged)
        if (lastSortAsc == sortAsc && lastSortField == sortField) {
            // make sure there is enough data in the cache for the callee to obtain what they need
            // based on range
            if (cachedEntries.size() >= rangeEnd)
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
