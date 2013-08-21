package org.jbei.ice.client.common;

import java.util.LinkedList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

// Takes care of retrieving all data page, by page
public abstract class EntryDataViewDataProvider extends AsyncDataProvider<PartData> implements IHasNavigableData {

    protected int resultSize;
    protected LinkedList<PartData> cachedEntries;
    protected final RegistryServiceAsync service;
    protected final DataTable<PartData> table;
    protected ColumnField lastSortField;
    protected boolean lastSortAsc = false;

    public EntryDataViewDataProvider(DataTable<PartData> view, RegistryServiceAsync service) {
        this.table = view;
        this.service = service;
        cachedEntries = new LinkedList<PartData>();

        // connect sorting to async handler
        AsyncHandler columnSortHandler = new AsyncHandler(table);
        table.addColumnSortHandler(columnSortHandler);

        DataTableColumn<PartData, ?> defaultSortField = this.table.getColumn(ColumnField.CREATED);

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
            this.table.getColumnSortList().push(info);
        }

        this.addDataDisplay(this.table);
    }

    @Override
    public PartData getCachedData(long entryId, String recordId) {
        for (PartData info : cachedEntries) {
            if (recordId != null && info.getRecordId().equalsIgnoreCase(recordId))
                return info;

            if (info.getId() == entryId)
                return info;
        }
        return null;
    }

    @Override
    public int indexOfCached(PartData info) {
        return cachedEntries.indexOf(info);
    }

    @Override
    public PartData getNext(PartData info) {
        int idx = cachedEntries.indexOf(info);
        int size = cachedEntries.size();
        if (size - 1 == idx + 1) {
            // fetch next
            cacheMore(lastSortField, lastSortAsc, size, size + 1);
        }

        if (size == idx + 1)
            return null;
        return cachedEntries.get(idx + 1);
    }

    @Override
    public PartData getPrev(PartData info) {
        int idx = cachedEntries.indexOf(info);
        if (idx == -1)
            return null;

        return cachedEntries.get(idx - 1);
    }

    @Override
    public int getSize() {
        return resultSize;
    }

    @Override
    protected void onRangeChanged(HasData<PartData> display) {
        if (resultSize == 0)   // display changed its range of interest but no data
            return;

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > resultSize ? resultSize
                : (rangeStart + range.getLength());

        // last page
        if (rangeEnd == resultSize && resultSize > range.getLength()) {
            fetchEntryData(lastSortField, lastSortAsc, rangeStart, (resultSize - rangeStart), false);
            return;
        }

        // user is sorting
        if (sortChanged(rangeEnd)) {
            fetchEntryData(lastSortField, lastSortAsc, 0, range.getLength(), true);
            return;
        }

        // sort did not change, use data in cache
        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));

        long cacheSize = cachedEntries.size();
        // if range is close enough to the cache within some delta, cache more entries
        if (rangeEnd + (2 * range.getLength()) >= cacheSize) {
            int fetchSize = (2 * range.getLength()) + rangeEnd;
            cacheMore(lastSortField, lastSortAsc, rangeEnd, fetchSize);
        }
    }

    public void reset() {
        this.cachedEntries.clear();
        this.table.setVisibleRangeAndClearData(table.getVisibleRange(), false);

        // reset sort
        if (lastSortField == null) {
            lastSortAsc = false;
            lastSortField = ColumnField.CREATED;

            this.table.getColumnSortList().clear();
            DataTableColumn<PartData, ?> defaultSortField = this.table.getColumn(lastSortField);

            if (defaultSortField != null) {
                ColumnSortList.ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
                this.table.getColumnSortList().push(info);
            }
        }
    }

    /**
     * Determines if the sort params have changed and therefore warrants a
     * call to retrieve new data based on those params.
     *
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
        if (lastSortAsc == sortAsc && lastSortField == sortField) {
            // make sure there is enough data in the cache for the callee to obtain what they need based on range
            if (cachedEntries.size() >= rangeEnd)
                return false;
            GWT.log("Not enough cached");
        }

        lastSortAsc = sortAsc;
        lastSortField = sortField;
        return true;
    }

    protected void cacheMore(final ColumnField field, final boolean ascending, int rangeStart, int rangeEnd) {
        GWT.log("Caching [" + rangeStart + " - " + rangeEnd + "]");
        int factor = (rangeEnd - rangeStart);  //  pages in advance
        if (factor <= 0)
            return;
        fetchEntryData(field, ascending, rangeStart, factor, false);
    }

    protected abstract void fetchEntryData(ColumnField field, boolean ascending, int start, int factor, boolean reset);
}
