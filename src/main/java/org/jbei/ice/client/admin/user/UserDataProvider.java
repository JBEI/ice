package org.jbei.ice.client.admin.user;

import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AccountResults;

import java.util.LinkedList;

/**
 * @author Hector Plahar
 */
public class UserDataProvider extends AsyncDataProvider<AccountInfo> {

    protected int resultSize;
    protected LinkedList<AccountInfo> cachedEntries;
    protected final RegistryServiceAsync service;
    protected final UserTable table;
    protected ColumnField lastSortField;
    protected boolean lastSortAsc = false;

    public UserDataProvider(UserTable view, RegistryServiceAsync service) {
        this.table = view;
        this.service = service;
        cachedEntries = new LinkedList<AccountInfo>();

        // connect sorting to async handler
        ColumnSortEvent.AsyncHandler columnSortHandler = new ColumnSortEvent.AsyncHandler(table);
        table.addColumnSortHandler(columnSortHandler);

//        DataTable<EntryInfo>.DataTableColumn<?> defaultSortField = this.table.getColumn(ColumnField.CREATED);
//
//        if (defaultSortField != null) {
//            ColumnSortList.ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
//            this.table.getColumnSortList().push(info);
//        }

        this.addDataDisplay(this.table);
    }

    @Override
    protected void onRangeChanged(HasData<AccountInfo> display) {
        if (resultSize == 0)   // display changed its range of interest but no data
            return;

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > resultSize ? resultSize
                : (rangeStart + range.getLength());

        // sort did not change
        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));

        if (rangeEnd == cachedEntries.size()) { // or close enough within some delta, retrieve more
            cacheMore(lastSortField, lastSortAsc, rangeEnd, rangeEnd + range.getLength());
        }
    }

    protected void cacheMore(final ColumnField field, final boolean ascending, int rangeStart, int rangeEnd) {
        int factor = (rangeEnd - rangeStart) * 2;  //  pages in advance
        fetchEntryData(field, ascending, rangeStart, factor, false);
    }

    protected void fetchEntryData(ColumnField field, boolean ascending, int start, int factor, final boolean reset) {
        try {
            service.retrieveAllUserAccounts(ClientController.sessionId, start, factor,
                    new AsyncCallback<AccountResults>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(AccountResults result) {
                            if (result == null) {
                                return;
                            }

                            if (reset)
                                setResultsData(result, false);
                            else {
                                cachedEntries.addAll(result.getResults());
//                    pager.setLoading(true);  //todo
                            }
                        }
                    });
        } catch (AuthenticationException ar) {

        }
    }

    public void setResultsData(AccountResults results, boolean resetSort) {
        if (resetSort)
            lastSortField = null;

        reset();
        if (results == null) {
            updateRowCount(0, true);
            return;
        }

        cachedEntries.addAll(results.getResults());
        resultSize = (int) results.getResultCount();
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        final Range range = this.table.getVisibleRange();
        final int rangeStart = 0;
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > resultSize)
            rangeEnd = resultSize;

        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));
        table.setPageStart(0);
    }

    public void reset() {
        this.cachedEntries.clear();
        this.table.setVisibleRangeAndClearData(table.getVisibleRange(), false);

//        // reset sort
//        if (lastSortField == null) {
//            lastSortAsc = false;
//            lastSortField = ColumnField.CREATED;
//
//            this.table.getColumnSortList().clear();
//            DataTable<EntryInfo>.DataTableColumn<?> defaultSortField = this.table.getColumn(lastSortField);
//
//            if (defaultSortField != null) {
//                ColumnSortList.ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
//                this.table.getColumnSortList().push(info);
//            }
//        }
    }
}
