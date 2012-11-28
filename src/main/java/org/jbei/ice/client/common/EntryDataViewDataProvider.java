package org.jbei.ice.client.common;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.cellview.client.ColumnSortEvent;
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

        this.table.addColumnSortHandler(new AsyncHandler(this.table) {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                super.onColumnSort(event);

                cachedEntries.clear();
                int pageSize = table.getVisibleRange().getLength();
                table.setVisibleRange(0, pageSize);
            }
        });

        DataTable<EntryInfo>.DataTableColumn<?> defaultSortField = this.table
                .getColumn(ColumnField.CREATED);

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
            this.table.getColumnSortList().push(info);
        }

        this.addDataDisplay(this.table);
    }

    // experimental. assumes that we have the entryId cached if 
    // user can click on it. performance is terrible. need a better data structure
    // expect this to be called only once and getNext()/getPrev() used instead

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

        // todo : we just may have reached the end of the cache and need to retrieve more
//        if (idx == -1 || cachedEntries.size() <= idx + 1)
//            return null;
//
//        if (idx + 1 == size) {
//            GWT.log("Retrieving extra info");
//            final Range range = this.getRanges()[0];
//            final int rangeStart = range.getStart();
//            final int rangeEnd;
//
//            if ((rangeStart + range.getLength()) > valuesIds.size())
//                rangeEnd = valuesIds.size();
//            else
//                rangeEnd = (rangeStart + range.getLength());
//
//            retrieveEntryData(lastSortField, lastSortAsc, rangeStart, rangeEnd);
//        }
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

        if (sort(rangeStart, rangeEnd))
            return;

        // did not need to sort so use the cache
        ArrayList<EntryInfo> show = new ArrayList<EntryInfo>();
        show.addAll(cachedEntries.subList(rangeStart, rangeEnd));
        updateRowData(rangeStart, show);
    }

    /**
     * Determines if the sort params have changed and therefore warrants a
     * call to retrieve new data based on those params. Note that the rpc is
     * still made if the cache does not contain enough data;
     *
     * @param rangeStart data range start (based on page user is on)
     * @param rangeEnd   data range end
     * @return true if the data is sorted/rpc is made
     */
    protected boolean sort(int rangeStart, int rangeEnd) {
        boolean sortAsc;
        ColumnField sortField = lastSortField;
        ColumnSortList sortList = this.table.getColumnSortList();
        sortAsc = sortList.get(0).isAscending();

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

        //        results.clear();
        lastSortAsc = sortAsc;
        lastSortField = sortField;

        fetchEntryData(sortField, sortAsc, rangeStart, rangeEnd);
        return true;
    }

    protected abstract void fetchEntryData(ColumnField field, boolean asc, int rangeStart, int rangeEnd);
}
