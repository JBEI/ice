package org.jbei.ice.client.component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.component.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;

import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

// TODO : consider using composition instead of inheritance for the dataProvider
// TODO : especially considering addDataDisplay();

// Takes care of retrieving all data page, by page

public class EntryDataViewDataProvider extends AsyncDataProvider<EntryData> {

    protected final List<Long> values;
    protected LinkedList<EntryData> results;
    private RegistryServiceAsync service;
    private final DataTable<EntryData> table;

    public EntryDataViewDataProvider(DataTable<EntryData> view, List<Long> data,
            RegistryServiceAsync service) {

        this.values = new LinkedList<Long>(data);
        this.service = service;

        // set total number available
        //        updateRowCount(data.size(), true);
        results = new LinkedList<EntryData>();
        this.table = view;
        this.addDataDisplay(this.table);
        AsyncHandler columnSortHandler = new AsyncHandler(this.table);
        this.table.addColumnSortHandler(columnSortHandler);
        DataTable<EntryData>.DataTableColumn<?> createdField = this.table
                .getColumn(ColumnField.CREATED);
        if (createdField != null)
            this.table.getColumnSortList().push(createdField);
    }

    public EntryDataViewDataProvider(DataTable<EntryData> view, RegistryServiceAsync service) {
        this(view, new LinkedList<Long>(), service);
    }

    // clears all table data 
    private void resetTableAndData(List<Long> data) {

        this.results.clear();
        this.values.clear();
        this.table.setVisibleRangeAndClearData(table.getVisibleRange(), false);
        this.values.addAll(data);
        updateRowCount(data.size(), true);
        // TODO : reset sort by clearing or just maintain the last one
        //        this.table.getColumnSortList().clear();
    }

    public void setValues(List<Long> data) {

        resetTableAndData(data);

        // retrieve the first page of results and updateRowData
        final Range range = this.getRanges()[0];
        final int rangeStart = range.getStart();
        final int rangeEnd;

        if ((rangeStart + range.getLength()) > values.size())
            rangeEnd = values.size();
        else
            rangeEnd = (rangeStart + range.getLength());

        // TODO : you have access to the sort info from the table
        // TODO : this goes with the above todo. if we clear all the sort info then we use default else use the top sort
        fetchEntryData(ColumnField.CREATED, false, rangeStart, rangeEnd);
    }

    @Override
    public void updateRowCount(int size, boolean exact) {
        super.updateRowCount(size, exact);
    }

    @Override
    public void updateRowData(int start, List<EntryData> values) {
        super.updateRowData(start, values);
    }

    @Override
    protected void onRangeChanged(final HasData<EntryData> display) {

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > values.size() ? values.size()
                : (rangeStart + range.getLength());

        boolean sorted = sort(this.table.getColumnSortList(), rangeStart, rangeEnd);
        if (!sorted) {

            // check if it has been pre-fetched/cached
            if (results.size() >= rangeEnd) {

                ArrayList<EntryData> show = new ArrayList<EntryData>();
                show.addAll(results.subList(rangeStart, rangeEnd));
                updateRowData(rangeStart, show);
            } else {

                fetchEntryData(null, false, rangeStart, rangeEnd);
            }
        }
    }

    // TODO : check if data is already sorted to prevent retrieving
    /**
     * 
     * @param sortList
     * @param rangeStart
     * @param rangeEnd
     * @return
     */
    protected boolean sort(ColumnSortList sortList, int rangeStart, int rangeEnd) {

        if (sortList == null || sortList.size() == 0) {
            return false;
        }

        // get sort data
        boolean asc = sortList.get(0).isAscending();

        int colIndex = this.table.getColumns().indexOf(sortList.get(0).getColumn());
        if (colIndex == -1)
            return false; // TODO : this will be pretty unusual

        ColumnField field = this.table.getColumns().get(colIndex).getField();
        fetchEntryData(field, asc, rangeStart, rangeEnd);
        return true;
    }

    protected void fetchEntryData(ColumnField field, boolean ascending, final int rangeStart,
            final int rangeEnd) {

        if (values == null || values.isEmpty())
            return;

        int factor = (rangeEnd - rangeStart) * 9; // get nine pages in advance
        factor = (factor + rangeEnd) > values.size() ? values.size() : (factor + rangeEnd);
        List<Long> subList = values.subList(rangeStart, factor);
        final ArrayList<Long> realValues = new ArrayList<Long>(subList);

        service.retrieveEntryData(AppController.sessionId, realValues, field, ascending,
            new AsyncCallback<ArrayList<EntryData>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(ArrayList<EntryData> result) {
                    results.addAll(result);
                    updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
                }
            });
    }
}
