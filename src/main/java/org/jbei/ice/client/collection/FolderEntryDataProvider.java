package org.jbei.ice.client.collection;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class FolderEntryDataProvider extends EntryDataViewDataProvider {

    private FolderDetails details;
    private final EntryTablePager pager;

    public FolderEntryDataProvider(CollectionDataTable view, RegistryServiceAsync service) {
        super(view, service);
        pager = view.getPager();
    }

    private void retrieveAllVisibleParts(ColumnField field, boolean asc, final int start, final int factor,
            final boolean reset) {
        service.retrieveAllVisibleEntrys(
                ClientController.sessionId, details, field, asc, start, factor,
                new AsyncCallback<FolderDetails>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(FolderDetails result) {
                        details = result;
                        if (result == null) {
                            resetLoading();
                            return;
                        }

                        if (reset)
                            setFolderData(details, false);
                        else
                            cachedEntries.addAll(result.getEntries());

                        resetLoading();
                    }
                });

    }

    protected void resetLoading() {
        pager.determineSetNextEnabled();
        pager.setDefaultHTML();
    }

    private void retrieveUserEntrys(ColumnField field, boolean asc, final int start, final int factor,
            final boolean reset) {
        service.retrieveUserEntries(
                ClientController.sessionId, ClientController.account.getId() + "", field, asc, start, factor,
                new AsyncCallback<FolderDetails>() {

                    @Override
                    public void onSuccess(FolderDetails result) {
                        details = result;
                        if (result == null) {
                            resetLoading();
                            return;
                        }

                        if (reset)
                            setFolderData(details, false);
                        else
                            cachedEntries.addAll(result.getEntries());
                        resetLoading();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(caught.getMessage());
                        resetLoading();
                    }
                });
    }

    private void retrieveEntriesForFolder(ColumnField field, boolean asc, final int start, final int factor,
            final boolean reset) {
        service.retrieveEntriesForFolder(
                ClientController.sessionId, details.getId(), field, asc, start, factor,
                new AsyncCallback<FolderDetails>() {

                    @Override
                    public void onSuccess(final FolderDetails result) {
                        details = result;
                        if (result == null) {
                            resetLoading();
                            return;
                        }

                        if (reset)
                            setFolderData(details, false);
                        else
                            cachedEntries.addAll(result.getEntries());
                        resetLoading();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(caught.getMessage());
                    }
                });
    }

    @Override
    protected void fetchEntryData(ColumnField field, boolean asc, final int start, final int factor,
            final boolean reset) {   // if reset setFolderData() is called instead of adding to cache

        if (!reset && (cachedEntries.size() == resultSize || cachedEntries.size() >= (start + factor))) {
            updateRowData(start, cachedEntries.subList(start, (start + factor)));
            return;
        }

        if (!reset && resultSize != (start + factor)) {
            pager.setLoading();
        }

        switch ((int) details.getId()) {
            case -1:
                retrieveAllVisibleParts(field, asc, start, factor, reset);
                break;
            case 0:
                retrieveUserEntrys(field, asc, start, factor, reset);
                break;
            default:
                retrieveEntriesForFolder(field, asc, start, factor, reset);
        }
    }

    public void reset() {
        this.cachedEntries.clear();
        details = null;
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

    public void setFolderData(FolderDetails details, boolean resetSort) {
        if (resetSort)
            lastSortField = null;

        reset();  // lastSortField is set reset to CREATED if set to null (resetSort == true)
        this.details = details;
        if (details == null) {
            updateRowCount(0, true);
            return;
        }

        cachedEntries.addAll(details.getEntries());
        resultSize = (int) details.getCount();
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        Range range = this.table.getVisibleRange();
        int rangeStart = 0;
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > resultSize)
            rangeEnd = resultSize;

        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));
        table.setPageStart(0);

        rangeStart = rangeEnd;
        if (rangeEnd < resultSize) {
            rangeEnd += range.getLength();
            if (rangeEnd > resultSize)
                rangeEnd = resultSize;
            cacheMore(lastSortField, lastSortAsc, rangeStart, rangeEnd);
        }
    }
}

