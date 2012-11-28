package org.jbei.ice.client.collection;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class FolderEntryDataProvider extends EntryDataViewDataProvider implements IHasNavigableData {

    private FolderDetails details;

    public FolderEntryDataProvider(DataTable<EntryInfo> view, RegistryServiceAsync service) {
        super(view, service);
    }

    /**
     * Fetches the data user is interested in viewing
     */
    protected void fetchEntryData(final ColumnField field, final boolean ascending, int rangeStart, int rangeEnd) {
        if (resultSize == 0)
            return;

        int factor = (rangeEnd - rangeStart) * 3;  //  pages in advance
//        factor = (factor + rangeEnd) > resultSize ? resultSize : (factor + rangeEnd);
        makeServiceCall(field, ascending, rangeStart, factor);
    }

    protected void makeServiceCall(ColumnField field, boolean ascending, int rangeStart, int factor) {
        service.retrieveAllVisibleEntryIDs(AppController.sessionId, details, field, ascending, rangeStart, factor,
                                           new AsyncCallback<FolderDetails>() {

                                               @Override
                                               public void onFailure(Throwable caught) {
                                                   Window.alert(caught.getMessage());
                                               }

                                               @Override
                                               public void onSuccess(FolderDetails result) {
                                                   details = result;
                                                   cachedEntries.addAll(result.getEntries());
                                                   // retrieve the first page of results and updateRowData
                                                   final Range range = table.getVisibleRange();
                                                   final int rangeStart = range.getStart();
                                                   int rangeEnd = rangeStart + range.getLength();
                                                   if (rangeEnd > resultSize)
                                                       rangeEnd = resultSize;
                                                   updateRowData(rangeStart,
                                                                 cachedEntries.subList(rangeStart, rangeEnd));
                                               }
                                           });
    }

    public void setFolderData(FolderDetails details) {
        reset();
        this.details = details;
        if (details == null) {
            updateRowCount(0, true);
            return;
        }

        cachedEntries.addAll(details.getEntries());
        resultSize = (int) details.getCount();
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        final Range range = this.table.getVisibleRange();
        final int rangeStart = range.getStart();
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > resultSize)
            rangeEnd = resultSize;

        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));
    }
}

