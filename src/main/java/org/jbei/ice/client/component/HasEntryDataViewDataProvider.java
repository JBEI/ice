package org.jbei.ice.client.component;

import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.component.table.DataTable;
import org.jbei.ice.client.component.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.HasEntryData;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public abstract class HasEntryDataViewDataProvider<T extends HasEntryData> extends
        AsyncDataProvider<T> {

    protected final LinkedList<Long> valueIds;
    protected final LinkedList<T> results;
    protected final RegistryServiceAsync service;
    private final HasEntryDataTable<T> dataTable;

    // default values
    private boolean lastSortAsc = false;
    private ColumnField lastSortField = ColumnField.CREATED;

    public HasEntryDataViewDataProvider(HasEntryDataTable<T> view, List<Long> data,
            RegistryServiceAsync service) {

        this.dataTable = view;
        this.service = service;
        this.valueIds = new LinkedList<Long>();
        //        updateRowCount(data.size(), true);
        results = new LinkedList<T>();
        this.addDataDisplay(this.dataTable);

        // sorting. set created sort as the default
        this.dataTable.addColumnSortHandler(new AsyncHandler(this.dataTable));
        DataTable<T>.DataTableColumn<?> createdField = this.dataTable
                .getColumn(ColumnField.CREATED);

        ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(createdField, false);
        this.dataTable.getColumnSortList().push(info);

        if (!data.isEmpty())
            this.setValues(data);
    }

    public HasEntryDataViewDataProvider(HasEntryDataTable<T> view, RegistryServiceAsync service) {
        this(view, new LinkedList<Long>(), service);
    }

    // clears all table data but does not reset the 
    private void resetTableAndData(List<Long> data) {

        this.results.clear();
        this.valueIds.clear();
        this.valueIds.addAll(data);

        updateRowCount(this.valueIds.size(), true);

        lastSortAsc = false;
        lastSortField = ColumnField.CREATED;
    }

    /**
     * Set the valueIds
     * 
     * @param data
     */
    public void setValues(List<Long> data) {

        resetTableAndData(data);
        // retrieve the first page of results and updateRowData
        final Range range = this.dataTable.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd;
        if ((rangeStart + range.getLength()) > valueIds.size())
            rangeEnd = valueIds.size();
        else
            rangeEnd = (rangeStart + range.getLength());

        // TODO : you have access to the sort info from the table
        // TODO : this goes with the above todo. if we clear all the sort info then we use default else use the top sort
        // TODO : look at the sort method for an example of how to do this
        fetchHasEntryData(rangeStart, rangeEnd);
    }

    @Override
    public void updateRowCount(int size, boolean exact) {
        super.updateRowCount(size, exact);
    }

    @Override
    public void updateRowData(int start, List<T> values) {
        super.updateRowData(start, values);
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

        // if sort data is available, then sort
        // but if we push a column sort, then a sort should always be available.
        // just a question of what is at the top
        boolean sorted = sort(rangeStart, rangeEnd);
        if (!sorted) {
            fetchHasEntryData(rangeStart, rangeEnd);
        }
    }

    protected boolean sort(int rangeStart, int rangeEnd) {

        ColumnSortList sortList = this.dataTable.getColumnSortList();
        final boolean sortAsc;
        final ColumnField sortField;

        if (sortList == null || sortList.size() == 0) {
            // set sort defaults. typically on first page load
            sortAsc = lastSortAsc;
            sortField = lastSortField;
        } else {
            // get sort data
            sortAsc = sortList.get(0).isAscending();
            Column<?, ?> column = sortList.get(0).getColumn();
            int colIndex = getDataTable().getColumns().indexOf(column); //TODO : find a way to perform a single lookup to retrieve the field
            if (colIndex < 0)
                sortField = lastSortField;
            else
                sortField = getDataTable().getColumns().get(colIndex).getField();
        }

        // do not resort if sort params have not changed
        if (sortAsc == lastSortAsc && sortField == lastSortField)
            return false;

        service.retrieveSamplesByDepositor(AppController.sessionId, null, sortField, sortAsc,
            new AsyncCallback<LinkedList<Long>>() {

                // re-sorting
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(LinkedList<Long> result) {
                    if (result == null)
                        return;

                    setValues(result);
                    lastSortAsc = sortAsc;
                    lastSortField = sortField;
                }
            });

        return true;
    }

    protected void fetchHasEntryData(final int rangeStart, final int rangeEnd) {

        if (this.valueIds == null || this.valueIds.isEmpty())
            return;

        int factor = (rangeEnd - rangeStart) * 9; // factor is used to retrieve more data than will be shown; for caching
        factor = (factor + rangeEnd) > valueIds.size() ? valueIds.size() : (factor + rangeEnd);
        LinkedList<Long> subList = new LinkedList<Long>(valueIds.subList(rangeStart, factor));

        final LinkedList<Long> realValues = new LinkedList<Long>(subList);
        boolean asc;
        ColumnSortList sortList = this.dataTable.getColumnSortList();
        if (sortList == null || sortList.size() == 0)
            asc = false;
        else
            asc = sortList.get(0).isAscending();

        retrieveValues(realValues, rangeStart, rangeEnd, asc);
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
            boolean asc);

    public HasEntryDataTable<T> getDataTable() {
        return this.dataTable;
    }

    protected LinkedList<T> getResults() {
        return this.results;
    }
}
