package org.jbei.ice.client.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

// TODO : consider using composition instead of inheritance for the dataProvider
// TODO : especially considering addDataDisplay();

// Takes care of retrieving all data page, by page

public class EntryDataViewDataProvider extends AsyncDataProvider<EntryInfo> {

    protected final List<Long> valuesIds;
    protected LinkedList<EntryInfo> results;
    private final RegistryServiceAsync service;
    private final DataTable<EntryInfo> table;
    private ColumnField lastSortField = ColumnField.CREATED;
    private boolean lastSortAsc = false;

    public EntryDataViewDataProvider(DataTable<EntryInfo> view, List<Long> data,
            RegistryServiceAsync service) {

        this.table = view;
        this.service = service;
        this.valuesIds = new LinkedList<Long>();
        results = new LinkedList<EntryInfo>();

        this.table.addColumnSortHandler(new AsyncHandler(this.table));
        DataTable<EntryInfo>.DataTableColumn<?> defaultSortField = this.table
                .getColumn(lastSortField);

        if (defaultSortField != null) {
            ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, false);
            this.table.getColumnSortList().push(info);
        }

        if (!data.isEmpty())
            this.setValues(data);

        this.addDataDisplay(this.table);
    }

    public EntryDataViewDataProvider(DataTable<EntryInfo> view, RegistryServiceAsync service) {
        this(view, new LinkedList<Long>(), service);
    }

    // clears all table data 
    private void resetTableAndData(List<Long> data) {

        this.results.clear();
        this.valuesIds.clear();
        this.table.setVisibleRangeAndClearData(table.getVisibleRange(), false);
        this.valuesIds.addAll(data);
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

        if ((rangeStart + range.getLength()) > valuesIds.size())
            rangeEnd = valuesIds.size();
        else
            rangeEnd = (rangeStart + range.getLength());

        // TODO : you have access to the sort info from the table
        // TODO : this goes with the above todo. if we clear all the sort info then we use default else use the top sort
        fetchEntryData(ColumnField.CREATED, false, rangeStart, rangeEnd);
    }

    @Override
    protected void onRangeChanged(final HasData<EntryInfo> display) {

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > valuesIds.size() ? valuesIds.size()
                : (rangeStart + range.getLength());

        boolean sorted = sort(rangeStart, rangeEnd);
        if (!sorted) {

            // check if it has been pre-fetched/cached
            if (results.size() >= rangeEnd) {

                ArrayList<EntryInfo> show = new ArrayList<EntryInfo>();
                show.addAll(results.subList(rangeStart, rangeEnd));
                updateRowData(rangeStart, show);
            } else {

                fetchEntryData(lastSortField, lastSortAsc, rangeStart, rangeEnd);
            }
        }
    }

    /**
     * 
     * @param sortList
     * @param rangeStart
     * @param rangeEnd
     * @return
     */
    protected boolean sort(int rangeStart, int rangeEnd) {

        ColumnSortList sortList = this.table.getColumnSortList();
        final boolean sortAsc;
        final ColumnField sortField;

        if (sortList == null || sortList.size() == 0) {
            sortAsc = lastSortAsc;
            sortField = lastSortField;
        } else {

            sortAsc = sortList.get(0).isAscending();

            int colIndex = this.table.getColumns().indexOf(sortList.get(0).getColumn());
            if (colIndex < 0)
                sortField = lastSortField;
            else
                sortField = this.table.getColumns().get(colIndex).getField();
        }

        if (sortAsc == lastSortAsc && sortField == lastSortField)
            return false;

        results.clear();
        lastSortAsc = sortAsc;
        lastSortField = sortField;
        fetchEntryData(sortField, sortAsc, rangeStart, rangeEnd);
        return true;
    }

    protected void fetchEntryData(ColumnField field, boolean ascending, final int rangeStart,
            final int rangeEnd) {

        if (valuesIds == null || valuesIds.isEmpty())
            return;

        // TODO : index out of bounds exception here when we page to the last page and sort
        // TODO : this is because we clear results and when we do not retrieve enough (factor below) 
        // TODO : solution is to go to page one when user sorts

        int factor = (rangeEnd - rangeStart) * 9; // get nine pages in advance
        factor = (factor + rangeEnd) > valuesIds.size() ? valuesIds.size() : (factor + rangeEnd);
        List<Long> subList = valuesIds.subList(rangeStart, factor);
        final ArrayList<Long> realValues = new ArrayList<Long>(subList);

        service.retrieveEntryData(AppController.sessionId, realValues, field, ascending,
            new AsyncCallback<ArrayList<EntryInfo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(ArrayList<EntryInfo> result) {

                    results.addAll(result);
                    int end = rangeEnd;
                    if (rangeEnd > results.size())
                        end = results.size();
                    updateRowData(rangeStart, results.subList(rangeStart, end));
                }
            });
    }
}
