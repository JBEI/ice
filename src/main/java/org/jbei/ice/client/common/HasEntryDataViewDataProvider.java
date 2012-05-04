package org.jbei.ice.client.common;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.HasEntryInfo;

import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public abstract class HasEntryDataViewDataProvider<T extends HasEntryInfo> extends
        AsyncDataProvider<T> {

    protected final LinkedList<Long> valueIds;
    protected final LinkedList<T> results;
    protected final RegistryServiceAsync service;
    protected final HasEntryDataTable<T> dataTable;

    // default values
    protected boolean lastSortAsc = false;
    protected ColumnField lastSortField;
    protected final ColumnField defaultSort;

    public HasEntryDataViewDataProvider(HasEntryDataTable<T> view, RegistryServiceAsync service,
            ColumnField defaultSort) {

        this.dataTable = view;
        this.service = service;
        this.valueIds = new LinkedList<Long>();
        results = new LinkedList<T>();

        // sorting. set created sort as the default
        this.dataTable.addColumnSortHandler(new AsyncHandler(this.dataTable) {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                super.onColumnSort(event);

                //                results.clear();
                int pageSize = dataTable.getVisibleRange().getLength();
                dataTable.setVisibleRange(0, pageSize);
            }
        });

        DataTable<T>.DataTableColumn<?> defaultSortField = this.dataTable.getColumn(defaultSort);
        lastSortField = defaultSort;
        this.defaultSort = defaultSort;

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, false);
            this.dataTable.getColumnSortList().push(info);
        }

        this.addDataDisplay(this.dataTable);
    }

    public void setValues(ArrayList<Long> data) {
        reset();
        this.valueIds.addAll(data);
        updateRowCount(data.size(), true);

        // retrieve the first page of results and updateRowData
        final Range range = this.getRanges()[0];
        final int rangeStart = range.getStart();
        final int rangeEnd;

        if ((rangeStart + range.getLength()) > valueIds.size())
            rangeEnd = valueIds.size();
        else
            rangeEnd = (rangeStart + range.getLength());

        // this will always cause a sort since reset() is call.
        sort(rangeStart, rangeEnd);
    }

    // when a user sorts a column, setVisibleRangeAndclearData is called which
    // triggers a rangeChangeEvent 
    @Override
    protected void onRangeChanged(final HasData<T> display) {

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > valueIds.size() ? valueIds.size()
                : (rangeStart + range.getLength());

        if (sort(rangeStart, rangeEnd))
            return;

        // did not need to sort so use the cache
        ArrayList<T> show = new ArrayList<T>();
        show.addAll(results.subList(rangeStart, rangeEnd));
        updateRowData(rangeStart, show);
    }

    protected boolean sort(int rangeStart, int rangeEnd) {

        ColumnSortList sortList = this.dataTable.getColumnSortList();
        final boolean sortAsc;
        final ColumnField sortField;

        sortAsc = sortList.get(0).isAscending();

        int colIndex = this.dataTable.getColumns().indexOf(sortList.get(0).getColumn());
        if (colIndex < 0)
            sortField = lastSortField;
        else
            sortField = this.dataTable.getColumns().get(colIndex).getField();

        // check whether we need to sort in order to determine whether we can use the cache or not
        // this is done because sort() is also called when we are paging (from onRangeChanged)
        if (lastSortAsc == sortAsc && lastSortField == sortField) {
            // make sure there is enough data in the cache for the callee to obtain what they need
            // based on range
            if (results.size() >= rangeEnd)
                return false;
        }

        results.clear();
        lastSortAsc = sortAsc;
        lastSortField = sortField;
        fetchHasEntryData(sortField, sortAsc, rangeStart, rangeEnd);
        return true;
    }

    protected void fetchHasEntryData(ColumnField sortField, boolean asc, final int rangeStart,
            final int rangeEnd) {

        if (this.valueIds == null || this.valueIds.isEmpty())
            return;

        int factor = (rangeEnd - rangeStart) * 9; // factor is used to retrieve more data than will be shown; for caching
        factor = (factor + rangeEnd) > valueIds.size() ? valueIds.size() : (factor + rangeEnd);
        LinkedList<Long> subList = new LinkedList<Long>(valueIds.subList(rangeStart, factor));

        final LinkedList<Long> realValues = new LinkedList<Long>(subList);
        retrieveValues(realValues, rangeStart, rangeEnd, sortField, asc);
    }

    protected ColumnField getSortField() {
        ColumnSortList sortList = this.dataTable.getColumnSortList();
        if (sortList.size() == 0)
            return ColumnField.BIT_SCORE;

        int colIndex = this.dataTable.getColumns().indexOf(sortList.get(0).getColumn());
        if (colIndex == -1)
            return null; // TODO : this will be pretty unusual

        ColumnField field = this.dataTable.getColumns().get(colIndex).getField();
        return field;
    }

    /**
     * @param values
     *            list of ids for records to retrieve
     * @param rangeStart
     *            start of range to show
     * @param rangeEnd
     *            end of range to show
     * @param asc
     *            sort Type
     */
    protected abstract void retrieveValues(LinkedList<Long> values, int rangeStart, int rangeEnd,
            ColumnField sortField, boolean asc);

    public HasEntryDataTable<T> getDataTable() {
        return this.dataTable;
    }

    public void reset() {
        this.results.clear();
        this.valueIds.clear();
        this.dataTable.setVisibleRangeAndClearData(dataTable.getVisibleRange(), false);

        // reset sort 
        lastSortAsc = false;
        lastSortField = null;

        this.dataTable.getColumnSortList().clear();
        DataTable<T>.DataTableColumn<?> defaultSortField = this.dataTable
                .getColumn(ColumnField.BIT_SCORE);

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
            this.dataTable.getColumnSortList().push(info);
        }
    }

    protected LinkedList<T> getResults() {
        return this.results;
    }
}
