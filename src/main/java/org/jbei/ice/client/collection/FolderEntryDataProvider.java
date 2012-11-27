package org.jbei.ice.client.collection;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class FolderEntryDataProvider extends EntryDataViewDataProvider implements IHasNavigableData {

    private int resultSize;
    private FolderDetails details;

    public FolderEntryDataProvider(DataTable<EntryInfo> view, RegistryServiceAsync service) {
        super(view, service);
    }

    @Override
    public int getSize() {
        return resultSize;
    }

    public void reset() {
        super.reset();
        resultSize = 0;
    }

    @Override
    protected void onRangeChanged(final HasData<EntryInfo> display) {

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > resultSize ? resultSize
                : (rangeStart + range.getLength());

        if (sort(rangeStart, rangeEnd))
            return;

        // did not need to sort so use the cache
        ArrayList<EntryInfo> show = new ArrayList<EntryInfo>();
        show.addAll(results.subList(rangeStart, rangeEnd));

        updateRowData(rangeStart, show);
    }

    /**
     * Fetches the data user is interested in viewing
     */
    protected void fetchEntryData(final ColumnField field, final boolean ascending, int rangeStart, int rangeEnd) {
        if (resultSize == 0)
            return;

        int factor = (rangeEnd - rangeStart) * 3; // EntryTablePager.JUMP_PAGE_COUNT; // get 4 pages in advance
        factor = (factor + rangeEnd) > resultSize ? resultSize : (factor + rangeEnd);
        makeServiceCall(field, ascending, rangeStart, factor);
    }

    // this method should be called after sorting, if sorting is desired
    protected void retrieveEntryData(final ColumnField field, final boolean ascending, int rangeStart, int rangeEnd) {
        int factor = (rangeEnd - rangeStart) * EntryTablePager.JUMP_PAGE_COUNT; //  pages in advance
        factor = (factor + rangeEnd) > resultSize ? resultSize : (factor + rangeEnd);
        makeServiceCall(field, ascending, rangeStart, factor);
    }

    protected void makeServiceCall(final ColumnField field, final boolean ascending, int rangeStart, int factor) {
        service.retrieveAllVisibleEntryIDs(AppController.sessionId, details, field, ascending, rangeStart, factor,
                                           new AsyncCallback<FolderDetails>() {

                                               @Override
                                               public void onFailure(Throwable caught) {
                                                   Window.alert(caught.getMessage());
                                               }

                                               @Override
                                               public void onSuccess(FolderDetails result) {
                                                   setData(result);
                                               }
                                           });
    }

    public void setData(FolderDetails details) {
        this.details = details;
        results.addAll(details.getEntries());
        resultSize = (int) details.getCount();
        updateRowCount(resultSize, true);
        lastSortAsc = false;
        lastSortField = ColumnField.CREATED;

        // retrieve the first page of results and updateRowData
        final Range range = this.table.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd;
        if ((rangeStart + range.getLength()) > resultSize)
            rangeEnd = resultSize;
        else
            rangeEnd = (rangeStart + range.getLength());

        if (results.size() >= rangeEnd) {
            updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
        } else {
            // not so fetch more; fetch more
            retrieveEntryData(ColumnField.CREATED, true, rangeStart, rangeEnd);
        }
    }
}

